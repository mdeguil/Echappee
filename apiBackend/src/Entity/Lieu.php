<?php

namespace App\Entity;

use ApiPlatform\Metadata\ApiResource;
use ApiPlatform\Metadata\GetCollection;
use App\Dto\LieuListeDto;
use App\Repository\LieuRepository;
use App\State\LieuListeProvider;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: LieuRepository::class)]
#[ApiResource(
    operations: [
        new GetCollection(
            output:   LieuListeDto::class,
            provider: LieuListeProvider::class,
        ),
    ],
    paginationItemsPerPage: 20,
)]
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

    #[ORM\Column(nullable: true)]
    private ?float $latitude = null;

    #[ORM\Column(nullable: true)]
    private ?float $longitude = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $categorie = null;

    // ── Relations ──────────────────────────────────────────────────────────

    #[ORM\ManyToOne(inversedBy: 'lieu')]
    private ?ListeLieu $listeLieu = null;

    #[ORM\OneToOne(inversedBy: 'lieu', cascade: ['persist', 'remove'])]
    #[ORM\JoinColumn(nullable: false)]
    private ?DetailLieu $detail = null;

    #[ORM\ManyToOne(inversedBy: 'lieu')]
    private ?Commentaire $commentaires = null;

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

    public function getCategorie(): ?string { return $this->categorie; }
    public function setCategorie(?string $categorie): static { $this->categorie = $categorie; return $this; }

    public function getListeLieu(): ?ListeLieu { return $this->listeLieu; }
    public function setListeLieu(?ListeLieu $listeLieu): static { $this->listeLieu = $listeLieu; return $this; }

    public function getDetail(): ?DetailLieu { return $this->detail; }
    public function setDetail(DetailLieu $detail): static { $this->detail = $detail; return $this; }

    public function getCommentaires(): ?Commentaire { return $this->commentaires; }
    public function setCommentaires(?Commentaire $commentaires): static { $this->commentaires = $commentaires; return $this; }
}
