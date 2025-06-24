# 🌿 InkHeart 🖋️

A secure, private, zero-knowledge journal application.

InkHeart is a journaling application built for those who value absolute privacy. It leverages a zero-knowledge architecture, meaning your data is encrypted on your device *before* it's ever stored on our servers. Even service providers have no way to access, read, or decrypt your entries.
Your journal remains yours and yours alone.

## Why InkHeart?

Most journaling apps today rely on centralized cloud storage, leaving your personal data vulnerable to breaches and misuse. InkHeart was built for users who demand complete control over their data and privacy.

It’s ideal for:
-   Personal journaling with absolute privacy.
-   Secure note-taking (e.g., for therapy notes, health records, or private reflections).
-   Developers looking to dive deep into zero-knowledge systems or implement strong cryptographic practices.

By using the zero-knowledge principle with end-to-end encryption and client-side authentication, InkHeart ensures that **only you** can read your entries. Not even the system administrators can.

## Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3
* **Security:** Spring Security
    * **Cryptographic Primitives:** Bouncy Castle
    * **SRP6a Implementation:** NimbusDS
* **Build Tool:** Maven
* **Key Protocols:** SRP6a, AES-256-GCM, Argon2id

## The Core Principle: Zero Knowledge

In a world where data is a commodity, InkHeart is designed to be a sanctuary. Promise is simple:

> **We know nothing about you or your data.**

* **No one can see your data.** Your entries, tags, and even moods are encrypted client-side using a key derived from your master password.
* **No one knows your password.** Authentication is handled using SRP (Secure Remote Password), a protocol that verifies your identity without your password ever leaving your device.
* **Data loss is the trade-off for privacy.** Because we can't recover your password, we also can't recover your data if you lose it. This is the ultimate proof of our zero-knowledge commitment. (A secure, user-controlled recovery mechanism is planned for a future release).

## Features

* 🔐 **Secure User Registration & Login:** Authentication powered by the Secure Remote Password (SRP) protocol.
* ✍️ **Full CRUD Operations:** Create, view, edit, and delete journal entries.
* 🏷️ **Mood Tagging:** Tag each entry with a mood for easy reflection.
* 🔍 **Powerful Search:** Filter entries by date range, view the 10 most recent entries, and search by mood, tag, or keyword/phrase.
* 🔒 **End-to-End Encrypted:** All features operate on data that is only ever decrypted on the client side.

## Security Design Deep Dive

This project was built with a security-first mindset using modern, peer-reviewed cryptography.

### 1. Authentication: Secure Remote Password (SRP)
SRP is a Password-Authenticated Key Exchange (PAKE) protocol used for password verification.

* **Why SRP?** It allows the server to authenticate a user without ever storing a password hash or transmitting the password across the network (even in encrypted form). This protects against replay attacks and offline password-cracking attempts on a stolen database.

### 2. Key Derivation: Argon2id
The client derives your unique encryption key from your master password using Argon2id, the current OWASP recommendation for key derivation. It is a memory-hard function designed to be highly resistant to GPU and ASIC-based cracking attempts.

* **Current Parameters:** These are strong baseline values that can be tuned based on threat models and hardware.
    ```java
    // OWASP-recommended baseline
    ITERATIONS = 3;
    // 64MB: High enough to be memory-hard, deters GPU-based attacks
    MEMORY = 65536; // in KB
    // Maintains cross-platform compatibility
    PARALLELISM = 1;
    ```

### 3. Encryption: AES-256-GCM
Journal entries and metadata are encrypted using AES-256-GCM, an Authenticated Encryption with Associated Data (AEAD) cipher.

* **Why AES-256-GCM?** It provides confidentiality (your data is secret), integrity, and authenticity (your data cannot be secretly tampered with). It is highly performant and widely adopted in secure applications. A unique 96-bit (12-byte) Initialization Vector (IV) is generated for every single encrypted entry, which is a critical requirement for AES-GCM's security.

## Getting Started

**Prerequisites:**
* JDK 17 or higher
* Git
* Maven (optional, as the Maven wrapper is included)

**Installation & Build:**
1.  Clone the repository:
    ```sh
    git clone [https://github.com/your-username/inkheart.git](https://github.com/your-username/inkheart.git)
    ```
2.  Navigate to the project directory:
    ```sh
    cd inkheart
    ```
3.  Build the project:
    ```sh
    ./mvnw clean install
    ```
    This command will compile the code, run tests, and package the application into executable JAR files located in the `inkHeart-backend/target` and `inkHeart-cli/target` directories.

**Running the Application:**

First, start the backend server, and then run the CLI to interact with it.

1.  **Start the Backend Server**
    In a terminal, run the following command from the project root:
    ```sh
    java -jar inkHeart-backend/target/inkheart-*.jar
    ```
    The backend server will start on port 8080 by default.

2.  **Run the CLI Client**
    Open a *new* terminal window. From the project root, start the command-line interface:
    ```sh
    java -jar inkHeart-cli/target/inkheart-cli-*.jar
    ```
    The interactive CLI session will start.

**Optional: Using the Startup Script**

A helper script is provided to automate the build and launch process.
```sh
./start-inkHeart.sh
```
This script will:
- Build the entire project
- Start the Spring Boot backend in the background
- Launch the CLI for interaction
- Prompt you whether to shut down the backend after the CLI session ends

To manually stop the backend server use this script `./stop-backend.sh`

*Note:  These scripts assume a Unix-like shell (macOS/Linux/Git Bash on Windows).
If you're on Windows and using CMD or PowerShell, consider using WSL or Git Bash.*

## Roadmap

#### Core Infrastructure & Backend
- [ ] **Containerize with Docker:** Provide a `Dockerfile` for simplified deployment.
- [ ] **Persistent Storage:** Add PostgreSQL support to the backend for durable data storage.

#### Client & Performance
- [ ] **Web Client:** Develop a full-featured web client as an alternative to the CLI and improve UI/UX.
- [ ] **Search Optimization:** Implement a client-side caching system using an embedded database (eg., SQLite) to provide instant search results after initial sync.

#### Future Features
- [ ] **Time Capsule:** A feature to "lock" an entry until a user-specified future date.
- [ ] **AI Insights:** An opt-in, client-side AI model to provide supportive feedback on emotionally-tagged entries, ensuring analysis happens locally to preserve privacy.
- [ ] **Secure Recovery:** Implement a secure, user-controlled password recovery mechanism (e.g., using a recovery key).