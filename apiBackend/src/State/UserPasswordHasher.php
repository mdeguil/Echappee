<?php

namespace App\State;

use ApiPlatform\Metadata\Operation;
use ApiPlatform\State\ProcessorInterface;
use App\Entity\Utilisateur;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

class UserPasswordHasher implements ProcessorInterface
{
    public function __construct(
        #[Autowire(service: 'api_platform.doctrine.orm.state.persist_processor')]
        private ProcessorInterface $persistProcessor,
        private UserPasswordHasherInterface $passwordHasher
    ) {}

    public function process(mixed $data, Operation $operation, array $uriVariables = [], array $context = []): mixed
    {
        if ($data instanceof Utilisateur && $data->getPassword()) {
            // On hache le mot de passe
            $data->setPassword(
                $this->passwordHasher->hashPassword($data, $data->getPassword())
            );
        }

        // On passe la main au processeur de Doctrine pour enregistrer en base
        return $this->persistProcessor->process($data, $operation, $uriVariables, $context);
    }
}
