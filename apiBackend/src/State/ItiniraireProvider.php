<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Repository\ItiniraireRepository;
use Symfony\Component\HttpFoundation\JsonResponse;

final class ItiniraireProvider implements ProviderInterface
{
    public function __construct(
        private readonly ItiniraireRepository $itiniraireRepo,
    ) {}

    public function provide(Operation $operation, array $uriVariables = [], array $context = []): JsonResponse
    {
        $itineraires = $this->itiniraireRepo->findAll();

        $donnees = array_map(function($iti) {
            // On transforme la collection de ListeLieux en un tableau simple de lieux
            $lieuxDetails = [];
            foreach ($iti->getListeLieux() as $relation) {
                $lieu = $relation->getIdLieu();
                $lieuxDetails[] = [
                    'id'   => $lieu->getId(),
                    'nom'  => $lieu->getNom(),
                    'lat'  => $lieu->getLatitude(),
                    'lng'  => $lieu->getLongitude()
                ];
            }

            return [
                'id'         => $iti->getId(),
                'dureTotal'  => $iti->getDureTotal(),
                'nbLieux'    => count($lieuxDetails),
                'lieux'      => $lieuxDetails
            ];
        }, $itineraires);

        return new JsonResponse(['data' => $donnees]);
    }
}
