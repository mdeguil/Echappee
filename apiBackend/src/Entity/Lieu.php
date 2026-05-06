<?php

namespace App\Entity;

use ApiPlatform\Doctrine\Orm\Filter\SearchFilter;
use ApiPlatform\Metadata\ApiFilter;
use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\GetCollection;
use App\Dto\LieuListe;
use App\Repository\LieuRepository;
use App\State\Provider\LieuListeProvider;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Serializer\Annotation\Groups;

#[ORM\Entity(repositoryClass: LieuRepository::class)]
#[ApiResource(
    operations: [
        new GetCollection(
            output:   LieuListe::class,
            provider: LieuListeProvider::class,
        ),
    ],
    paginationItemsPerPage: 20,
)]
#[ApiFilter(SearchFilter::class, properties: [
    'categorie.nom' => 'exact',
    'nom'           => 'partial',
])]
class Lieu
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    #[Groups(['lieu:read', 'commentaire:read', 'visite:read'])]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    #[Groups(['lieu:read', 'commentaire:read', 'visite:read'])]
    private ?string $nom = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $photo = null;

    #[ORM\Column(nullable: true)]
    private ?int $noteMoyen = null;

    #[ORM\Column(nullable: true)]
    private ?float $latitude = null;

    #[ORM\Column(nullable: true)]
    private ?float $longitude = null;

    #[ORM\ManyToOne(inversedBy: 'lieux')]
    #[ORM\JoinColumn(nullable: true)]
    private ?Categorie $categorie = null;


    #[ORM\OneToOne(inversedBy: 'lieu', cascade: ['persist', 'remove'])]
    #[ORM\JoinColumn(nullable: false)]
    private ?DetailLieu $detail = null;

    /**
     * @var Collection<int, Commentaire>
     */
    #[ORM\OneToMany(targetEntity: Commentaire::class, mappedBy: 'lieu')]
    private Collection $listeCommentaires;

    public function __construct()
    {
        $this->listeCommentaires = new ArrayCollection();
    }


    public function getId(): ?int { return $this->id; }

    public function getNom(): ?string { return $this->nom; }
    public function setNom(string $nom): static { $this->nom = $nom; return $this; }

    public function getPhoto(): ?string { return $this->photo; }
    public function setPhoto(?string $photo): static { $this->photo = $photo; return $this; }

    public function getNoteMoyen(): ?int { return $this->noteMoyen; }
    public function setNoteMoyen(?int $noteMoyen): static { $this->noteMoyen = $noteMoyen; return $this; }

    public function getLatitude(): ?float { return $this->latitude; }
    public function setLatitude(?float $latitude): static { $this->latitude = $latitude; return $this; }

    public function getLongitude(): ?float { return $this->longitude; }
    public function setLongitude(?float $longitude): static { $this->longitude = $longitude; return $this; }

    public function getCategorie(): ?Categorie { return $this->categorie; }
    public function setCategorie(?Categorie $categorie): static { $this->categorie = $categorie; return $this; }

    public function getDetail(): ?DetailLieu { return $this->detail; }
    public function setDetail(DetailLieu $detail): static { $this->detail = $detail; return $this; }

    /**
     * @return Collection<int, Commentaire>
     */
    public function getListeCommentaires(): Collection
    {
        return $this->listeCommentaires;
    }

    public function addListeCommentaire(Commentaire $listeCommentaire): static
    {
        if (!$this->listeCommentaires->contains($listeCommentaire)) {
            $this->listeCommentaires->add($listeCommentaire);
            $listeCommentaire->setLieu($this);
        }

        return $this;
    }

    public function removeListeCommentaire(Commentaire $listeCommentaire): static
    {
        if ($this->listeCommentaires->removeElement($listeCommentaire)) {
            if ($listeCommentaire->getLieu() === $this) {
                $listeCommentaire->setLieu(null);
            }
        }

        return $this;
    }

}
