<?php

namespace App\State\Provider;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Repository\DetailLieuRepository;

final readonly class DetailLieuListeProvider implements ProviderInterface
{
    public function __construct(
        private DetailLieuRepository $depotDetailLieu,
    ) {}

    public function provide(Operation $operation, array $uriVariables = [], array $context = []): object
    {
        $id = $uriVariables['id'] ?? null;

        if (!$id) {
            throw new \RuntimeException('ID manquant pour récupérer le détail du lieu.');
        }

        $detailLieu = $this->depotDetailLieu->find($id);

        if (!$detailLieu) {
            throw new \Symfony\Component\HttpKernel\Exception\NotFoundHttpException(
                sprintf('Aucun DetailLieu trouvé avec l\'ID %d.', $id)
            );
        }

        return $detailLieu;
    }
}
