<?php

namespace App\Entity;

use App\Repository\LieuRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: LieuRepository::class)]
class Lieu
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private ?string $nom = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $photo = null;

    #[ORM\Column(nullable: true)]
    private ?int $noteMoyen = null;

    #[ORM\ManyToOne(inversedBy: 'lieu')]
    private ?ListeLieu $listeLieu = null;

    /**
     * @var Collection<int, Categorie>
     */
    #[ORM\OneToMany(targetEntity: Categorie::class, mappedBy: 'lieux')]
    private Collection $categorie;

    #[ORM\OneToOne(inversedBy: 'lieu', cascade: ['persist', 'remove'])]
    #[ORM\JoinColumn(nullable: false)]
    private ?DetailLieu $detail = null;

    #[ORM\ManyToOne(inversedBy: 'lieu')]
    private ?Commentaire $commentaires = null;

    #[ORM\Column]
    private ?int $longitude = null;

    #[ORM\Column]
    private ?int $latitude = null;

    public function __construct()
    {
        $this->categorie = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(string $nom): static
    {
        $this->nom = $nom;

        return $this;
    }

    public function getPhoto(): ?string
    {
        return $this->photo;
    }

    public function setPhoto(?string $photo): static
    {
        $this->photo = $photo;

        return $this;
    }

    public function getNoteMoyen(): ?int
    {
        return $this->noteMoyen;
    }

    public function setNoteMoyen(?int $noteMoyen): static
    {
        $this->noteMoyen = $noteMoyen;

        return $this;
    }

    public function getListeLieu(): ?ListeLieu
    {
        return $this->listeLieu;
    }

    public function setListeLieu(?ListeLieu $listeLieu): static
    {
        $this->listeLieu = $listeLieu;

        return $this;
    }

    /**
     * @return Collection<int, Categorie>
     */
    public function getCategorie(): Collection
    {
        return $this->categorie;
    }

    public function addCategorie(Categorie $categorie): static
    {
        if (!$this->categorie->contains($categorie)) {
            $this->categorie->add($categorie);
            $categorie->setLieux($this);
        }

        return $this;
    }

    public function removeCategorie(Categorie $categorie): static
    {
        if ($this->categorie->removeElement($categorie)) {
            // set the owning side to null (unless already changed)
            if ($categorie->getLieux() === $this) {
                $categorie->setLieux(null);
            }
        }

        return $this;
    }

    public function getDetail(): ?DetailLieu
    {
        return $this->detail;
    }

    public function setDetail(DetailLieu $detail): static
    {
        $this->detail = $detail;

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

    public function getLongitude(): ?int
    {
        return $this->longitude;
    }

    public function setLongitude(int $longitude): static
    {
        $this->longitude = $longitude;

        return $this;
    }

    public function getLatitude(): ?int
    {
        return $this->latitude;
    }

    public function setLatitude(int $latitude): static
    {
        $this->latitude = $latitude;

        return $this;
    }
}
