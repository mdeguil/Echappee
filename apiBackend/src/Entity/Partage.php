<?php

namespace App\Entity;

use App\Repository\PartageRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PartageRepository::class)]
class Partage
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    /**
     * @var Collection<int, Media>
     */
    #[ORM\OneToMany(targetEntity: Media::class, mappedBy: 'partage')]
    private Collection $media;

    /**
     * @var Collection<int, Visite>
     */
    #[ORM\OneToMany(targetEntity: Visite::class, mappedBy: 'partage')]
    private Collection $visite;

    public function __construct()
    {
        $this->media = new ArrayCollection();
        $this->visite = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    /**
     * @return Collection<int, Media>
     */
    public function getMedia(): Collection
    {
        return $this->media;
    }

    public function addMedium(Media $medium): static
    {
        if (!$this->media->contains($medium)) {
            $this->media->add($medium);
            $medium->setPartage($this);
        }

        return $this;
    }

    public function removeMedium(Media $medium): static
    {
        if ($this->media->removeElement($medium)) {
            // set the owning side to null (unless already changed)
            if ($medium->getPartage() === $this) {
                $medium->setPartage(null);
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
            $visite->setPartage($this);
        }

        return $this;
    }

    public function removeVisite(Visite $visite): static
    {
        if ($this->visite->removeElement($visite)) {
            // set the owning side to null (unless already changed)
            if ($visite->getPartage() === $this) {
                $visite->setPartage(null);
            }
        }

        return $this;
    }
}
