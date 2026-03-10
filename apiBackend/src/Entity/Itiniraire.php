<?php

namespace App\Entity;

use App\Repository\ItiniraireRepository;
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

    #[ORM\ManyToOne(inversedBy: 'itineraire')]
    private ?ListeLieu $listeLieu = null;

    #[ORM\ManyToOne(inversedBy: 'itineraire')]
    private ?ListeUtilisateur $listeUtilisateur = null;

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

    public function getListeLieu(): ?ListeLieu
    {
        return $this->listeLieu;
    }

    public function setListeLieu(?ListeLieu $listeLieu): static
    {
        $this->listeLieu = $listeLieu;

        return $this;
    }

    public function getListeUtilisateur(): ?ListeUtilisateur
    {
        return $this->listeUtilisateur;
    }

    public function setListeUtilisateur(?ListeUtilisateur $listeUtilisateur): static
    {
        $this->listeUtilisateur = $listeUtilisateur;

        return $this;
    }
}
