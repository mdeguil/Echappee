<?php

namespace App\State\Provider;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Repository\DetailLieuRepository;
use Symfony\Component\HttpFoundation\JsonResponse;

final class DetailLieuCollectionProvider implements ProviderInterface
{
    public function __construct(
        private readonly DetailLieuRepository $depotDetailLieu,
    ) {}

    /**
     * Retourne tous les détails de lieux en un seul appel.
     * Utilisé par l'application mobile pour précharger le cache offline.
     *
     * GET /api/detail_lieus
     * Réponse : { "data": [ { "id": 1, "description": "...", ... }, ... ] }
     */
    public function provide(Operation $operation, array $uriVariables = [], array $context = []): JsonResponse
    {
        $details = $this->depotDetailLieu->findAll();

        $donnees = array_map(
            fn($detail) => [
                'id'            => $detail->getId(),
                'description'   => $detail->getDescription(),
                'horaires'      => $detail->getHoraires(),
                'tarif'         => $detail->getTarif(),
                'accessibilite' => $detail->getAccessibilite(),
                'photos'        => $detail->getPhotos(),
            ],
            $details
        );

        return new JsonResponse(['data' => $donnees]);
    }
}
