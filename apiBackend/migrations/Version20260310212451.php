<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260310212451 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE categorie (id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(255) NOT NULL, lieux_id INT NOT NULL, INDEX IDX_497DD634A2C806AC (lieux_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE commentaire (id INT AUTO_INCREMENT NOT NULL, note INT NOT NULL, message VARCHAR(255) DEFAULT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE detail_lieu (id INT AUTO_INCREMENT NOT NULL, description VARCHAR(255) DEFAULT NULL, horaires VARCHAR(255) NOT NULL, tarif INT NOT NULL, accessibilite VARCHAR(255) NOT NULL, photos VARCHAR(255) DEFAULT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE itiniraire (id INT AUTO_INCREMENT NOT NULL, dure_total INT NOT NULL, liste_lieu_id INT DEFAULT NULL, liste_utilisateur_id INT DEFAULT NULL, INDEX IDX_3FBE5A6ADB888B9D (liste_lieu_id), INDEX IDX_3FBE5A6AC1401EBA (liste_utilisateur_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE lieu (id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(255) NOT NULL, photo VARCHAR(255) DEFAULT NULL, note_moyen INT DEFAULT NULL, liste_lieu_id INT DEFAULT NULL, detail_id INT NOT NULL, commentaires_id INT DEFAULT NULL, INDEX IDX_2F577D59DB888B9D (liste_lieu_id), UNIQUE INDEX UNIQ_2F577D59D8D003BB (detail_id), INDEX IDX_2F577D5917C4B2B0 (commentaires_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE liste_lieu (id INT AUTO_INCREMENT NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE liste_utilisateur (id INT AUTO_INCREMENT NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE media (id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(255) NOT NULL, lien VARCHAR(255) NOT NULL, partage_id INT DEFAULT NULL, INDEX IDX_6A2CA10CD5CB766D (partage_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE partage (id INT AUTO_INCREMENT NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE point_sante (id INT AUTO_INCREMENT NOT NULL, date VARCHAR(255) NOT NULL, nombre_pas INT NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE utilisateur (id INT AUTO_INCREMENT NOT NULL, mail VARCHAR(255) NOT NULL, mdp VARCHAR(255) NOT NULL, liste_utilisateur_id INT DEFAULT NULL, commentaires_id INT DEFAULT NULL, sante_id INT NOT NULL, INDEX IDX_1D1C63B3C1401EBA (liste_utilisateur_id), INDEX IDX_1D1C63B317C4B2B0 (commentaires_id), UNIQUE INDEX UNIQ_1D1C63B3C1683D7D (sante_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE visite (id INT AUTO_INCREMENT NOT NULL, date DATE NOT NULL, commentaires_id INT NOT NULL, partage_id INT DEFAULT NULL, INDEX IDX_B09C8CBB17C4B2B0 (commentaires_id), INDEX IDX_B09C8CBBD5CB766D (partage_id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL, available_at DATETIME NOT NULL, delivered_at DATETIME DEFAULT NULL, INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('ALTER TABLE categorie ADD CONSTRAINT FK_497DD634A2C806AC FOREIGN KEY (lieux_id) REFERENCES lieu (id)');
        $this->addSql('ALTER TABLE itiniraire ADD CONSTRAINT FK_3FBE5A6ADB888B9D FOREIGN KEY (liste_lieu_id) REFERENCES liste_lieu (id)');
        $this->addSql('ALTER TABLE itiniraire ADD CONSTRAINT FK_3FBE5A6AC1401EBA FOREIGN KEY (liste_utilisateur_id) REFERENCES liste_utilisateur (id)');
        $this->addSql('ALTER TABLE lieu ADD CONSTRAINT FK_2F577D59DB888B9D FOREIGN KEY (liste_lieu_id) REFERENCES liste_lieu (id)');
        $this->addSql('ALTER TABLE lieu ADD CONSTRAINT FK_2F577D59D8D003BB FOREIGN KEY (detail_id) REFERENCES detail_lieu (id)');
        $this->addSql('ALTER TABLE lieu ADD CONSTRAINT FK_2F577D5917C4B2B0 FOREIGN KEY (commentaires_id) REFERENCES commentaire (id)');
        $this->addSql('ALTER TABLE media ADD CONSTRAINT FK_6A2CA10CD5CB766D FOREIGN KEY (partage_id) REFERENCES partage (id)');
        $this->addSql('ALTER TABLE utilisateur ADD CONSTRAINT FK_1D1C63B3C1401EBA FOREIGN KEY (liste_utilisateur_id) REFERENCES liste_utilisateur (id)');
        $this->addSql('ALTER TABLE utilisateur ADD CONSTRAINT FK_1D1C63B317C4B2B0 FOREIGN KEY (commentaires_id) REFERENCES commentaire (id)');
        $this->addSql('ALTER TABLE utilisateur ADD CONSTRAINT FK_1D1C63B3C1683D7D FOREIGN KEY (sante_id) REFERENCES point_sante (id)');
        $this->addSql('ALTER TABLE visite ADD CONSTRAINT FK_B09C8CBB17C4B2B0 FOREIGN KEY (commentaires_id) REFERENCES commentaire (id)');
        $this->addSql('ALTER TABLE visite ADD CONSTRAINT FK_B09C8CBBD5CB766D FOREIGN KEY (partage_id) REFERENCES partage (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE categorie DROP FOREIGN KEY FK_497DD634A2C806AC');
        $this->addSql('ALTER TABLE itiniraire DROP FOREIGN KEY FK_3FBE5A6ADB888B9D');
        $this->addSql('ALTER TABLE itiniraire DROP FOREIGN KEY FK_3FBE5A6AC1401EBA');
        $this->addSql('ALTER TABLE lieu DROP FOREIGN KEY FK_2F577D59DB888B9D');
        $this->addSql('ALTER TABLE lieu DROP FOREIGN KEY FK_2F577D59D8D003BB');
        $this->addSql('ALTER TABLE lieu DROP FOREIGN KEY FK_2F577D5917C4B2B0');
        $this->addSql('ALTER TABLE media DROP FOREIGN KEY FK_6A2CA10CD5CB766D');
        $this->addSql('ALTER TABLE utilisateur DROP FOREIGN KEY FK_1D1C63B3C1401EBA');
        $this->addSql('ALTER TABLE utilisateur DROP FOREIGN KEY FK_1D1C63B317C4B2B0');
        $this->addSql('ALTER TABLE utilisateur DROP FOREIGN KEY FK_1D1C63B3C1683D7D');
        $this->addSql('ALTER TABLE visite DROP FOREIGN KEY FK_B09C8CBB17C4B2B0');
        $this->addSql('ALTER TABLE visite DROP FOREIGN KEY FK_B09C8CBBD5CB766D');
        $this->addSql('DROP TABLE categorie');
        $this->addSql('DROP TABLE commentaire');
        $this->addSql('DROP TABLE detail_lieu');
        $this->addSql('DROP TABLE itiniraire');
        $this->addSql('DROP TABLE lieu');
        $this->addSql('DROP TABLE liste_lieu');
        $this->addSql('DROP TABLE liste_utilisateur');
        $this->addSql('DROP TABLE media');
        $this->addSql('DROP TABLE partage');
        $this->addSql('DROP TABLE point_sante');
        $this->addSql('DROP TABLE utilisateur');
        $this->addSql('DROP TABLE visite');
        $this->addSql('DROP TABLE messenger_messages');
    }
}
