<?php

namespace App\Service;

use App\Entity\Categorie;
use App\Entity\DetailLieu;
use App\Entity\Lieu;
use App\Repository\CategorieRepository;
use App\Repository\LieuRepository;
use Doctrine\ORM\EntityManagerInterface;
use Psr\Log\LoggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class ImportationLieuxService
{
    private const URL_DATASET = 'https://services8.arcgis.com/Mu477K6amNa9Pa6f/arcgis/rest/services/Patrimoine_Culturel_de_Charente/FeatureServer/3/query?outFields=*&where=1%3D1&f=geojson';

    public function __construct(
        private readonly HttpClientInterface    $clientHttp,
        private readonly EntityManagerInterface $gestionnaireEntites,
        private readonly LieuRepository         $depotLieu,
        private readonly CategorieRepository    $depotCategorie,
        private readonly LoggerInterface        $journalisation,
    ) {}

    /**
     * @return array{creations: int, misesAJour: int, erreurs: int, total: int}
     */
    public function importerLieuxCharente(): array
    {
        // ── Étape 1 : catégories en base avant les lieux ──────────────────
        $this->initialiserCategories();

        // ── Étape 2 : import des lieux ────────────────────────────────────
        $this->journalisation->info('[Data16] Import du patrimoine culturel...');

        $reponse  = $this->clientHttp->request('GET', self::URL_DATASET);
        $features = $reponse->toArray()['features'] ?? [];

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

        $bilan = ['creations' => $creations, 'misesAJour' => $misesAJour, 'erreurs' => $erreurs, 'total' => $creations + $misesAJour];
        $this->journalisation->info('[Data16] Import terminé.', $bilan);

        return $bilan;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Crée en base les catégories trouvées dans l'API si elles n'existent pas.
     * On fait d'abord un appel API pour récupérer toutes les valeurs distinctes
     * de Type_de_patrimoine_culturel, puis on les insère en un seul flush.
     */
    private function initialiserCategories(): void
    {
        $this->journalisation->info('[Data16] Initialisation des catégories...');

        $reponse  = $this->clientHttp->request('GET', self::URL_DATASET);
        $features = $reponse->toArray()['features'] ?? [];

        // Collecte des valeurs distinctes depuis l'API
        $nomsDistincts = [];
        foreach ($features as $feature) {
            $nom = $feature['properties']['Type_de_patrimoine_culturel'] ?? null;
            if ($nom !== null && !in_array($nom, $nomsDistincts, true)) {
                $nomsDistincts[] = $nom;
            }
        }

        $nouvelles = 0;
        foreach ($nomsDistincts as $nom) {
            if ($this->depotCategorie->findOneBy(['nom' => $nom]) === null) {
                $categorie = new Categorie();
                $categorie->setNom($nom);
                $this->gestionnaireEntites->persist($categorie);
                $nouvelles++;
                $this->journalisation->info("[Data16] Catégorie créée : {$nom}");
            }
        }

        if ($nouvelles > 0) {
            $this->gestionnaireEntites->flush();
        }

        $this->journalisation->info("[Data16] {$nouvelles} catégorie(s) insérée(s).");
    }

    // ─────────────────────────────────────────────────────────────────────────

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

        // Liaison vers l'entité Categorie
        $nomCategorie = $p['Type_de_patrimoine_culturel'] ?? null;
        $categorie    = $nomCategorie
            ? $this->depotCategorie->findOneBy(['nom' => $nomCategorie])
            : null;
        $lieu->setCategorie($categorie);

        // ── DetailLieu ────────────────────────────────────────────────────
        $detail = $lieu->getDetail() ?? new DetailLieu();

        $detail->setDescription(mb_substr((string) ($p['Descriptif_court'] ?? $p['Descriptif_détaillé'] ?? ''), 0, 255));
        $detail->setHoraires(mb_substr((string) ($p['Période_en_clair'] ?? 'Non renseignés'), 0, 255));
        $detail->setAccessibilite(mb_substr((string) ($p['Marque_Tourisme_et_Handicap'] ?? 'Non renseignée'), 0, 255));
        $detail->setTarif(0);
        $detail->setPhotos($this->extraireUrlSiteWeb($p['Moyens_de_communication'] ?? null));

        $detail->setLieu($lieu);
        $lieu->setDetail($detail);

        $this->gestionnaireEntites->persist($detail);
        $this->gestionnaireEntites->persist($lieu);

        return $estNouveau;
    }

    // ─────────────────────────────────────────────────────────────────────────

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
