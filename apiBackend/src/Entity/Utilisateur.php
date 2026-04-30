<?php

namespace App\Entity;

use ApiPlatform\Metadata\Post;
use App\Repository\UtilisateurRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use ApiPlatform\Metadata\ApiResource;

#[ApiResource(
    operations: [
        new Post(processor: \App\State\UserPasswordHasher::class),
    ],
    formats: ['json', 'jsonld']
)]
#[ORM\Entity(repositoryClass: UtilisateurRepository::class)]
#[ORM\UniqueConstraint(name: 'UNIQ_IDENTIFIER_EMAIL', fields: ['email'])]
class Utilisateur implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 180)]
    private ?string $email = null;

    /**
     * @var list<string> The user roles
     */
    #[ORM\Column]
    private array $roles = ["ROLE_USER"];

    /**
     * @var string The hashed password
     */
    #[ORM\Column]
    private ?string $password = null;

    /**
     * @var Collection<int, Itiniraire>
     */
    #[ORM\OneToMany(targetEntity: Itiniraire::class, mappedBy: 'utilisateur')]
    private Collection $listeItiniraire;

    /**
     * @var Collection<int, Commentaire>
     */
    #[ORM\OneToMany(targetEntity: Commentaire::class, mappedBy: 'utilisateur')]
    private Collection $listeCommentaires;

    public function __construct()
    {
        $this->listeItiniraire = new ArrayCollection();
        $this->listeCommentaires = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): static
    {
        $this->email = $email;

        return $this;
    }

    /**
     * A visual identifier that represents this user.
     *
     * @see UserInterface
     */
    public function getUserIdentifier(): string
    {
        return (string) $this->email;
    }

    /**
     * @see UserInterface
     */
    public function getRoles(): array
    {
        $roles = $this->roles;
        // guarantee every user at least has ROLE_USER
        $roles[] = 'ROLE_USER';

        return array_unique($roles);
    }

    /**
     * @param list<string> $roles
     */
    public function setRoles(array $roles): static
    {
        $this->roles = $roles;

        return $this;
    }

    /**
     * @see PasswordAuthenticatedUserInterface
     */
    public function getPassword(): ?string
    {
        return $this->password;
    }

    public function setPassword(string $password): static
    {
        $this->password = $password;

        return $this;
    }

    /**
     * Ensure the session doesn't contain actual password hashes by CRC32C-hashing them, as supported since Symfony 7.3.
     */
    public function __serialize(): array
    {
        $data = (array) $this;
        $data["\0".self::class."\0password"] = hash('crc32c', $this->password);

        return $data;
    }

    #[\Deprecated]
    public function eraseCredentials(): void
    {
        // @deprecated, to be removed when upgrading to Symfony 8
    }

    /**
     * @return Collection<int, Itiniraire>
     */
    public function getListeItiniraire(): Collection
    {
        return $this->listeItiniraire;
    }

    public function addListeItiniraire(Itiniraire $listeItiniraire): static
    {
        if (!$this->listeItiniraire->contains($listeItiniraire)) {
            $this->listeItiniraire->add($listeItiniraire);
            $listeItiniraire->setUtilisateur($this);
        }

        return $this;
    }

    public function removeListeItiniraire(Itiniraire $listeItiniraire): static
    {
        if ($this->listeItiniraire->removeElement($listeItiniraire)) {
            // set the owning side to null (unless already changed)
            if ($listeItiniraire->getUtilisateur() === $this) {
                $listeItiniraire->setUtilisateur(null);
            }
        }

        return $this;
    }

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
            $listeCommentaire->setUtilisateur($this);
        }

        return $this;
    }

    public function removeListeCommentaire(Commentaire $listeCommentaire): static
    {
        if ($this->listeCommentaires->removeElement($listeCommentaire)) {
            // set the owning side to null (unless already changed)
            if ($listeCommentaire->getUtilisateur() === $this) {
                $listeCommentaire->setUtilisateur(null);
            }
        }

        return $this;
    }
}
