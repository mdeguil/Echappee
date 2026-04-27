# 📱 Guide d'installation : Application Android & API Symfony

Ce document détaille les étapes nécessaires pour installer et exécuter l'application Android 
ainsi que son API Symfony sur un nouveau poste de travail. Il couvre l'installation des 
dépendances, la configuration de l'environnement et le déploiement du serveur.

------------------------------------------------------------------------

## 🚀 Prérequis

Avant de commencer, assurez-vous que les outils suivants sont installés :

### ✔ Pour l'API Symfony

-  PHP (version 8.1 ou supérieure)\
-   Composer\
-   Symfony CLI\
-   MySQL ou MariaDB \

### ✔ Pour l'application Android

-   Android Studio (dernière version stable)
-   SDK et outils Android configurés

------------------------------------------------------------------------

## 📂 1. Installation de l'API Symfony

### 🔽 Cloner le projet

Ouvrez un terminal et exécutez les commandes suivantes :

``` bash
git clone https://github.com/mdeguil/Echappee.git
cd Echappee/apiBackend
```

### 📦 Installer les dépendances

supprimer le composer.lock avant de faire la commande 

``` bash
composer install
```

### ⚙️ Configurer l'environnement

Créez un fichier nommé .env.local à la racine du projet pour y configurer votre base de données :

``` env
DATABASE_URL="mysql://USER:PASSWORD@127.0.0.1:3306/NOM_DE_BASE?serverVersion=8.0.32&charset=utf8mb4"
```

(Remplacez USER, PASSWORD et NOM_DE_BASE par vos identifiants locaux).

### 🗃️ Créer la base et importer les données

Exécutez ces commandes pour structurer la base et importer les données :

``` bash
php bin/console doctrine:database:create
php bin/console make:migration
php bin/console doctrine:migrations:migrate
php bin/console doctrine:fixture:load
php bin/console app:importer-lieux
```

### ⚙️ Générer les Clé JWT

``` bash
php bin/console lexik:jwt:generate-keypair
```

------------------------------------------------------------------------

## ▶️ 2. Lancer le serveur Symfony

Pour permettre à l'application Android de communiquer avec l'API, utilisez impérativement cette commande :

``` bash
php -S 0.0.0.0:8000 -t public
```

L'API sera alors accessible à l'adresse suivante :
👉 http://[VOTRE_ADRESSE_IP]:8000/api

Note : L'adresse IP correspond à celle de votre machine sur le réseau local. 

------------------------------------------------------------------------

## 📱 3. Configuration de l'application Android

### 🔽 Importer le projet

1.  Lancez Android Studio.
2.  Allez dans `File > Open`
3.  Sélectionner le dossier du projet Android

### 🔧 Modifier l'URL de l'API si nécessaire

Ouvrez le fichier `utils/ApiConfig.java` et mettez à jour la variable suivante :

``` java
private static final String URL_PAR_DEFAUT = "http://[VOTRE_ADRESSE_IP]:8000";
```

(Remplacez [VOTRE_ADRESSE_IP] par l'adresse IP réelle de l'ordinateur hébergeant l'API).

### 🔧 Ajouter la clé API pour la météo

Ouvrez le fichier `local.properties` et ajouter la ligne suivante :

``` java
OPENWEATHER_API_KEY=[CLE_API_OPENWEATHERMAP]
```

### ▶️ Exécution

-   Connectez un appareil Android physique ou lancez un émulateur.
-   Cliquez sur le bouton **Run ▶** (Exécuter).

------------------------------------------------------------------------

## 🛠️ 5. Dépannage

### ❌ L'application n'arrive pas à contacter l'API ?

✔ **Pare-feu :** Vérifiez que votre pare-feu autorise les connexions entrantes sur le port 8000.\
✔ **Adresse IP :** Assurez-vous que l'IP saisie dans `ApiConfig.java` est correcte.\
✔ **Statut du serveur :** Confirmez que le serveur Symfony est actif et tourne bien sur le **port 8000**.\
✔ **Test réseau :** Essayez d'accéder à l'URL de l'API via le navigateur du téléphone ou de l'émulateur.

------------------------------------------------------------------------

## ✔️ Conclusion

En suivant cette procédure, vous disposez désormais d'un environnement fonctionnel :

-   L'API Symfony est opérationnelle.\
-   La base de données est initialisée.\
-   L'application Android est configurée pour communiquer avec le backend de manière fluide.
