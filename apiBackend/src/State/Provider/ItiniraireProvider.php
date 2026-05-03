<?php

namespace App\State\Provider;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProviderInterface;
use App\Repository\ItiniraireRepository;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpKernel\Exception\UnauthorizedHttpException;

final class ItiniraireProvider implements ProviderInterface
{
    public function __construct(
        private readonly ItiniraireRepository $itiniraireRepo,
        private readonly Security             $security,
    ) {}

    public function provide(Operation $operation, array $uriVariables = [], array $context = []): JsonResponse
    {
        // Récupère l'utilisateur connecté via le JWT
        $utilisateur = $this->security->getUser();

        if (!$utilisateur) {
            throw new UnauthorizedHttpException('Bearer', 'Utilisateur non authentifié');
        }

        // Filtre uniquement les itinéraires de l'utilisateur connecté
        $itineraires = $this->itiniraireRepo->findBy(['utilisateur' => $utilisateur]);

        $donnees = array_map(function ($iti) {
            $lieuxDetails = [];
            foreach ($iti->getListeLieux() as $relation) {
                $lieu           = $relation->getIdLieu();
                $lieuxDetails[] = [
                    'id'  => $lieu->getId(),
                    'nom' => $lieu->getNom(),
                    'lat' => $lieu->getLatitude(),
                    'lng' => $lieu->getLongitude(),
                ];
            }

            return [
                'id'        => $iti->getId(),
                'dureTotal' => $iti->getDureTotal(),
                'nbLieux'   => count($lieuxDetails),
                'lieux'     => $lieuxDetails,
            ];
        }, $itineraires);

        return new JsonResponse(['data' => $donnees]);
    }
}
