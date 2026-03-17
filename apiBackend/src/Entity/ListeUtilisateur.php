<?php

namespace App\Entity;

use App\Repository\ListeUtilisateurRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ListeUtilisateurRepository::class)]
class ListeUtilisateur
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'listeUtilisateurs')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Itiniraire $idItiniraire = null;

    #[ORM\ManyToOne]
    #[ORM\JoinColumn(nullable: false)]
    private ?Utilisateur $idUtilisateur = null;

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

    public function getIdUtilisateur(): ?Utilisateur
    {
        return $this->idUtilisateur;
    }

    public function setIdUtilisateur(?Utilisateur $idUtilisateur): static
    {
        $this->idUtilisateur = $idUtilisateur;

        return $this;
    }
}
