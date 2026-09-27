# 04. Слэш-команды в чате и элементы интерфейса

Плагины TeamTalk Next могут добавлять интерактивные команды в строку ввода чата, а также внедрять пользовательские кнопки действий и диалоги.

---

## ⌨️ Регистрация слэш-команд чата

Слэш-команды — это быстрый и удобный способ взаимодействия пользователя с плагином прямо из чата каналов или личных сообщений.

### Как зарегистрировать команду:
Используйте метод `registerCommand(name, handler)` внутри `onLoad`:

```java
registerCommand("calc", (sender, command, args) -> {
    if (args.length < 3) {
        sender.sendMessage("Использование: /calc <число1> <+|-|*|/> <число2>");
        return true;
    }

    try {
        double a = Double.parseDouble(args[0]);
        String op = args[1];
        double b = Double.parseDouble(args[2]);
        double res = 0;

        switch (op) {
            case "+": res = a + b; break;
            case "-": res = a - b; break;
            case "*": res = a * b; break;
            case "/": res = b != 0 ? a / b : Double.NaN; break;
            default:
                sender.sendMessage("Неизвестный оператор: " + op);
                return true;
        }

        sender.sendMessage("Результат: " + a + " " + op + " " + b + " = " + res);
    } catch (NumberFormatException e) {
        sender.sendMessage("Ошибка: неверный формат чисел!");
    }
    return true;
});
```

### Интерфейс `PluginCommandSender`:
При вызове команды плагин получает объект `sender`:
* `void sendMessage(String message)` — отправляет ответное сообщение пользователю (отображается в виде всплывающего уведомления или сообщения).
* `boolean isLocalUser()` — возвращает `true`, если команду набрал локальный пользователь приложения.
* `int getSenderUserId()` — идентификатор пользователя, выполнившего команду.
* `int getChannelId()` — идентификатор канала, в котором набрана команда.

---

## 🎨 Пользовательские действия и диалоги (`PluginAction`)

Плагин может добавить кнопку действия прямо в карточку своего плагина в менеджере плагинов (например, для открытия окна настроек или вызова справки):

```java
registerAction(new PluginAction() {
    @Override
    public String getId() {
        return "open_my_dialog";
    }

    @Override
    public String getTitle() {
        return "О плагине";
    }

    @Override
    public void onActionClick(Context context) {
        // Показ диалогового окна Android
        new AlertDialog.Builder(context)
            .setTitle("О плагине")
            .setMessage("Версия плагина: " + getInfo().getVersion() + "\nАвтор: " + getInfo().getAuthor())
            .setPositiveButton("Понятно", null)
            .show();
    }
});
```

Кнопка с текстом `getTitle()` автоматически появится в карточке плагина на экране **«Управление плагинами»**.
