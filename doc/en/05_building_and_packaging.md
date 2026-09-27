# 05. Building & Packaging Plugins

To run inside Android's ART virtual machine, Java source code must be compiled into standard Java bytecode (`.class`), then translated into Dalvik bytecode (`classes.dex`).

---

## 🛠 Prerequisites

1. **JDK 17** (`javac`, `jar`, `zip`).
2. **Android Build-Tools** with `d8` tool (e.g. `/root/Android/Sdk/build-tools/36.1.0/d8`).
3. `TeamTalk5.jar` and compiled TeamTalk Next classes on the classpath.

---

## 🚀 Step-by-Step Build Process

### Step 1. Setup Environment & Classpath

```bash
TT_ROOT="/root/tt"
CLIENT_CLASSES="$TT_ROOT/client/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes"
TT_JAR="$TT_ROOT/client/libs/TeamTalk5.jar"
ANDROID_JAR="/root/Android/Sdk/platforms/android-34/android.jar"
D8="/root/Android/Sdk/build-tools/36.1.0/d8"

CLASSPATH="$CLIENT_CLASSES:$TT_JAR:$ANDROID_JAR"
```

### Step 2. Compile Java Source Code

```bash
mkdir -p build/classes
javac -cp "$CLASSPATH" -source 17 -target 17 -d build/classes src/org/example/echo/*.java
```

### Step 3. Convert to Android DEX Bytecode with `d8`

```bash
mkdir -p build/dex
$D8 --output build/dex/ --classpath "$CLASSPATH" build/classes/org/example/echo/*.class
```
This generates `classes.dex` inside `build/dex/`.

### Step 4. Package into Final `.jar` Plugin

Copy `plugin.json` next to `classes.dex` and create the archive:

```bash
cp plugin.json build/dex/
cd build/dex
zip -r ../../EchoBotPlugin.jar classes.dex plugin.json
cd ../..
```

The resulting `EchoBotPlugin.jar` contains:
* `classes.dex`: Compiled Dalvik bytecode.
* `plugin.json`: Metadata manifest.

---

## ⚡ Automated Build Script

An automated script to compile all sample plugins is located at:
```bash
bash /root/tt/tests/build_sample_plugins.sh
```
