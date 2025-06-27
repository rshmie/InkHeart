# 🌿 InkHeart 🖋️

A secure, private, zero-knowledge journal application.

InkHeart is a journaling application built for those who value absolute privacy. It leverages a zero-knowledge architecture, meaning your data is encrypted on your device *before* it's ever stored on our servers. Even service providers have no way to access, read, or decrypt your entries.
Your journal remains yours and yours alone.

## Why InkHeart?

Most journaling apps today rely on centralized cloud storage, leaving your personal data vulnerable to breaches and misuse. InkHeart was built for users who demand complete control over their data and privacy.

It's ideal for:
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

In a world where data is a commodity, InkHeart gives you a private space that's truly yours. 
The Promise is simple:

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
* **AAD**: GCM mode of encryption takes an optional field which binds the cipher text to the context and this data need not have to be secret. This is crucial for ensuring that the context of the ciphertext hasn't been tampered with.
<br/> Following AAD pattern is used: `AAD = concatenate(Entry_ID, Field_Name)`  (separated by delimiter ":")
    * Prevents fields swapping within and between entries: Ensures that the encrypted content belongs to that specific entry.
      If an attacker tries to swap content between EntryA and EntryB, the GCM tag verification would fail because the AAD that we associated it with would be incorrect for the swapped content
      and cannot be successfully decrypted. This provides integrity across different entries.
  
## Getting Started

**Prerequisites:**
* JDK 17 or higher
* Git
* Maven (optional, as the Maven wrapper is included)

**Installation & Build:**
1.  Clone the repository: (or download the zip)
    ```sh
    https://github.com/rshmie/InkHeart.git
    ```
2.  Navigate to the project directory:
    * If you cloned the repo:
      ```sh
       cd inkHeart
      ```
      
    * If you downloaded the ZIP and extracted it:
      ```sh
       cd InkHeart-main
      ```
    Make sure you're in the root directory before building or running the project/script.

**Build and Run (Automated):**

A convenient helper script is provided to automate the entire build and launch process.

1. Execute the starter script:
   ```sh
      ./start-inkHeart.sh
   ```
   This script will perform the following actions:
   - Build the entire project.
   - Start the Spring Boot backend server in the background.
   - Launch the interactive Command-Line Interface (CLI).
   - Prompt you to shut down the backend server after your CLI session ends.


2. Stopping the Server:
   If you need to manually stop the backend server, use the provided script:
   ```sh
      ./stop-backend.sh
   ```

*Note: These scripts assume a Unix-like shell (macOS/Linux/Git Bash on Windows).
If you're on Windows and using CMD or PowerShell, consider using WSL or Git Bash.*

**Optional: Manual Build and Run**

If you prefer to build and run the application components separately, follow the steps below.

1. Build the project:
    ```sh
    ./mvnw clean install
    ```
   This command compiles the source code, runs tests, and packages the application into executable JAR files, which will be located in the `inkHeart-backend/target` and `inkHeart-cli/target` directories.

    <br/>**JWT Secret Handling**:
   During the build process, a `jwt.properties` file is automatically generated under the `inkHeart-backend` directory if one does not already exist. 
   This file contains a secure JWT secret and is used internally by the backend for authentication.
   To enhance security, this file is explicitly excluded from version control (via `.gitignore`), eliminating the need for manual secret configuration, especially in environments without a dedicated secret manager.


2. Running the Application: 

   The application consists of two main components - the backend server and the CLI client. You'll need to run them in separate terminal windows.
   
   * Start the Backend Server:
      
      Go to `inkHeart-backend` directory from the project root.
      In a terminal, run the following command from the current directory.
      ```sh
       java -jar target/inkHeart-*.jar
      ```
      The backend server will start on port 8080 by default. 
   
   * Run the CLI Client:
      
      Open a *new* terminal window. From the project root, go to `inkHeart-cli` directory and start the command-line interface
      ```sh
         java -jar target/inkHeart-cli-*.jar
      ```
      The interactive CLI session will start.


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
