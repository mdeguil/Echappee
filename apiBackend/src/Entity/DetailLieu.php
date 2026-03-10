<?php

namespace App\Entity;

use App\Repository\DetailLieuRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: DetailLieuRepository::class)]
class DetailLieu
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $description = null;

    #[ORM\Column(length: 255)]
    private ?string $horaires = null;

    #[ORM\Column]
    private ?int $tarif = null;

    #[ORM\Column(length: 255)]
    private ?string $accessibilite = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $photos = null;

    #[ORM\OneToOne(mappedBy: 'detail', cascade: ['persist', 'remove'])]
    private ?Lieu $lieu = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(?string $description): static
    {
        $this->description = $description;

        return $this;
    }

    public function getHoraires(): ?string
    {
        return $this->horaires;
    }

    public function setHoraires(string $horaires): static
    {
        $this->horaires = $horaires;

        return $this;
    }

    public function getTarif(): ?int
    {
        return $this->tarif;
    }

    public function setTarif(int $tarif): static
    {
        $this->tarif = $tarif;

        return $this;
    }

    public function getAccessibilite(): ?string
    {
        return $this->accessibilite;
    }

    public function setAccessibilite(string $accessibilite): static
    {
        $this->accessibilite = $accessibilite;

        return $this;
    }

    public function getPhotos(): ?string
    {
        return $this->photos;
    }

    public function setPhotos(?string $photos): static
    {
        $this->photos = $photos;

        return $this;
    }

    public function getLieu(): ?Lieu
    {
        return $this->lieu;
    }

    public function setLieu(Lieu $lieu): static
    {
        // set the owning side of the relation if necessary
        if ($lieu->getDetail() !== $this) {
            $lieu->setDetail($this);
        }

        $this->lieu = $lieu;

        return $this;
    }
}
