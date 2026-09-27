# 05. Сборка и упаковка плагина

Чтобы плагин мог исполняться виртуальной машиной Android (ART), Java-исходники должны быть скомпилированы в стандартный Java-байткод (`.class`), а затем транслированы в Dalvik-байткод (`classes.dex`).

---

## 🛠 Необходимые инструменты

1. **JDK 17** (`javac`, `jar`, `zip`).
2. **Android Build-Tools** с утилитой `d8` (например, `/root/Android/Sdk/build-tools/36.1.0/d8`).
3. Библиотека `TeamTalk5.jar` и скомпилированные классы TeamTalk Next для classpath.

---

## 🚀 Пошаговый процесс сборки

### Шаг 1. Подготовка путей и classpath

```bash
# Пути к SDK и классам клиента TeamTalk
TT_ROOT="/root/tt"
CLIENT_CLASSES="$TT_ROOT/client/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes"
TT_JAR="$TT_ROOT/client/libs/TeamTalk5.jar"
ANDROID_JAR="/root/Android/Sdk/platforms/android-34/android.jar"
D8="/root/Android/Sdk/build-tools/36.1.0/d8"

CLASSPATH="$CLIENT_CLASSES:$TT_JAR:$ANDROID_JAR"
```

### Шаг 2. Компиляция исходного кода Java

```bash
mkdir -p build/classes
javac -cp "$CLASSPATH" -source 17 -target 17 -d build/classes src/org/example/echo/*.java
```

### Шаг 3. Трансляция в DEX-байткод утилитой `d8`

```bash
mkdir -p build/dex
$D8 --output build/dex/ --classpath "$CLASSPATH" build/classes/org/example/echo/*.class
```
В директории `build/dex/` будет создан файл `classes.dex`.

### Шаг 4. Упаковка в итоговый `.jar` плагин

Скопируйте `plugin.json` в папку с `classes.dex` и упакуйте в архив:

```bash
cp plugin.json build/dex/
cd build/dex
zip -r ../../EchoBotPlugin.jar classes.dex plugin.json
cd ../..
```

Готово! Файл `EchoBotPlugin.jar` содержит:
* `classes.dex` — исполняемый байткод плагина;
* `plugin.json` — манифест с метаданными.

---

## ⚡ Автоматическая сборка скриптом

В папке `tests/` проекта подготовлен готовый bash-скрипт сборки:
```bash
bash /root/tt/tests/build_sample_plugins.sh
```
Скрипт автоматически соберет все примеры плагинов и создаст готовые `.jar` пакеты.
