# 02. Быстрый старт: Создание первого плагина

В этом руководстве мы пошагово создадим простой плагин «Эхо-Бот», который перехватывает личные сообщения и автоматически отвечает отправителю.

---

## 📁 Структура исходного кода плагина

Проект плагина состоит из двух основных элементов: манифеста `plugin.json` и Java-исходников:

```text
my-echo-plugin/
├── plugin.json
└── src/
    └── org/
        └── example/
            └── echo/
                └── EchoBotPlugin.java
```

---

## 📝 Шаг 1. Создание манифеста `plugin.json`

В корне проекта создайте файл `plugin.json`:

```json
{
  "id": "org.example.echobot",
  "name": "Эхо Бот",
  "version": "1.0.0",
  "versionCode": 1,
  "author": "Разработчик",
  "description": "Автоматически отвечает на входящие личные сообщения.",
  "mainClass": "org.example.echo.EchoBotPlugin",
  "minClientVersion": "5.28.6"
}
```

### Назначение полей:
* `id` — глобальный идентификатор плагина (используется для хранения настроек и состояния).
* `name` — имя плагина в списке установленных плагинов.
* `version` — отображаемая версия (строка).
* `versionCode` — целочисленный номер ревизии.
* `author` — автор разработки.
* `description` — подробное описание для пользователя.
* `mainClass` — полное каноническое имя класса точки входа, реализующего `Plugin`.
* `minClientVersion` — минимальная версия клиента TeamTalk Next (`5.28.6` или выше).

---

## 💻 Шаг 2. Написание кода плагина (`EchoBotPlugin.java`)

Создайте класс `EchoBotPlugin`, унаследованный от `BasePlugin`:

```java
package org.example.echo;

import org.nekit.ttproplus.plugin.BasePlugin;
import org.nekit.ttproplus.plugin.PluginContext;
import dk.bearware.TextMessage;
import dk.bearware.TextMsgType;

public class EchoBotPlugin extends BasePlugin {

    @Override
    public void onLoad(PluginContext context) throws Exception {
        super.onLoad(context);
        getLogger().info("Эхо-бот успешно загружен!");

        // Регистрируем быструю команду проверки статуса /echostatus
        registerCommand("echostatus", (sender, command, args) -> {
            sender.sendMessage("🟢 Эхо-бот активен и готов к работе.");
            return true;
        });
    }

    @Override
    public void onEnable() throws Exception {
        getLogger().info("Эхо-бот включен пользователем.");
    }

    @Override
    public void onDisable() throws Exception {
        getLogger().info("Эхо-бот выключен.");
    }

    @Override
    public boolean onTextMessageReceived(TextMessage message) {
        // Проверяем, что это входящее личное сообщение (MSGTYPE_USER)
        if (message.nMsgType == TextMsgType.MSGTYPE_USER && message.szMessage != null) {
            String incoming = message.szMessage.trim();

            // Если сообщение отправлено другим пользователем (не нами)
            if (getClient() != null && message.nFromUserID != getClient().getMyUserID()) {
                String reply = "Эхо: " + incoming;
                sendUserMessage(message.nFromUserID, reply);
                getLogger().info("Отвечено пользователю #" + message.nFromUserID);
            }
        }
        // Возвращаем false, чтобы сообщение также отобразилось в окне чата пользователя
        return false;
    }
}
```

---

## 🚀 Что дальше?
Перейдите к разделу [**05. Сборка и упаковка**](05_building_and_packaging.md), чтобы скомпилировать данный код в готовый `.jar` файл для установки на телефон!
