<?php

namespace App\Entity;

use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\Get;
use ApiPlatform\Metadata\GetCollection;
use ApiPlatform\Metadata\Post;
use ApiPlatform\Metadata\Delete;
use App\Repository\CommentaireRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: CommentaireRepository::class)]
#[ApiResource(
    operations: [
        new GetCollection(normalizationContext: ['groups' => ['commentaire:read']]),
        new Get(normalizationContext: ['groups' => ['commentaire:read']]),
        new Post(
            normalizationContext:   ['groups' => ['commentaire:read']],
            denormalizationContext: ['groups' => ['commentaire:write']]
        ),
        new Delete(),
    ]
)]
class Commentaire
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    #[Groups(['commentaire:read'])]
    private ?int $id = null;

    #[ORM\Column]
    #[Groups(['commentaire:read', 'commentaire:write', 'visite:read'])]
    private ?int $note = null;

    #[ORM\Column(length: 255, nullable: true)]
    #[Groups(['commentaire:read', 'commentaire:write', 'visite:read'])]
    private ?string $message = null;


    /**
     * @var Collection<int, Visite>
     */
    #[ORM\OneToMany(targetEntity: Visite::class, mappedBy: 'commentaires')]
    private Collection $visite;

    #[ORM\ManyToOne(inversedBy: 'listeCommentaires')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Lieu $lieu = null;

    #[ORM\ManyToOne(inversedBy: 'listeCommentaires')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Utilisateur $utilisateur = null;

    public function __construct()
    {
        $this->lieu = new ArrayCollection();
        $this->utilisateur = new ArrayCollection();
        $this->visite = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNote(): ?int
    {
        return $this->note;
    }

    public function setNote(int $note): static
    {
        $this->note = $note;

        return $this;
    }

    public function getMessage(): ?string
    {
        return $this->message;
    }

    public function setMessage(?string $message): static
    {
        $this->message = $message;

        return $this;
    }

    /**
     * @return Collection<int, Visite>
     */
    public function getVisite(): Collection
    {
        return $this->visite;
    }

    public function addVisite(Visite $visite): static
    {
        if (!$this->visite->contains($visite)) {
            $this->visite->add($visite);
            $visite->setCommentaires($this);
        }

        return $this;
    }

    public function removeVisite(Visite $visite): static
    {
        if ($this->visite->removeElement($visite)) {
            // set the owning side to null (unless already changed)
            if ($visite->getCommentaires() === $this) {
                $visite->setCommentaires(null);
            }
        }

        return $this;
    }

    public function getLieu(): ?Lieu
    {
        return $this->lieu;
    }

    public function setLieu(?Lieu $lieu): static
    {
        $this->lieu = $lieu;

        return $this;
    }

    public function getUtilisateur(): ?Utilisateur
    {
        return $this->utilisateur;
    }

    public function setUtilisateur(?Utilisateur $utilisateur): static
    {
        $this->utilisateur = $utilisateur;

        return $this;
    }
}
