# 📚 d2anki - Oxford to Anki Scraper

![Java](https://img.shields.io/badge/Java-25-orange)
![Build](https://img.shields.io/badge/Build-Gradle-elephant)
![License](https://img.shields.io/badge/License-MIT-blue)

**d2anki** is a high-performance, asynchronous CLI tool designed to fetch vocabulary definitions, phonetics, and audio
pronunciations from **Oxford Learner's Dictionaries** and convert them into ready-to-import **Anki** flashcards.

It leverages Java's modern `HttpClient` for non-blocking I/O, ensuring fast execution while respecting server rate
limits using semaphores and dynamic throttling.

---

## 🚀 Features

* **⚡ High-Performance & Asynchronous:** Uses `CompletableFuture` and `Java HttpClient` to fetch definitions and
  download media concurrently without blocking the CPU.
* **🔊 Audio Support:** Automatically downloads **US** and **UK** pronunciation audio (MP3) for every word directly to a
  local media folder.
* **🎨 Rich Formatting:** Generates HTML-styled Anki cards with embedded CSS (Phonetics, Categories, Definitions,
  Examples).
* **🛡️ Smart Throttling:** Includes built-in rate limiting, exponential backoff, and retry logic to prevent IP bans.
* **📦 CLI Support:** Accepts custom word lists via command-line arguments.
* **📝 Logging:** Detailed logging with `Log4j2` (Console & Rolling File support).

---

## 🛠️ Installation & Build

### Prerequisites

* **Java JDK 25**.
* **Gradle** (Wrapper included).

### Building the JAR

Clone the repository and build the "fat jar" (shadow jar) using Gradle:

```bash
git clone [https://github.com/YOUR_USERNAME/d2anki.git](https://github.com/YOUR_USERNAME/d2anki.git)
cd d2anki
./gradlew shadowJar

```

The executable JAR will be located at: `build/libs/d2anki-app-1.0.jar`

---

## 📖 Usage

### 1. Prepare Your Word List

Create a text file (e.g., `my_words.txt`) and add words line by line:

```text
serendipity
ephemeral
obfuscate

```

### 2. Run the Application

You can run the application by passing your text file path as an argument. If no argument is provided, it defaults to
looking for `words.txt` in the same directory.

```bash
# Run with a custom file
java -jar build/libs/d2anki-app-1.0.jar my_words.txt

# Or run with default 'words.txt'
java -jar build/libs/d2anki-app-1.0.jar

```

### 3. Output

After execution, the tool will generate:

1. **`output.tsv`**: The Tab-Separated Values file containing the card data (Front/Back/Tags).
2. **`collection.media/`**: A folder containing all downloaded MP3 files.

---

## ⚠️ Known Limitations

* **Primary Definition Targeting:** Currently, the scraper automatically appends `_1` to URLs to fetch
  the **first entry** (primary definition/homonym) of a word.

---

## 📥 Importing into Anki

1. Open **Anki** on your desktop.
2. Click **File** -> **Import** and select `output.tsv`.
3. **Field Mapping:**
    * Field 1 -> **Front** (Word + Pronunciation)
    * Field 2 -> **Back** (Definitions + Examples)
    * Field 3 -> **Tags**
4. **Important:** Check "Allow HTML in fields".
5. **Media Files:**

* Copy all files from the generated `collection.media` folder.
* Paste them into your Anki User's `collection.media` folder:
* *Windows:* `%APPDATA%\Anki2\User 1\collection.media`
* *Mac:* `~/Library/Application Support/Anki2/User 1/collection.media`
* *Linux:* `~/.local/share/Anki2/User 1/collection.media`

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
