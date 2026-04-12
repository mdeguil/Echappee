<?php
namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
use App\Dto\ItineraireInput;
use App\Dto\ItineraireOutput;
use App\Dto\ListeLieuxOutput;
use App\Entity\Itiniraire;
use App\Entity\ListeLieux;
use App\Repository\LieuRepository;
use Doctrine\ORM\EntityManagerInterface;

class ItineraireProcessor implements ProcessorInterface
{
    public function __construct(
        private EntityManagerInterface $em,
        private LieuRepository         $lieuRepository,
    ) {}

    public function process(mixed $data, Operation $operation, array $uriVariables = [], array $context = []): ItineraireOutput
    {
        $itineraire = new Itiniraire();
        $itineraire->setDureTotal($data->dureTotal);

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

        $output           = new ItineraireOutput();
        $output->id        = $itineraire->getId();
        $output->dureTotal = $itineraire->getDureTotal();

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
