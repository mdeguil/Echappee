<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260310212600 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE categorie ADD CONSTRAINT FK_497DD634A2C806AC FOREIGN KEY (lieux_id) REFERENCES lieu (id)');
        $this->addSql('ALTER TABLE itiniraire ADD CONSTRAINT FK_3FBE5A6ADB888B9D FOREIGN KEY (liste_lieu_id) REFERENCES liste_lieu (id)');
        $this->addSql('ALTER TABLE itiniraire ADD CONSTRAINT FK_3FBE5A6AC1401EBA FOREIGN KEY (liste_utilisateur_id) REFERENCES liste_utilisateur (id)');
        $this->addSql('ALTER TABLE lieu ADD longitude INT NOT NULL, ADD latitude INT NOT NULL');
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
        $this->addSql('ALTER TABLE lieu DROP longitude, DROP latitude');
        $this->addSql('ALTER TABLE media DROP FOREIGN KEY FK_6A2CA10CD5CB766D');
        $this->addSql('ALTER TABLE utilisateur DROP FOREIGN KEY FK_1D1C63B3C1401EBA');
        $this->addSql('ALTER TABLE utilisateur DROP FOREIGN KEY FK_1D1C63B317C4B2B0');
        $this->addSql('ALTER TABLE utilisateur DROP FOREIGN KEY FK_1D1C63B3C1683D7D');
        $this->addSql('ALTER TABLE visite DROP FOREIGN KEY FK_B09C8CBB17C4B2B0');
        $this->addSql('ALTER TABLE visite DROP FOREIGN KEY FK_B09C8CBBD5CB766D');
    }
}
