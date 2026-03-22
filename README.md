# ArtFinder 🏛️

ArtFinder is a modern Android application that allows users to explore the vast collection of the Art Institute of Chicago, track their visits, share photos, and compete for prestige through a gamification system.

## Features ✨

### 1. Art Exploration 🖼️
- Browse thousands of artworks from the Art Institute of Chicago API.
- Search for specific pieces or artists.
- Filter by "On View" status to find works currently in the museum.
- View detailed information including artist, origin, medium, and credit line.

### 2. Interactive Map 🌍
- Locate artworks within the museum's galleries using an integrated Google Map.
- Find galleries with active displays and jump to their specific collections.
- Real-time orientation to help you navigate the museum floors.

### 3. Visit Tracking & Photo Sharing 📸
- Mark artworks as "Visited" to build your personal collection.
- Attach multiple photos to your visits (Camera or Gallery).
- **Social Gallery**: See photos from other community members on each artwork's page.
- Persistent storage: Photos remain even if you un-mark a visit.

### 4. Gamification & Leaderboard 🏆
- Earn points for every artwork you visit and document with photos.
- Unlock achievement badges: **Explorer**, **Curator**, and **Archivist**.
- **Global Leaderboard**: Compete with other users for the top spots. Features intelligent tied-rank logic (e.g., sharing a 7-10 position).

### 5. Personal Profile 👤
- Manage your identity and see your accumulated points and badges.
- Secure authentication via Firebase.
- Customizable theme (Light/Dark/System).

## Tech Stack 🛠️

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Modern, declarative UI)
- **Architecture**: Clean Architecture with MVVM & MVI patterns
- **Dependency Injection**: Hilt (Dagger)
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **Networking**: Retrofit & OkHttp
- **Database & Auth**: Firebase Firestore & Firebase Auth
- **Storage**: Firebase Storage (for user photos)
- **Image Loading**: Coil (with crossfade optimizations)
- **Maps**: Google Maps SDK for Android

## Setup Instructions 🚀

1. Clone the repository.
2. Add your `local.properties` with a valid Google Maps API Key:
   ```properties
   MAPS_API_KEY=your_key_here
   ```
3. Connect your Firebase project and add the `google-services.json` file to the `app/` directory.
4. Build and run the project using Android Studio.

## Architecture & Performance ⚡

The project follows a modular structure focused on performance:
- **Lazy Rendering**: Complex screens use `LazyColumn` for efficient list recycling.
- **Resource Management**: GPU-intensive components (like Maps) are intelligently suspended during modal interactions to ensure high FPS.
- **Background Processing**: All I/O and heavy calculations are offloaded to `Dispatchers.IO` and `Dispatchers.Default`.

## License 📄
This project was developed as part of the Android 2 course at Humber College.
