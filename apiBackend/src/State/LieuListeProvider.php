<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Dto\LieuListeDto;
use App\Repository\LieuRepository;
use Symfony\Component\HttpFoundation\JsonResponse;

// php bin/console app:importer-lieux
// php -S 0.0.0.0:8000 -t public

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
                // On expose le nom de la catégorie, pas l'objet entier
                'categorie'   => $lieu->getCategorie()?->getNom(),
                'commentaire' => $lieu->getCommentaires()?->getMessage(),
            ],
            $lieux
        );

        return new JsonResponse(['data' => $donnees]);
    }
}
