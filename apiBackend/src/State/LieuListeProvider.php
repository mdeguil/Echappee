<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Dto\LieuListeDto;
use App\Repository\LieuRepository;
use Symfony\Component\HttpFoundation\JsonResponse;

/**
 * State Provider pour GET /api/lieux.
 * Retourne une réponse JSON au format :
 * {
 *   "data": [ { "id": 1, "nom": "...", ... } ]
 * }
 */
final class LieuListeProvider implements ProviderInterface
{
    public function __construct(
        private readonly LieuRepository $depotLieu,
    ) {}

    public function provide(Operation $operation, array $uriVariables = [], array $context = []): JsonResponse
    {
        $lieux = $this->depotLieu->findAll();

        $donnees = array_map(
            fn($lieu) => new LieuListeDto(
                id:          $lieu->getId(),
                nom:         $lieu->getNom(),
                photo:       $lieu->getPhoto(),
                noteMoyen:   $lieu->getNoteMoyen(),
                latitude:    $lieu->getLatitude(),
                longitude:   $lieu->getLongitude(),
                categorie:   $lieu->getCategorie(),
                commentaire: $lieu->getCommentaires()?->getMessage(),
            ),
            $lieux
        );

        return new JsonResponse([
            'data' => array_map(
                fn(LieuListeDto $dto) => [
                    'id'          => $dto->id,
                    'nom'         => $dto->nom,
                    'photo'       => $dto->photo,
                    'noteMoyen'   => $dto->noteMoyen,
                    'latitude'    => $dto->latitude,
                    'longitude'   => $dto->longitude,
                    'categorie'   => $dto->categorie,
                    'commentaire' => $dto->commentaire,
                ],
                $donnees
            ),
        ]);
    }
}
