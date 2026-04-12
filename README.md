# 📱 Application Android + API Symfony --- Guide d'installation

Ce document explique comment installer et exécuter l'application Android
ainsi que l'API Symfony sur un autre ordinateur.\
Il couvre l'installation, la configuration, et le lancement du serveur
API.

------------------------------------------------------------------------

## 🚀 Prérequis

Assure-toi que le PC possède :

### ✔ Pour l'API Symfony

-   PHP 8.1 ou plus\
-   Composer\
-   Symfony CLI\
-   MySQL / MariaDB (si ton projet utilise une base de données)\
-   Extensions PHP nécessaires (pdo_mysql, etc.)

### ✔ Pour l'application Android

-   Android Studio (version récente)
-   SDK et outils Android configurés

------------------------------------------------------------------------

## 📂 1. Installation de l'API Symfony

### 🔽 Cloner le projet

``` bash
git clone https://github.com/TON_REPOSITORY/api-symfony.git
cd api-symfony
```

### 📦 Installer les dépendances

``` bash
composer install
```

### ⚙️ Configurer l'environnement

Créer un fichier `.env.local` :

``` env
DATABASE_URL="mysql://root:@127.0.0.1:3306/nom_de_ta_base"
```

### 🗃️ Créer la base et importer les données

``` bash
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate
```

(Si tu as un fichier fixtures)

``` bash
php bin/console doctrine:fixtures:load
```

------------------------------------------------------------------------

## ▶️ 2. Lancer le serveur Symfony

Utilise **impérativement cette commande** :

``` bash
symfony server:start --allow-http --port=8000
```

Le serveur sera disponible à l'adresse :

👉 http://localhost:8000

Si ton API expose les joueurs via API Platform :

👉 http://localhost:8000/api/joueurs

------------------------------------------------------------------------

## 📱 3. Lancer l'application Android

### 🔽 Importer le projet

1.  Ouvrir **Android Studio**
2.  `File > Open`
3.  Sélectionner le dossier du projet Android

### 🔧 Modifier l'URL de l'API si nécessaire

Dans ton code, tu dois avoir une constante :

``` java
public static final String BASE_URL = "http://192.168.X.X:8000/api/";
```

🔹 Adapter l'adresse IP selon le PC où tourne Symfony :

-   Si l'application tourne sur un **émulateur**, utiliser :

        http://10.0.2.2:8000/api/

-   Si l'application tourne sur un **vrai téléphone**, utiliser l'IP
    locale du PC, par exemple :

        http://192.168.1.42:8000/api/

### ▶️ Lancer l'application

-   Brancher un téléphone Android **ou** utiliser l'émulateur
-   Cliquer sur **Run ▶**

------------------------------------------------------------------------

## 🧪 4. Vérification rapide

Tester si l'API répond :

``` bash
curl http://localhost:8000/api/joueurs
```

Si tu vois une liste JSON → tout fonctionne.

------------------------------------------------------------------------

## 🛠️ 5. Dépannage

### ❌ L'application n'arrive pas à contacter l'API ?

✔ Vérifier le firewall\
✔ Vérifier l'IP utilisée dans Android\
✔ Vérifier que Symfony est bien lancé sur **port 8000**\
✔ Tester l'API depuis un navigateur mobile

### ❌ "Network On Main Thread" ou crash ?

Assure-toi d'utiliser Volley/Retrofit (ce que tu fais déjà).

------------------------------------------------------------------------

## ✔️ Conclusion

Avec ces étapes, n'importe qui peut :

-   installer ton API Symfony\
-   la lancer sur le port 8000\
-   lancer ton application Android\
-   s'y connecter sans problème
