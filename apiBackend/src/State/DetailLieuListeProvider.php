<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Repository\DetailLieuRepository;
use App\Entity\DetailLieu;

final class DetailLieuListeProvider implements ProviderInterface
{
    public function __construct(
        private readonly DetailLieuRepository $depotDetailLieu,
    ) {}

    public function provide(Operation $operation, array $uriVariables = [], array $context = []): object
    {
        // Récupère l'ID depuis les variables d'URI
        $id = $uriVariables['id'] ?? null;

        if (!$id) {
            throw new \RuntimeException('ID manquant pour récupérer le détail du lieu.');
        }

        // Récupère le DetailLieu depuis le repository
        $detailLieu = $this->depotDetailLieu->find($id);

        if (!$detailLieu) {
            throw new \Symfony\Component\HttpKernel\Exception\NotFoundHttpException(
                sprintf('Aucun DetailLieu trouvé avec l\'ID %d.', $id)
            );
        }

        // Retourne l'objet DetailLieu (API Platform se charge de la sérialisation)
        return $detailLieu;
    }
}