# AniTrack — Suivi d'Animes & Mangas avec Cloud Sync & Premium

AniTrack est une application Android moderne et épurée permettant de cataloguer, noter et suivre l'avancement de ses animes et mangas (En cours, Terminé, En pause, À voir), synchronisée en temps réel via Firebase Firestore et Google Sign-In, enrichie par les données officielles de l'API Jikan (MyAnimeList) et dotée d'une formule Premium avec recommandations personnalisées et expérience sans publicité.

---

## Synthèse des Décisions & Préférences Utilisateur

> [!IMPORTANT]
> Les choix clés confirmés lors de la phase d'échange avec l'utilisateur :

- **Identité & Nom** : *AniTrack* — Esthétique sombre moderne, contrastes néon subtils (bleu cyan / violet profond), typographie ultra-lisible.
- **Organisation du suivi** : 4 statuts complets (*En cours*, *Terminé*, *En pause*, *À voir*) pour les Animes (épisodes vus) et Mangas (chapitres et tomes lus), avec notation sur 10 et notes personnelles.
- **Authentification & Cloud Sync** : Google Sign-In via Android Credential Manager & Firebase Firestore pour synchroniser les données entre appareils avec sécurité zero-trust.
- **Expérience Premium** : Mode sans publicité (suppression de la bannière sponsorisée), recommandations personnalisées intelligentes basées sur les genres et notes des œuvres aimées, badge Membre VIP et statistiques avancées de visionnage.
- **API Données** : Jikan API v4 (`https://api.jikan.moe/v4`) pour la recherche en direct, le top anime/manga, les synopsis complets, images haute définition, genres et studios.
- **Ergonomie Adaptative** : Expérience fluide optimisée sur smartphones (Bottom Navigation Bar, gestes tactiles) et tablettes (Navigation Rail, mise en page adaptative multi-colonnes).

---

## 1. Vue d'Ensemble & Concept

AniTrack résout le problème de dispersion des listes d'animes et de mangas en offrant un hub unique, rapide et élégant. L'utilisateur peut explorer les tendances actuelles du Japon, rechercher instantanément n'importe quel titre, l'ajouter à sa bibliothèque personnelle en un geste et retrouver sa progression sur tous ses appareils grâce au compte Google synchronisé avec Firestore.

### Public Cible
- Passionnés d'animes et lecteurs de mangas souhaitant garder une trace précise de leurs épisodes et chapitres.
- Utilisateurs multi-appareils (téléphone, tablette) recherchant une synchronisation sans friction.
- Utilisateurs souhaitant découvrir de nouvelles pépites grâce aux recommandations personnalisées Premium.

---

## 2. Expérience Utilisateur & Design Visuel

### Parcours Utilisateur Clés
1. **Démarrage & Connexion** :
   - Écran d'accueil invitant à se connecter avec Google en un tap via Credential Manager.
   - Synchronisation automatique et transparente de la bibliothèque cloud Firestore dès la connexion.
2. **Exploration & Découverte** :
   - Onglet Découverte avec bannières des animes/mangas tendances, filtres par type (Anime / Manga), barre de recherche instantanée avec suggestions Jikan API.
   - Fiche détaillée complète : affiche haute définition, note MAL, statut de diffusion, synopsis, genres, nombre d'épisodes/tomes.
3. **Gestion de Ma Bibliothèque** :
   - Onglets rapides : *En cours*, *Terminé*, *En pause*, *À voir*.
   - Incrémentation rapide (+1 épisode / +1 chapitre) directement depuis la liste sans ouvrir le détail.
   - Éditeur modal : statut, épisodes vus / tomes lus, score personnel (1 à 10 étoiles), commentaire personnel.
4. **Espace Premium & Recommandations** :
   - Bannière promotionnelle non intrusive en version gratuite.
   - Écran de souscription Premium avec activation interactive de l'abonnement.
   - Déblocage instantané de l'onglet **Recommandations Personnalisées** (moteur d'affinité analysant les genres favoris de l'utilisateur) et suppression totale des bannières publicitaires.
