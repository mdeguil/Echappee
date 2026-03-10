<?php

namespace App\Entity;

use App\Repository\ListeLieuRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ListeLieuRepository::class)]
class ListeLieu
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    /**
     * @var Collection<int, Lieu>
     */
    #[ORM\OneToMany(targetEntity: Lieu::class, mappedBy: 'listeLieu')]
    private Collection $lieu;

    /**
     * @var Collection<int, Itiniraire>
     */
    #[ORM\OneToMany(targetEntity: Itiniraire::class, mappedBy: 'listeLieu')]
    private Collection $itineraire;

    public function __construct()
    {
        $this->lieu = new ArrayCollection();
        $this->itineraire = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
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
            $lieu->setListeLieu($this);
        }

        return $this;
    }

    public function removeLieu(Lieu $lieu): static
    {
        if ($this->lieu->removeElement($lieu)) {
            // set the owning side to null (unless already changed)
            if ($lieu->getListeLieu() === $this) {
                $lieu->setListeLieu(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, Itiniraire>
     */
    public function getItineraire(): Collection
    {
        return $this->itineraire;
    }

    public function addItineraire(Itiniraire $itineraire): static
    {
        if (!$this->itineraire->contains($itineraire)) {
            $this->itineraire->add($itineraire);
            $itineraire->setListeLieu($this);
        }

        return $this;
    }

    public function removeItineraire(Itiniraire $itineraire): static
    {
        if ($this->itineraire->removeElement($itineraire)) {
            // set the owning side to null (unless already changed)
            if ($itineraire->getListeLieu() === $this) {
                $itineraire->setListeLieu(null);
            }
        }

        return $this;
    }
}
