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
     * Importe les lieux du patrimoine culturel de la Charente depuis l'API Open Data.
     * * Cette méthode orchestre l'importation complète :
     * 1. Initialise les catégories nécessaires en base de données.
     * 2. Récupère les données géographiques (GeoJSON) via une requête HTTP.
     * 3. Traite chaque élément pour création ou mise à jour.
     * 4. Persiste les changements en base de données.
     *
     * @return array{creations: int, misesAJour: int, erreurs: int, total: int}
     * Le bilan chiffré de l'opération d'importation.
     * @throws \Throwable Si une erreur majeure survient durant l'appel API.
     */
    public function importerLieuxCharente(): array
    {
        $this->initialiserCategories();

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

    // ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

    /**
     * Initialise et synchronise les catégories de patrimoine culturel en base de données.
     * * Cette méthode récupère l'ensemble des données depuis l'API externe pour extraire
     * la liste unique des types de patrimoine. Elle vérifie ensuite l'existence de chaque
     * catégorie en base et ne persiste que les nouvelles entrées pour éviter les doublons.
     *
     * @return void
     * @throws \Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface
     * Si la connexion à l'API échoue.
     * @throws \Symfony\Contracts\HttpClient\Exception\DecodingExceptionInterface
     * Si le format de la réponse API est invalide.
     */
    private function initialiserCategories(): void
    {
        $this->journalisation->info('[Data16] Initialisation des catégories...');

        $reponse  = $this->clientHttp->request('GET', self::URL_DATASET);
        $features = $reponse->toArray()['features'] ?? [];

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

    // ─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

    /**
     * Traite un élément (feature) de l'API pour créer ou mettre à jour un Lieu et son Détail.
     * * Cette méthode assure le mapping des propriétés GeoJSON vers les entités :
     * 1. Vérifie la présence du nom (identifiant fonctionnel).
     * 2. Gère l'existence préalable du lieu (Upsert).
     * 3. Associe la catégorie correspondante.
     * 4. Remplit les métadonnées du détail (description, horaires, accessibilité).
     * 5. Tronque les chaînes de caractères à 255 caractères pour la sécurité SQL.
     *
     * @param array $feature Les données brutes d'un lieu issues du GeoJSON.
     * @return bool True si un nouveau lieu a été créé, false s'il s'agit d'une mise à jour.
     * @throws \InvalidArgumentException Si le nom du lieu est manquant ou vide.
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

        $lieu->setNom(mb_substr($nom, 0, 255));
        $lieu->setLatitude(isset($p['Latitude'])  ? (float) $p['Latitude']  : null);
        $lieu->setLongitude(isset($p['Longitude']) ? (float) $p['Longitude'] : null);
        $lieu->setPhoto(null);

        $nomCategorie = $p['Type_de_patrimoine_culturel'] ?? null;
        $categorie    = $nomCategorie
            ? $this->depotCategorie->findOneBy(['nom' => $nomCategorie])
            : null;
        $lieu->setCategorie($categorie);

        $detail = $lieu->getDetail() ?? new DetailLieu();

        $detail->setDescription(mb_substr((string) ($p['Descriptif_court'] ?? $p['Descriptif_détaillé'] ?? ''), 0, 255));
        $detail->setHoraires(mb_substr((string) ($p['Période_en_clair'] ?? 'Non renseignés'), 0, 255));
        $detail->setAccessibilite(mb_substr((string) ($p['Marque_Tourisme_et_Handicap'] ?? 'Non renseignée'), 0, 255));
        $detail->setTarif(0);
        $detail->setPhotos(null);

        $detail->setLieu($lieu);
        $lieu->setDetail($detail);

        $this->gestionnaireEntites->persist($detail);
        $this->gestionnaireEntites->persist($lieu);

        return $estNouveau;
    }
}