5. **Profil & Statistiques** :
   - Temps total estimé passé à regarder des animes, nombre de chapitres lus, genres de prédilection, bouton de déconnexion sécurisé.

### Identité Visuelle & Thème (Dark Minimalist)
- **Palette de Couleurs** :
  - Fond immersif : `#0F1117` (Deep Obsidian Dark)
  - Cartes et surfaces : `#1A1D27` (Elevated Charcoal Surface) avec bordure subtile `#2A2F42`
  - Accent primaire : `#6366F1` (Indigo Vibrant) & `#38BDF8` (Cyan Électrique)
  - Accent secondaire / Premium : `#F59E0B` (Gold Amber) pour les badges et fonctionnalités VIP
  - Texte : Blanc cassé `#F8FAFC` et gris secondaire `#94A3B8`
- **Typographie** :
  - Titres et affiches : Sans-serif moderne, gras, géométrique.
  - Corps de texte et synopsis : Typographie fluide avec espacement agréable pour la lecture.
- **Feedback & Micro-interactions** :
  - Effets d'élévation tonale Material 3, animations d'apparition fluides (`AnimatedVisibility`), barres de progression visuelles pour chaque anime/manga.

---

## 3. Décisions Produits & Arbitrages Techniques

### Décision 1 : Authentification & Sécurité Cloud
- **Approche** : Jetpack Credential Manager avec `GetSignInWithGoogleOption` + Firebase Auth & Firestore avec règles de sécurité strictes (`request.auth.uid == userId`).
- **Justification** : Conformité avec les règles de la plateforme AI Studio, garantie que chaque utilisateur n'accède qu'à ses propres données, et synchronisation immédiate sans risque de perte de données.
- **Alternative rejetée** : Authentification anonyme ou email/mot de passe manuel sans validation (interdits ou instables).

### Décision 2 : Gestion de l'API Jikan v4
- **Approche** : Client Retrofit avec Moshi + OkHttp avec intercepteur de gestion des erreurs 429 (Too Many Requests avec backoff) et mise en cache mémoire des résultats récents.
- **Justification** : Jikan est l'API publique ouverte la plus complète pour MyAnimeList mais impose un léger rate-limiting (~3 req/s). La mise en cache et le debouncing de la recherche assurent une navigation ultra fluide sans crash.

### Décision 3 : Moteur de Recommandations Personnalisées (Premium)
- **Approche** : Analyse dynamique des animes notés $\ge 7/10$ ou marqués comme "Terminé" dans la bibliothèque utilisateur pour extraire les genres dominants, puis requête ciblée sur Jikan API pour suggérer des œuvres similaires non encore présentes dans la liste de l'utilisateur.
- **Justification** : Offre une réelle valeur ajoutée au statut Premium sans dépendance externe lourde, tout en respectant les goûts concrets de l'utilisateur.

---

## 4. Architecture Technique & Schéma des Données

### Diagramme d'Architecture Système

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  ┌──────────────────────┬──────────────────────┬─────────────────────┐ │
│  │     AuthScreen       │   DiscoverScreen     │   LibraryScreen     │ │
│  │ (Google Sign-In Btn) │  (Search & Top MAL)  │ (Anime/Manga Tabs)  │ │
│  ├──────────────────────┼──────────────────────┼─────────────────────┤ │
│  │   DetailSheet/View   │    PremiumScreen     │    ProfileScreen    │ │
│  │ (Progression & Note) │ (VIP Recomms & Perks)│ (Stats & Cloud Sync)│ │
│  └──────────────────────┴──────────────────────┴─────────────────────┘ │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / Events
┌───────────────────────────────────▼────────────────────────────────────┐
│                       ViewModels & UI State                            │
│  • AuthViewModel (Session, User State, CredentialManager)              │
│  • LibraryViewModel (CRUD Firestore, Statuts, Incrémentations)         │
│  • DiscoverViewModel (Recherche Jikan, Top Anime/Manga, Filtres)       │
│  • PremiumViewModel (Statut VIP, Moteur de Recommandations, Ads Toggle)│
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
┌───────────────────▼─────────────┐    ┌─────────────▼───────────────────┐
│     FirestoreRepository         │    │       JikanApiRepository        │
│  • observeUserItems(uid)        │    │  • searchAnime(query)           │
│  • saveItem(item)               │    │  • searchManga(query)           │
│  • updateProgress(id, count)    │    │  • getTopAnime() / getTopManga()│
│  • observeUserProfile(uid)      │    │  • getRecommendations(genres)   │
│  • updatePremiumStatus(uid, isP)│    └─────────────────────────────────┘
└───────────────────┬─────────────┘
                    │
