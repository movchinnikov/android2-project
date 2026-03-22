# ArtFinder - Android Application 🎨

ArtFinder — это современное Android-приложение для поиска произведений искусства из Art Institute of Chicago, отслеживания посещений и получения наград.

## Технологии 🚀
- **Язык**: Kotlin + Coroutines
- **UI**: Jetpack Compose + Material 3
- **Архитектура**: MVVM + Repository
- **Инъекция зависимостей**: Hilt
- **Сеть**: Retrofit + Gson
- **База данных/Облако**: Firebase Auth, Firestore, Storage
- **Карты**: Google Maps Compose

## Настройка Firebase 🛡️
Для работы аутентификации и хранения данных:
1. Создайте проект в [Firebase Console](https://console.firebase.google.com/).
2. Добавьте Android-приложение (Package Name: `com.artfinder`).
3. Скачайте `google-services.json` и поместите его в папку `app/`.
4. **Важно**: Включите **Email/Password** в разделе Authentication -> Sign-in method.
5. Создайте **Cloud Firestore** и **Firebase Storage** (в режиме Test Mode для начала).

## Настройка Google Maps 🗺️
1. Получите API ключ в [Google Cloud Console](https://console.cloud.google.com/).
2. Создайте файл `local.properties` в корне проекта.
3. Добавьте строку: `MAPS_API_KEY=ваш_ключ_здесь`.

## Запуск 📱
- Откройте проект в Android Studio (Ladybug+).
- Убедитесь, что эмулятор использует **API 35**.
- Соберите и запустите приложение.

## Тестирование ✅
План тестирования доступен здесь: [testing_plan_ru.md](file:///Users/movchinnikov/.gemini/antigravity/brain/9d140087-260f-45e4-85c0-c609c56b8468/testing_plan_ru.md)
