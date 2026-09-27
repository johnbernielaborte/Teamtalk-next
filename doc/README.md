# TeamTalk Next — Документация системы плагинов / Plugin System Documentation

Добро пожаловать в официальную документацию системы плагинов **TeamTalk Next** (версия 5.28.6).

Выберите язык документации / Choose documentation language:

---

## 🇷🇺 Документация на русском языке (`doc/ru/`)
* [**Главная страница документации**](ru/README.md)
* [**01. Архитектура и принципы работы**](ru/01_architecture.md) — загрузка через `DexClassLoader`, жизненный цикл, изоляция ошибок.
* [**02. Быстрый старт**](ru/02_quickstart.md) — создание первого плагина, манифест `plugin.json`.
* [**03. Справочник API**](ru/03_api_reference.md) — детальное описание `PluginContext`, `PluginEventListener`, `PluginCommandSender`.
* [**04. Слэш-команды и интерфейс**](ru/04_commands_and_ui.md) — регистрация команд в чате (`/ping`, `/roll`) и кнопок в UI (`PluginAction`).
* [**05. Сборка и упаковка**](ru/05_building_and_packaging.md) — компиляция Java-классов в `.dex` с помощью `d8` и упаковка в `.jar`.
* [**06. Установка и отладка**](ru/06_installation_and_debug.md) — установка через менеджер плагинов, просмотр логов.

---

## 🇬🇧 Documentation in English (`doc/en/`)
* [**Main Plugin Documentation Index**](en/README.md)
* [**01. Architecture & Core Concepts**](en/01_architecture.md) — `DexClassLoader` loading, lifecycle, crash isolation.
* [**02. Quick Start Guide**](en/02_quickstart.md) — creating your first plugin and `plugin.json` manifest.
* [**03. API Reference**](en/03_api_reference.md) — detailed reference for `PluginContext`, `PluginEventListener`, `PluginCommandSender`.
* [**04. Slash Commands & UI Actions**](en/04_commands_and_ui.md) — registering chat commands (`/ping`, `/roll`) and UI buttons (`PluginAction`).
* [**05. Building & Packaging**](en/05_building_and_packaging.md) — compiling Java classes into `.dex` with `d8` and packaging `.jar`.
* [**06. Installation & Debugging**](en/06_installation_and_debug.md) — installing via Plugin Manager and debugging logs.

---

## 📂 Готовые примеры плагинов / Sample Plugins
Все примеры исходного кода и скрипт сборки находятся в директории:
* [`tests/`](../tests/) — папка примеров и тестов плагинов.
