<?php

namespace App\Command;

use App\Service\ImportationLieuxService;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

// lance l'importation
// php bin/console app:importer-lieux  | php bin/console app:importer-lieux -vvv

#[AsCommand(
    name: 'app:importer-lieux',
    description: 'Importe les lieux touristiques de la Charente depuis l\'API Data16',
)]
class ImportationLieuxCommand extends Command
{
    public function __construct(
        private readonly ImportationLieuxService $importationLieuxService
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $entree, OutputInterface $sortie): int
    {
        $io = new SymfonyStyle($entree, $sortie);

        $io->title('Import des lieux touristiques — Data16 Charente');
        $io->text('Connexion à l\'API en cours...');

        try {
            $bilan = $this->importationLieuxService->importerLieuxCharente();

            $io->success('Import terminé avec succès !');
            $io->table(
                ['', 'Nombre'],
                [
                    ['✅ Lieux créés',       $bilan['creations']],
                    ['🔄 Lieux mis à jour',  $bilan['misesAJour']],
                    ['❌ Erreurs ignorées',   $bilan['erreurs']],
                    ['📊 Total traité',       $bilan['total']],
                ]
            );

            return Command::SUCCESS;

        } catch (\Throwable $erreur) {
            $io->error('Échec de l\'import : ' . $erreur->getMessage());
            return Command::FAILURE;
        }
    }
}
