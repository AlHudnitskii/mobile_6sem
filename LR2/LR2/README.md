# ЛР 2 — Tabata Timer

## Запуск
1. Открыть проект в Android Studio
2. Синхронизировать Gradle (File → Sync Project with Gradle Files)
3. Создать keystore для подписи:
   ```
   keytool -genkey -v -keystore app/my-release-key.jks -alias mykey -keyalg RSA -keysize 2048 -validity 10000
   ```
4. Запустить на устройстве или эмуляторе (minSdk 26)

## Структура
- `data/` — Room DB, DAO, Repository, модели
- `service/` — ForegroundService для работы таймера в фоне
- `ui/main/` — список последовательностей
- `ui/timer/` — экран таймера
- `ui/edit/` — редактор последовательности
- `ui/settings/` — настройки (тема, шрифт, язык, очистка данных)
- `util/` — LocaleHelper, FontScaleContextWrapper

## Диаграммы
Папка `diagrams/` содержит `.puml` файлы. Для рендеринга используйте:
- Плагин PlantUML в Android Studio / IntelliJ
- https://www.plantuml.com/plantuml/uml/
