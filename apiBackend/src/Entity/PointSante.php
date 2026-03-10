<?php

namespace App\Entity;

use App\Repository\PointSanteRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: PointSanteRepository::class)]
class PointSante
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private ?string $date = null;

    #[ORM\Column]
    private ?int $nombrePas = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDate(): ?string
    {
        return $this->date;
    }

    public function setDate(string $date): static
    {
        $this->date = $date;

        return $this;
    }

    public function getNombrePas(): ?int
    {
        return $this->nombrePas;
    }

    public function setNombrePas(int $nombrePas): static
    {
        $this->nombrePas = $nombrePas;

        return $this;
    }
}
