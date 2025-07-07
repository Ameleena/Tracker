# Habit Tracker

## Описание
Мобильное приложение для отслеживания привычек и финансов с использованием современных Android-технологий.

## Архитектура
- **Clean Architecture**: разделение на domain, data, ui
- **MVVM**: ViewModel, use-case, репозитории
- **Jetpack Compose**: декларативный UI
- **Room**: локальная база данных
- **Retrofit**: сетевые запросы
- **Корутины и Flow**: асинхронность и реактивность
- **Hilt**: Dependency Injection
- **SharedPreferences**: для хранения настроек
- **LiveData/StateFlow**: для реактивного UI
- **MVVM (Model-View-ViewModel)**
- **UseCase (бизнес-логика вынесена в отдельные классы)**
- **Repository (работа с данными через абстракции)**
- **Dependency Injection (Koin)**
- **Асинхронность: Kotlin Coroutines, Flow, StateFlow, launch, async, withContext**
- **Рефакторинг: повторяющиеся элементы вынесены в отдельные компоненты, код структурирован**

## Структура проекта
- `domain/` — бизнес-логика, use-cases, интерфейсы репозиториев
- `data/` — реализации репозиториев, Room, Retrofit
- `ui/` — экраны, компоненты, темы, ViewModel

## Сборка и запуск
```sh
./gradlew build
./gradlew installDebug
```

## Тестирование
- Unit-тесты: `./gradlew test`
- UI-тесты: `./gradlew connectedAndroidTest`

## CI/CD
- GitHub Actions: автоматическая сборка, тесты и lint при каждом push/pull request в ветки `main` и `release`.

## Технологии
- Kotlin, Jetpack Compose, Room, Retrofit, Hilt, Coroutines, Flow, JUnit, Mockito

## Лицензия
MIT 