<?php

namespace App\Entity;

use App\Repository\ItiniraireRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ItiniraireRepository::class)]
class Itiniraire
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column]
    private ?int $dureTotal = null;

    /**
     * @var Collection<int, ListeLieux>
     */
    #[ORM\OneToMany(targetEntity: ListeLieux::class, mappedBy: 'idItiniraire')]
    private Collection $listeLieux;

    public function __construct()
    {
        $this->listeLieux = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDureTotal(): ?int
    {
        return $this->dureTotal;
    }

    public function setDureTotal(int $dureTotal): static
    {
        $this->dureTotal = $dureTotal;

        return $this;
    }

    /**
     * @return Collection<int, ListeLieux>
     */
    public function getListeLieux(): Collection
    {
        return $this->listeLieux;
    }

    public function addListeLieux(ListeLieux $listeLieux): static
    {
        if (!$this->listeLieux->contains($listeLieux)) {
            $this->listeLieux->add($listeLieux);
            $listeLieux->setIdItiniraire($this);
        }

        return $this;
    }

    public function removeListeLieux(ListeLieux $listeLieux): static
    {
        if ($this->listeLieux->removeElement($listeLieux)) {
            // set the owning side to null (unless already changed)
            if ($listeLieux->getIdItiniraire() === $this) {
                $listeLieux->setIdItiniraire(null);
            }
        }

        return $this;
    }
}
