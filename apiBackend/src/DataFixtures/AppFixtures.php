<?php

namespace App\DataFixtures;

use App\Entity\Utilisateur;
use Doctrine\Bundle\FixturesBundle\Fixture;
use Doctrine\Persistence\ObjectManager;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

class AppFixtures extends Fixture
{
    private UserPasswordHasherInterface $hasher;

    // On injecte le service de hachage via le constructeur
    public function __construct(UserPasswordHasherInterface $hasher)
    {
        $this->hasher = $hasher;
    }

    public function load(ObjectManager $manager): void
    {
        $user = new Utilisateur();
        $user->setEmail("test@gmail.com");
        $user->setRoles(['ROLE_USER']);

        // Hachage du mot de passe avant enregistrement
        $hashedPassword = $this->hasher->hashPassword($user, 'mdptest');
        $user->setPassword($hashedPassword);

        $manager->persist($user);
        $manager->flush();
    }
}
