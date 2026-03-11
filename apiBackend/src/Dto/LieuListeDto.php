<?php

namespace App\Dto;

final class LieuListeDto
{
    public function __construct(
        public readonly int     $id,
        public readonly string  $nom,
        public readonly ?string $photo,
        public readonly ?int    $noteMoyen,
        public readonly ?float  $latitude,
        public readonly ?float  $longitude,
        public readonly ?string $categorie,
        public readonly ?string $commentaire,
    ) {}
}
