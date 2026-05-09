<?php

namespace App\Entity;

use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\ApiFilter;
use ApiPlatform\Doctrine\Orm\Filter\SearchFilter;
use ApiPlatform\Metadata\Get;
use ApiPlatform\Metadata\GetCollection;
use ApiPlatform\Metadata\Post;
use ApiPlatform\Metadata\Delete;
use App\Repository\VisiteRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: VisiteRepository::class)]
#[ApiResource(
    operations: [
        new GetCollection(normalizationContext: ['groups' => ['visite:read']]),
        new Get(normalizationContext: ['groups' => ['visite:read']]),
        new Post(
            normalizationContext:   ['groups' => ['visite:read']],
            denormalizationContext: ['groups' => ['visite:write']]
        ),
        new Delete(),
    ]
)]
// Filtre via la relation existante Visite → Commentaire → Utilisateur
// Aucune nouvelle colonne : le schéma ne change pas
#[ApiFilter(SearchFilter::class, properties: ['commentaires.utilisateur.id' => 'exact'])]
class Visite
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    #[Groups(['visite:read'])]
    private ?int $id = null;

    #[ORM\Column(type: Types::DATE_MUTABLE)]
    #[Groups(['visite:read', 'visite:write'])]
    private ?\DateTime $date = null;

    /**
     * cascade: ['remove'] → quand une Visite est supprimée via l'ORM (DELETE /api/visites/{id}),
     * Doctrine supprime automatiquement le Commentaire associé.
     * onDelete: 'CASCADE' → filet de sécurité au niveau SQL.
     * Aucune migration nécessaire : on modifie seulement le comportement ORM, pas la structure de la table.
     */
    #[ORM\ManyToOne(inversedBy: 'visite', cascade: ['remove'])]
    #[ORM\JoinColumn(nullable: false, onDelete: 'CASCADE')]
    #[Groups(['visite:read', 'visite:write'])]
    private ?Commentaire $commentaires = null;

    #[ORM\ManyToOne(inversedBy: 'visite')]
    #[Groups(['visite:read'])]
    private ?Partage $partage = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDate(): ?\DateTime
    {
        return $this->date;
    }

    public function setDate(\DateTime $date): static
    {
        $this->date = $date;

        return $this;
    }

    public function getCommentaires(): ?Commentaire
    {
        return $this->commentaires;
    }

    public function setCommentaires(?Commentaire $commentaires): static
    {
        $this->commentaires = $commentaires;

        return $this;
    }

    public function getPartage(): ?Partage
    {
        return $this->partage;
    }

    public function setPartage(?Partage $partage): static
    {
        $this->partage = $partage;

        return $this;
    }
}