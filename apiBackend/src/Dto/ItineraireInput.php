<?php
namespace App\Dto;

class ItineraireInput
{
    public int $dureTotal;

    /**
     * @var int[]
     */
    public array $listeLieux = [];
}
