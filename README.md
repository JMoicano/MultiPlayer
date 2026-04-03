# MultiPlayer

MultiPlayer is a modular Android app that lets users discover music and play previews across Phone, Wear OS, and Android Auto.

It is designed to demonstrate production-oriented Android engineering: clean architecture, scalable module boundaries, and platform-specific user experiences.

## Why this project stands out

- Multi-platform delivery from a shared modular codebase
- Clear separation between core capabilities and feature modules
- Modern Android stack with maintainability and scalability in mind

## Architecture at a glance

- `app-phone/`: phone entry point and composition
- `app-wear/`: Wear OS entry point and UI adaptations
- `app-auto/`: Android Auto entry point and integrations
- `core/`: reusable building blocks (design system, network, database, player, utilities)
- `feature/`: screen/domain features (e.g., search and player)

## Tech stack

- Kotlin
- Jetpack Compose (Material 3 / Wear Compose)
- MVVM + Hilt
- Coroutines + Flow
- Ktor (network)
- Room (persistence)
- Media3 / ExoPlayer

## Build locally

```bash
./gradlew :app-phone:assembleDebug
```

For other targets:

```bash
./gradlew :app-wear:assembleDebug
./gradlew :app-auto:assembleDebug
```

## Run tests

```bash
./gradlew test
```

## Notes

- Project organized with a modular architecture to improve maintainability and scalability.
- Dependencies centralized in `gradle/libs.versions.toml`.

