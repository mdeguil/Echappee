<?php

namespace App\State\Processor;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
use App\Dto\ItineraireOutput;
use App\Dto\ListeLieuxOutput;
use App\Entity\Itiniraire;
use App\Entity\ListeLieux;
use App\Repository\LieuRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\HttpKernel\Exception\AccessDeniedHttpException;
use Symfony\Component\HttpKernel\Exception\UnauthorizedHttpException;

class ItineraireProcessor implements ProcessorInterface
{
    public function __construct(
        private EntityManagerInterface $em,
        private LieuRepository         $lieuRepository,
        private Security               $security,
    ) {}

    public function process(mixed $data, Operation $operation, array $uriVariables = [], array $context = []): ItineraireOutput
    {
        // Récupère l'utilisateur connecté via le JWT — ne jamais lire depuis le body
        $utilisateur = $this->security->getUser();

        if (!$utilisateur) {
            throw new UnauthorizedHttpException('Bearer', 'Utilisateur non authentifié');
        }

        $itineraire = new Itiniraire();
        $itineraire->setDureTotal($data->dureTotal);
        $itineraire->setUtilisateur($utilisateur);

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
        $output            = new ItineraireOutput();
        $output->id        = $itineraire->getId();
        $output->dureTotal = $itineraire->getDureTotal();

        foreach ($itineraire->getListeLieux() as $ll) {
            $llOutput           = new ListeLieuxOutput();
            $llOutput->id       = $ll->getId();
            $llOutput->idLieu   = $ll->getIdLieu()->getId();
            $llOutput->nomLieu  = $ll->getIdLieu()->getNom();
            $output->listeLieux[] = $llOutput;
        }

        return $output;
    }
}
