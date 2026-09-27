# 03. Справочник API плагинов

Подробное описание интерфейсов и методов, доступных разработчикам плагинов TeamTalk Next.

---

## 📌 Интерфейс `PluginContext`

Передаётся в метод `onLoad(PluginContext context)`. Доступен внутри `BasePlugin` через вызов `getContext()`.

### Базовые геттеры:
* `Context getApplicationContext()` — системный Android Context приложения.
* `TeamTalkService getService()` — активная служба TeamTalkService (управление потоками, звуком, соединением).
* `TeamTalkBase getClient()` — экземпляр нативного клиента TeamTalk 5 SDK (вызов любых C++ JNI методов ядра).
* `PluginInfo getInfo()` — метаданные текущего плагина.
* `PluginLogger getLogger()` — персональный логгер с префиксом `[TTPlugin/<Имя>]`.
* `File getDataFolder()` — приватная директория для сохранения файлов плагина (конфигурации, базы данных, дампы).
* `SharedPreferences getPreferences()` — изолированное хранилище ключ-значение.

### Отправка сообщений:
* `void sendTextMessage(TextMessage msg)` — отправка готового объекта сообщения через SDK.
* `void sendChannelMessage(String text)` — быстрая отправка текста в текущий канал.
* `void sendUserMessage(int userId, String text)` — отправка личного сообщения пользователю по его User ID.
* `void sendBroadcastMessage(String text)` — серверное широковещательное оповещение (если есть права оператора).

### Управление каналами:
* `void joinChannel(int channelId, String password)` — переход в канал по ID и паролю.
* `void leaveChannel()` — выход из текущего канала.

### Асинхронность и планировщик задач:
* `void runOnMainThread(Runnable action)` — выполнение кода в главном UI-потоке Android (для показа Toast, диалогов, обновления View).
* `void runAsync(Runnable task)` — фоновое асинхронное выполнение в пуле потоков плагинов.
* `ScheduledFuture<?> scheduleTask(Runnable task, long delayMs)` — однократный таймер с задержкой в миллисекундах.
* `ScheduledFuture<?> schedulePeriodicTask(Runnable task, long initialDelayMs, long periodMs)` — повторяющаяся периодическая задача.

### Регистрация слушателей и команд:
* `void registerEventListener(PluginEventListener listener)` — подписка на события.
* `void unregisterEventListener(PluginEventListener listener)` — отписка от событий.
* `void registerCommand(String command, PluginCommandHandler handler)` — добавление слэш-команды.
* `void unregisterCommand(String command)` — удаление слэш-команды.
* `void registerAction(PluginAction action)` — добавление кнопки действия в карточку плагина.

---

## 👂 Интерфейс `PluginEventListener`

Все методы имеют реализацию по умолчанию (`default`), переопределяйте только нужные.

### Сеть и авторизация:
| Метод | Описание |
|---|---|
| `void onConnectSuccess()` | Успешное TCP/UDP соединение с сервером |
| `void onConnectFailed()` | Ошибка подключения к серверу |
| `void onConnectionLost()` | Обрыв соединения с сервером |
| `void onLoggedOn(User myUser)` | Успешный вход под учётной записью |
| `void onLoggedOut()` | Выход из учётной записи / сервера |

### Текстовые сообщения:
* `boolean onTextMessageReceived(TextMessage message)`  
  Срабатывает при получении текстового сообщения.  
  *Возвращаемое значение:* `true` — перехватить и скрыть сообщение (оно не попадёт в историю и не вызовет звук), `false` — продолжить стандартную обработку.
* `boolean onTextMessageSending(TextMessage message)`  
  Срабатывает перед отправкой сообщения пользователем на сервер.  
  *Возвращаемое значение:* `true` — отменить отправку, `false` — разрешить отправку.

### Каналы:
* `void onChannelJoined(Channel channel)` — вход локального пользователя в канал.
* `void onChannelLeft(Channel channel)` — выход из канала.
* `void onChannelAdded(Channel channel)` — на сервере создан новый канал.
* `void onChannelUpdated(Channel channel)` — параметры канала изменены.
* `void onChannelRemoved(Channel channel)` — канал удален.

### Пользователи:
* `void onUserJoined(User user)` — вход пользователя на сервер/в канал.
* `void onUserLeft(User user)` — выход пользователя.
* `void onUserUpdated(User user)` — смена ника, статуса или прав пользователя.
* `void onUserSpeaking(User user, boolean speaking)` — начало или завершение разговора пользователем.

### Звуковая подсистема:
* `void onVoiceTransmissionStateChanged(boolean transmitting)` — изменение статуса передачи голоса локальным микрофоном (`true` — говорит, `false` — молчит).
