<?php

namespace App\Entity;

use App\Repository\ListeUtilisateurRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: ListeUtilisateurRepository::class)]
class ListeUtilisateur
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    /**
     * @var Collection<int, Utilisateur>
     */
    #[ORM\OneToMany(targetEntity: Utilisateur::class, mappedBy: 'listeUtilisateur')]
    private Collection $utilisateur;

    /**
     * @var Collection<int, Itiniraire>
     */
    #[ORM\OneToMany(targetEntity: Itiniraire::class, mappedBy: 'listeUtilisateur')]
    private Collection $itineraire;

    public function __construct()
    {
        $this->utilisateur = new ArrayCollection();
        $this->itineraire = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
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
            $utilisateur->setListeUtilisateur($this);
        }

        return $this;
    }

    public function removeUtilisateur(Utilisateur $utilisateur): static
    {
        if ($this->utilisateur->removeElement($utilisateur)) {
            // set the owning side to null (unless already changed)
            if ($utilisateur->getListeUtilisateur() === $this) {
                $utilisateur->setListeUtilisateur(null);
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
            $itineraire->setListeUtilisateur($this);
        }

        return $this;
    }

    public function removeItineraire(Itiniraire $itineraire): static
    {
        if ($this->itineraire->removeElement($itineraire)) {
            // set the owning side to null (unless already changed)
            if ($itineraire->getListeUtilisateur() === $this) {
                $itineraire->setListeUtilisateur(null);
            }
        }

        return $this;
    }
}
