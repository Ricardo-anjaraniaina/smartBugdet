# SmartBudget

Application desktop de **gestion de budget personnel** développée en Java Swing avec MySQL.

---

## Présentation

SmartBudget permet à un utilisateur de gérer ses finances personnelles :
suivi des revenus et dépenses, budgets mensuels par catégorie, objectifs d'épargne,
statistiques et graphiques.

---

## Fonctionnalités

- **Authentification** sécurisée (mot de passe hashé SHA-256) avec création de compte
- **Dashboard** : solde, revenus, dépenses, épargne, alertes automatiques
- **Transactions** : ajout, modification, suppression, consultation
- **Recherche** multicritère (description, catégorie, type, montant)
- **Filtres** : type, catégorie, période (date début / date fin)
- **Budgets mensuels** par catégorie avec barres de progression
- **Alertes** automatiques (budget à 70%, 90%, dépassé)
- **Objectifs d'épargne** avec suivi de progression et date limite
- **Statistiques** : taux d'épargne, dépense moyenne, top catégorie, etc.
- **Graphiques** Java2D : camembert par catégorie, barres mensuelles
- **Comparaison mensuelle** : évolution revenus / dépenses / épargne entre deux mois

---

## Technologies

| Technologie       | Rôle                              |
|-------------------|-----------------------------------|
| Java 17+          | Langage principal                 |
| Java Swing        | Interface graphique               |
| MySQL 8.0+        | Base de données                   |
| JDBC              | Communication Java ↔ MySQL        |
| MySQL Connector/J | Driver JDBC                       |
| Java2D            | Graphiques (sans dépendance tierce)|
| Architecture MVC  | Séparation des responsabilités    |
| Pattern DAO       | Accès aux données                 |

---

## Prérequis

- **Java JDK 17** ou supérieur
  → https://adoptium.net/
- **MySQL Server 8.0** ou supérieur
  → https://dev.mysql.com/downloads/mysql/
- **MySQL Connector/J** (driver JDBC)
  → https://dev.mysql.com/downloads/connector/j/

---

## Installation

### 1. Installer Java

Téléchargez et installez Java JDK 17+ depuis https://adoptium.net/

Vérifiez l'installation :
```
java -version
```

### 2. Installer MySQL

Téléchargez et installez MySQL Server depuis https://dev.mysql.com/downloads/mysql/

Démarrez le service MySQL.

### 3. Ajouter le driver JDBC

Téléchargez MySQL Connector/J depuis https://dev.mysql.com/downloads/connector/j/

Placez le fichier `.jar` dans le dossier `lib/` et renommez-le :
```
lib/mysql-connector-j.jar
```

### 4. Configurer la connexion MySQL

Copiez le fichier exemple :
```
config/database.properties.example  →  config/database.properties
```

Modifiez `config/database.properties` selon votre installation :
```properties
db.host=localhost
db.port=3306
db.name=smartbudget
db.username=root
db.password=
```

> Si votre MySQL utilise un mot de passe, renseignez-le dans `db.password`.

---

## Import de la base de données

### Option A — Import manuel (recommandé)

```bash
mysql -u root -p < database/smartbudget.sql
```

Ou via MySQL Workbench :
1. Ouvrez MySQL Workbench
2. Connectez-vous à votre serveur
3. Menu : `Server` → `Data Import`
4. Sélectionnez `database/smartbudget.sql`
5. Cliquez sur `Start Import`

### Option B — Initialisation automatique

Si vous ne faites pas l'import manuel, l'application créera automatiquement
la base et les tables au premier démarrage (si MySQL est démarré et configuré).

---

## Lancement

Double-cliquez sur `run.bat` ou exécutez dans un terminal :

```
run.bat
```

Le script vérifie automatiquement :
- la présence de Java
- la présence du driver JDBC
- la configuration MySQL
- compile le projet
- lance l'application

---

## Identifiants de démonstration

```
Nom d'utilisateur : admin
Mot de passe      : admin123
```

Des données de démonstration sont incluses (transactions, budgets, objectifs d'épargne)
pour permettre de tester toutes les fonctionnalités immédiatement.

Vous pouvez également créer un nouveau compte via le bouton **"Créer un compte"** sur l'écran de connexion.

---

## Structure du projet

```
SmartBudget/
├── src/
│   ├── Main.java                   — Point d'entrée
│   ├── model/                      — Entités métier (User, Transaction, Budget…)
│   ├── view/                       — Interface Swing (Login, Dashboard, Transactions…)
│   ├── controller/                 — Logique métier (Login, Transaction, Budget…)
│   ├── database/                   — JDBC / DAO (Connection, Initializer, *DAO)
│   └── utils/                      — Utilitaires (Password, Validation, Format, Session)
├── lib/
│   └── mysql-connector-j.jar       — Driver JDBC (à ajouter)
├── database/
│   └── smartbudget.sql             — Script SQL complet
├── config/
│   ├── database.properties         — Configuration MySQL (à créer)
│   └── database.properties.example — Modèle de configuration
├── README.md
└── run.bat                         — Script de lancement Windows
```

---

## Problèmes fréquents

### MySQL n'est pas démarré

**Symptôme :** `Communications link failure` ou `Connection refused`

**Solution :**
- Windows : `services.msc` → démarrer le service `MySQL80`
- Ou : `net start MySQL80` dans un terminal administrateur

---

### Mauvais mot de passe MySQL

**Symptôme :** `Access denied for user 'root'@'localhost'`

**Solution :** Modifiez `db.password` dans `config/database.properties`

---

### Port 3306 occupé

**Symptôme :** `Connection refused` sur le port 3306

**Solution :** Vérifiez que MySQL utilise bien le port 3306 :
```sql
SHOW VARIABLES LIKE 'port';
```
Adaptez `db.port` dans `config/database.properties` si nécessaire.

---

### Driver JDBC absent

**Symptôme :** `[ERREUR] Driver MySQL introuvable`

**Solution :** Placez `mysql-connector-j.jar` dans le dossier `lib/`

---

### La base n'existe pas

**Symptôme :** `Unknown database 'smartbudget'`

**Solution :**
1. Importez `database/smartbudget.sql`
2. Ou laissez l'application la créer automatiquement (si les droits MySQL le permettent)

---

### Colonne `initial_balance` manquante

**Symptôme :** `Unknown column 'initial_balance' in 'field list'`

**Cause :** La base a été créée avec une ancienne version du script SQL.

**Solution :** Exécutez cette requête sur votre base :
```sql
ALTER TABLE users ADD COLUMN initial_balance DECIMAL(15,2) DEFAULT 0.00;
```
Ou réimportez `database/smartbudget.sql` depuis zéro.

---

### Erreur de compilation

**Symptôme :** `error: source release 17 requires target release 17`

**Solution :** Vérifiez votre version Java :
```
java -version
javac -version
```
Installez Java JDK 17+ si nécessaire.

---

## Architecture MVC

```
Vue (view/)          → Affichage Swing, événements utilisateur
    ↓ appelle
Contrôleur (controller/) → Logique métier, validation
    ↓ appelle
DAO (database/)      → Requêtes SQL via JDBC
    ↓ accède
MySQL                → Données persistantes
```

Les modèles (`model/`) sont des objets Java simples (POJO) transportant les données
entre les couches.
