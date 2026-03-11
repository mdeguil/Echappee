<?php

namespace App\Service;

use App\Entity\DetailLieu;
use App\Entity\Lieu;
use App\Repository\LieuRepository;
use Doctrine\ORM\EntityManagerInterface;
use Psr\Log\LoggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * Importe le patrimoine culturel de la Charente depuis l'API ArcGIS Data16.
 * API publique — aucune clé requise.
 *
 * Correspondance API → entités :
 *   properties.Nom                         → Lieu.nom
 *   properties.Latitude                    → Lieu.latitude
 *   properties.Longitude                   → Lieu.longitude
 *   properties.Type_de_patrimoine_culturel → Lieu.categorie (string)
 *   properties.Descriptif_court            → DetailLieu.description
 *   properties.Période_en_clair            → DetailLieu.horaires
 *   properties.Marque_Tourisme_et_Handicap → DetailLieu.accessibilite
 *   0 (absent dans l'API)                  → DetailLieu.tarif
 *   properties.Moyens_de_communication     → DetailLieu.photos (URL site web)
 */
class ImportationLieuxService
{
    private const URL_DATASET = 'https://services8.arcgis.com/Mu477K6amNa9Pa6f/arcgis/rest/services/Patrimoine_Culturel_de_Charente/FeatureServer/3/query?outFields=*&where=1%3D1&f=geojson';

    public function __construct(
        private readonly HttpClientInterface    $clientHttp,
        private readonly EntityManagerInterface $gestionnaireEntites,
        private readonly LieuRepository         $depotLieu,
        private readonly LoggerInterface        $journalisation,
    ) {}

    /**
     * @return array{creations: int, misesAJour: int, erreurs: int, total: int}
     */
    public function importerLieuxCharente(): array
    {
        $this->journalisation->info('[Data16] Import du patrimoine culturel...');

        $reponse = $this->clientHttp->request('GET', self::URL_DATASET);

        if ($reponse->getStatusCode() !== 200) {
            throw new \RuntimeException('Erreur HTTP : ' . $reponse->getStatusCode());
        }

        $features = $reponse->toArray()['features'] ?? [];

        if (empty($features)) {
            $this->journalisation->warning('[Data16] Aucun résultat.');
            return ['creations' => 0, 'misesAJour' => 0, 'erreurs' => 0, 'total' => 0];
        }

        $this->journalisation->info(sprintf('[Data16] %d lieux trouvés.', count($features)));

        $creations  = 0;
        $misesAJour = 0;
        $erreurs    = 0;

        foreach ($features as $feature) {
            try {
                $this->traiterUnFeature($feature) ? $creations++ : $misesAJour++;
            } catch (\Throwable $e) {
                $erreurs++;
                $id = $feature['properties']['OBJECTID'] ?? '?';
                $this->journalisation->warning("[Data16] Feature #{$id} ignoré : " . $e->getMessage());
            }
        }

        $this->gestionnaireEntites->flush();

        $bilan = [
            'creations'  => $creations,
            'misesAJour' => $misesAJour,
            'erreurs'    => $erreurs,
            'total'      => $creations + $misesAJour,
        ];

        $this->journalisation->info('[Data16] Import terminé.', $bilan);

        return $bilan;
    }

    /**
     * @return bool true = création, false = mise à jour
     */
    private function traiterUnFeature(array $feature): bool
    {
        $p = $feature['properties'] ?? [];

        $nom = trim((string) ($p['Nom'] ?? ''));
        if ($nom === '') {
            throw new \InvalidArgumentException('"Nom" manquant.');
        }

        $lieu       = $this->depotLieu->findOneBy(['nom' => $nom]);
        $estNouveau = ($lieu === null);

        if ($estNouveau) {
            $lieu = new Lieu();
        }

        // ── Lieu ──────────────────────────────────────────────────────────
        $lieu->setNom(mb_substr($nom, 0, 255));
        $lieu->setLatitude(isset($p['Latitude'])  ? (float) $p['Latitude']  : null);
        $lieu->setLongitude(isset($p['Longitude']) ? (float) $p['Longitude'] : null);
        // Stockage direct de la valeur API — ex: "Musée", "Monument", "Parc"
        $lieu->setCategorie($p['Type_de_patrimoine_culturel'] ?? null);

        // ── DetailLieu ────────────────────────────────────────────────────
        $detail = $lieu->getDetail() ?? new DetailLieu();

        $detail->setDescription(
            mb_substr((string) ($p['Descriptif_court'] ?? $p['Descriptif_détaillé'] ?? ''), 0, 255)
        );
        $detail->setHoraires(
            mb_substr((string) ($p['Période_en_clair'] ?? 'Non renseignés'), 0, 255)
        );
        $detail->setAccessibilite(
            mb_substr((string) ($p['Marque_Tourisme_et_Handicap'] ?? 'Non renseignée'), 0, 255)
        );
        $detail->setTarif(0);
        $detail->setPhotos($this->extraireUrlSiteWeb($p['Moyens_de_communication'] ?? null));

        $detail->setLieu($lieu);
        $lieu->setDetail($detail);

        $this->gestionnaireEntites->persist($detail);
        $this->gestionnaireEntites->persist($lieu);

        return $estNouveau;
    }

    /**
     * Extrait l'URL du site web depuis le champ "Moyens_de_communication".
     * Format : "Téléphone : ...|Site web (URL) : https://exemple.fr|..."
     */
    private function extraireUrlSiteWeb(?string $moyensCommunication): ?string
    {
        if ($moyensCommunication === null) {
            return null;
        }

        foreach (explode('|', $moyensCommunication) as $partie) {
            if (str_contains($partie, 'Site web (URL)')) {
                $url = trim(explode('Site web (URL) : ', $partie)[1] ?? '');
                return $url !== '' ? mb_substr($url, 0, 255) : null;
            }
        }

        return null;
    }
}
