<?php

namespace App\Entity;

use App\Repository\ListeLieuxRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ListeLieuxRepository::class)]
class ListeLieux
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'listeLieux')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Itiniraire $idItiniraire = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(nullable: false)]
    private ?Lieu $idLieu = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getIdItiniraire(): ?Itiniraire
    {
        return $this->idItiniraire;
    }

    public function setIdItiniraire(?Itiniraire $idItiniraire): static
    {
        $this->idItiniraire = $idItiniraire;

        return $this;
    }

    public function getIdLieu(): ?Lieu
    {
        return $this->idLieu;
    }

    public function setIdLieu(?Lieu $idLieu): static
    {
        $this->idLieu = $idLieu;

        return $this;
    }
}
