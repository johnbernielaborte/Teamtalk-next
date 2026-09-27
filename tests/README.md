# Тестовые плагины и примеры / Sample Plugins and Tests

[English](#english) | [Русский](#russian)

---

<a name="russian"></a>
## Русский

Данная директория содержит примеры и тестовые плагины для системы плагинов **TeamTalk Next (v5.28.6)**.

### Структура директории

- `sample_echo_bot/` — плагин-бот, демонстрирующий подписку на входящие личные сообщения и автоматический ответ (эхо).
- `sample_utility_commands/` — плагин с набором slash-команд для чата (`/ping`, `/roll`, `/channelid`, `/calc`).
- `sample_channel_logger/` — плагин логирования событий канала (подключение/отключение пользователей, передача голоса).
- `build_sample_plugins.sh` — автоматический скрипт сборки плагинов в Dalvik DEX и упаковки в `.jar`.
- `output/` — готовые скомпилированные `.jar` файлы плагинов, готовые для установки в клиент.

### Сборка плагинов

Для сборки всех примеров выполните:
```bash
./build_sample_plugins.sh
```

Скрипт выполнит:
1. Компиляцию Java-исходников плагинов с целевой версией Java 17.
2. Трансляцию байт-кода в формат Android Dalvik DEX с помощью утилиты `d8`.
3. Упаковку `classes.dex` и `plugin.json` в финальные `.jar` файлы в папке `output/`.

### Установка плагинов в TeamTalk Next

#### Способ 1: Через интерфейс приложения (UI)
1. Скопируйте нужный `.jar` файл из папки `output/` на мобильное устройство (например, в папку `Загрузки`).
2. Откройте **TeamTalk Next** -> Меню (три точки) -> **Менеджер плагинов**.
3. Нажмите кнопку **Установить плагин** и выберите `.jar` файл через системный проводник.
4. Плагин будет проверен, скопирован и готов к включению.

#### Способ 2: Через ADB (для разработчиков)
```bash
adb push output/EchoBotPlugin.jar /sdcard/Android/data/org.nekit.ttproplus/files/plugins/
```
После этого откройте экран «Менеджер плагинов» и нажмите «Обновить список».

---

<a name="english"></a>
## English

This directory contains reference implementations and test plugins for the **TeamTalk Next (v5.28.6)** Plugin Architecture.

### Directory Structure

- `sample_echo_bot/` — an echo bot demonstrating text message listening and replying.
- `sample_utility_commands/` — custom chat slash commands (`/ping`, `/roll`, `/channelid`, `/calc`).
- `sample_channel_logger/` — channel event listener logging user joins/leaves and voice state changes.
- `build_sample_plugins.sh` — automated build script that compiles Java code, invokes `d8` to generate `classes.dex`, and packages `.jar` archives.
- `output/` — pre-built `.jar` plugin packages ready to install into the TeamTalk Next client.

### Building Plugins

To build all sample plugins:
```bash
./build_sample_plugins.sh
```

### Installation

#### Method 1: In-App UI
1. Copy the `.jar` file to your Android device storage.
2. Open **TeamTalk Next** -> Options menu -> **Plugin Manager**.
3. Tap **Install Plugin** and select the `.jar` file.
4. Toggle the switch to activate the plugin.

#### Method 2: ADB
```bash
adb push output/UtilityCommandsPlugin.jar /sdcard/Android/data/org.nekit.ttproplus/files/plugins/
```
Then tap "Refresh list" in the Plugin Manager.
