# Annotation des Textes

Une application web Spring Boot pour l'annotation collaborative de textes, permettant aux administrateurs de gérer des datasets, d'assigner des tâches d'annotation et aux annotateurs de réaliser des annotations sur des paires de textes.

## 📋 Fonctionnalités

### Pour les Administrateurs
- Gestion des datasets (importation de fichiers CSV/Excel)
- Création et assignation de tâches d'annotation
- Suivi de la progression des tâches
- Extension des délais et réassignation des tâches
- Statistiques avancées sur les annotations
- Détection de spam dans les annotations

### Pour les Annotateurs
- Interface d'annotation intuitive
- Visualisation des tâches assignées
- Suivi de la progression personnelle
- Modification des annotations existantes

## 🔧 Technologies Utilisées

- **Backend**: Java 21, Spring Boot 3.2.3
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Base de données**: MySQL 8
- **Sécurité**: Spring Security
- **Traitement de données**: Apache POI (pour les fichiers Excel/CSV)
- **Autres**: Lombok, Spring Data JPA, Spring Mail

## 🚀 Installation

### Prérequis
- Java 21
- Maven
- MySQL 8

### Configuration

1. Clonez le dépôt :
   ```bash
   git clone [URL_DU_REPO]
   cd annotation-des-textes
   ```

2. Configurez la base de données dans `src/main/resources/application.properties` :
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/annotations_db?createDatabaseIfNotExist=true
   spring.datasource.username=votre_utilisateur
   spring.datasource.password=votre_mot_de_passe
   ```

3. Configurez l'envoi d'emails (optionnel) :
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=votre-email@gmail.com
   spring.mail.password=votre-mot-de-passe-app
   ```

### Compilation et Exécution

```bash
# Compiler le projet
mvn clean install

# Exécuter l'application
mvn spring-boot:run
```

L'application sera accessible à l'adresse : http://localhost:8081

## 📊 Structure du Projet

```
src/main/java/com/otmane/annotation_des_textes/
├── config/           # Configuration Spring et sécurité
├── controllers/      # Contrôleurs REST et MVC
├── entities/         # Modèles de données JPA
├── repositories/     # Interfaces d'accès aux données
├── services/         # Logique métier
└── IA/               # Fonctionnalités d'IA (évaluation, entraînement)
```

## 🔍 Modèle de Données

- **Dataset**: Collection de paires de textes à annoter
- **CoupleText**: Paire de textes à comparer et annoter
- **Tache**: Assignation d'un ensemble de paires à un annotateur avec une date limite
- **Annotation**: Choix de classe effectué par un annotateur sur une paire de textes
- **Utilisateur**: Utilisateur du système (Administrateur ou Annotateur)

## 👥 Rôles Utilisateurs

- **Administrateur**: Gestion des datasets, tâches, et utilisateurs
- **Annotateur**: Réalisation des annotations sur les tâches assignées

## 🛠️ Utilisation

### Import de Datasets

1. Connectez-vous en tant qu'administrateur
2. Accédez à "Datasets" > "Importer un dataset"
3. Téléchargez un fichier CSV/Excel contenant des paires de textes
4. Définissez les classes possibles pour l'annotation

### Création de Tâches

1. Sélectionnez un dataset
2. Choisissez les annotateurs à assigner
3. Définissez une date limite
4. Sélectionnez les paires de textes à inclure dans la tâche

### Réalisation d'Annotations

1. Connectez-vous en tant qu'annotateur
2. Accédez à "Mes Tâches"
3. Sélectionnez une tâche active
4. Pour chaque paire de textes, choisissez la classe appropriée


