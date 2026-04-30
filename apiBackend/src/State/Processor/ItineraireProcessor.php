<?php
namespace App\State\Processor;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
use App\Dto\ItineraireOutput;
use App\Dto\ListeLieuxOutput;
use App\Entity\Itiniraire;
use App\Entity\ListeLieux;
use App\Repository\LieuRepository;
use App\Repository\UtilisateurRepository; // Ajout du Repository
use Doctrine\ORM\EntityManagerInterface;

class ItineraireProcessor implements ProcessorInterface
{
    public function __construct(
        private EntityManagerInterface $em,
        private LieuRepository         $lieuRepository,
        private UtilisateurRepository  $utilisateurRepository,
    ) {}

    public function process(mixed $data, Operation $operation, array $uriVariables = [], array $context = []): ItineraireOutput
    {
        $itineraire = new Itiniraire();
        $itineraire->setDureTotal($data->dureTotal);

        // --- GESTION DE L'UTILISATEUR ---
        if ($data->utilisateur) {
            $user = $this->utilisateurRepository->find($data->utilisateur);
            if ($user) {
                $itineraire->setUtilisateur($user); // On lie l'entité Utilisateur
            }
        }

        // --- GESTION DES LIEUX ---
        foreach ($data->listeLieux as $idLieu) {
            $lieu = $this->lieuRepository->find((int) $idLieu);
            if ($lieu === null) continue;

            $listeLieux = new ListeLieux();
            $listeLieux->setIdLieu($lieu);
            $listeLieux->setIdItiniraire($itineraire);

            $this->em->persist($listeLieux);
            $itineraire->addListeLieux($listeLieux);
        }

        $this->em->persist($itineraire);
        $this->em->flush();

        // --- CONSTRUCTION DE L'OUTPUT ---
        $output           = new ItineraireOutput();
        $output->id        = $itineraire->getId();
        $output->dureTotal = $itineraire->getDureTotal();

        // On renvoie l'ID de l'utilisateur dans l'output
        if ($itineraire->getUtilisateur()) {
            $output->utilisateur = $itineraire->getUtilisateur()->getId();
        }

        foreach ($itineraire->getListeLieux() as $ll) {
            $llOutput          = new ListeLieuxOutput();
            $llOutput->id       = $ll->getId();
            $llOutput->idLieu   = $ll->getIdLieu()->getId();
            $llOutput->nomLieu  = $ll->getIdLieu()->getNom();
            $output->listeLieux[] = $llOutput;
        }

        return $output;
    }
}
