<?php

namespace App\State\Provider;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Repository\LieuRepository;
use Symfony\Component\HttpFoundation\JsonResponse;


final class LieuListeProvider implements ProviderInterface
{
    public function __construct(
        private readonly LieuRepository $depotLieu,
    ) {}

    public function provide(Operation $operation, array $uriVariables = [], array $context = []): JsonResponse
    {
        $lieux = $this->depotLieu->findAll();

        $donnees = array_map(
            fn($lieu) => [
                'id'          => $lieu->getId(),
                'nom'         => $lieu->getNom(),
                'photo'       => $lieu->getPhoto(),
                'noteMoyen'   => $lieu->getNoteMoyen(),
                'latitude'    => $lieu->getLatitude(),
                'longitude'   => $lieu->getLongitude(),
                'categorie'   => $lieu->getCategorie()?->getNom(),
                'commentaire' => $lieu->getCommentaires()?->getMessage(),
            ],
            $lieux
        );

        return new JsonResponse(['data' => $donnees]);
    }
}