┌───────────────────▼────────────────────────────────────────────────────┐
│                   Firebase Firestore Cloud Backend                     │
│  /users/{userId} -> profil utilisateur & flag isPremium               │
│  /users/{userId}/items/{itemId} -> animes et mangas suivis             │
│  (Règles de sécurité: lecture et écriture isolées par userId)          │
└────────────────────────────────────────────────────────────────────────┘
```

### Modèles de Données Firestore

1. **`UserItem` (Sous-collection `/users/{userId}/items/{itemId}`)** :
   - `id`: String (clé unique, ex: `anime_21` ou `manga_11`)
   - `malId`: Long (identifiant MyAnimeList de l'œuvre)
   - `title`: String
   - `imageUrl`: String
   - `type`: String (`"ANIME"` ou `"MANGA"`)
   - `status`: String (`"WATCHING"` / `"READING"`, `"COMPLETED"`, `"ON_HOLD"`, `"PLAN_TO_WATCH"`)
   - `currentProgress`: Long (épisodes vus ou chapitres lus)
   - `totalProgress`: Long (total épisodes ou chapitres connus, 0 si inconnu)
   - `userScore`: Long (note de 0 à 10)
   - `personalNotes`: String
   - `genres`: List<String>
   - `createdAt`: Timestamp
   - `updatedAt`: Timestamp

2. **`UserProfile` (Document `/users/{userId}`)** :
   - `userId`: String
   - `email`: String
   - `displayName`: String
   - `isPremium`: Boolean
   - `premiumExpiresAt`: Timestamp?
   - `createdAt`: Timestamp
   - `updatedAt`: Timestamp

---

## 5. Étapes de Mise en Œuvre Validées

1. **Provisioning Firebase** : Exécution de `set_up_firebase` pour initialiser le backend Firestore et l'authentification Google Sign-In.
2. **Schéma & Règles Firestore** : Création de `firebase-blueprint.json` et de `firestore.rules` avec isolation par `request.auth.uid == userId`, validation des tests locaux JS & Kotlin, puis déploiement avec `deploy_firebase`.
3. **Couche Réseau & API Jikan** : Configuration de Retrofit, Moshi et des DTOs Jikan (`AnimeDto`, `MangaDto`, `ImagesDto`, etc.) avec gestion des erreurs.
4. **Couche Données & Repositories** : Implémentation de `FirestoreRepository` et `JikanRepository`.
5. **Interface Utilisateur Jetpack Compose** :
   - Navigation adaptative (Barre inférieure sur mobile, Rail sur tablette).
   - Écran Auth & Google Sign-In via Credential Manager.
   - Écran Découverte avec recherche debouncée et carrousels tendances.
   - Écran Bibliothèque avec onglets de statuts, incrémentation en 1 tap et filtre Anime/Manga.
   - Écran Détail avec notation étoiles et progression.
   - Écran & Fonctionnalités Premium (recommandations personnalisées débloquées, suppression de publicité).
6. **Icône d'Application Personnalisée** : Configuration d'une icône adaptative dédiée AniTrack.
7. **Compilation & Vérification Finale** : Validation avec `compile_applet`.
