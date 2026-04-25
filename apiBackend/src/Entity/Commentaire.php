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
     * @var Collection<int, Lieu>
     */
    #[ORM\OneToMany(targetEntity: Lieu::class, mappedBy: 'commentaires')]
    #[Groups(['commentaire:read', 'visite:read'])]
    private Collection $lieu;

    /**
     * @var Collection<int, Utilisateur>
     */
    #[ORM\OneToMany(targetEntity: Utilisateur::class, mappedBy: 'commentaires')]
    private Collection $utilisateur;

    /**
     * @var Collection<int, Visite>
     */
    #[ORM\OneToMany(targetEntity: Visite::class, mappedBy: 'commentaires')]
    private Collection $visite;

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
     * @return Collection<int, Lieu>
     */
    public function getLieu(): Collection
    {
        return $this->lieu;
    }

    public function addLieu(Lieu $lieu): static
    {
        if (!$this->lieu->contains($lieu)) {
            $this->lieu->add($lieu);
            $lieu->setCommentaires($this);
        }

        return $this;
    }

    public function removeLieu(Lieu $lieu): static
    {
        if ($this->lieu->removeElement($lieu)) {
            // set the owning side to null (unless already changed)
            if ($lieu->getCommentaires() === $this) {
                $lieu->setCommentaires(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, Utilisateur>
     */
    public function getUtilisateur(): Collection
    {
        return $this->utilisateur;
    }

    public function addUtilisateur(Utilisateur $utilisateur): static
    {
        if (!$this->utilisateur->contains($utilisateur)) {
            $this->utilisateur->add($utilisateur);
            $utilisateur->setCommentaires($this);
        }

        return $this;
    }

    public function removeUtilisateur(Utilisateur $utilisateur): static
    {
        if ($this->utilisateur->removeElement($utilisateur)) {
            // set the owning side to null (unless already changed)
            if ($utilisateur->getCommentaires() === $this) {
                $utilisateur->setCommentaires(null);
            }
        }

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
}
