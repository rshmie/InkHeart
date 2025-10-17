# 🌿 InkHeart 🖋️

Your private journal, encrypted on your device and unreadable by anyone but you.

InkHeart is a journaling application built for those who value absolute privacy. It leverages a zero-knowledge architecture, meaning your data is encrypted on your device *before* it's sent to the backend.
This zero-knowledge architecture ensures that even the service provider hosting the data has no way to access, read, or decrypt your entries. Your journal remains yours and yours alone.

> ### 📍 Current State: Architectural Demo
>
> This project is a fully functional proof-of-concept demonstrating the zero-knowledge architecture and end-to-end encryption. The backend currently uses an **in-memory H2 database**, meaning **all data will be cleared when the server is shut down**.
>
> Persistent storage is my high-priority item on the project [Roadmap](#roadmap).
>

## Why InkHeart?

Most journaling apps today rely on centralized cloud storage, leaving your personal data vulnerable to breaches, misuse and legal obligations. InkHeart is built for users who demand complete control over their data and privacy.

It's ideal for:
-   Personal journaling with absolute privacy.
-   Secure note-taking (e.g., for therapy notes, health records, or private reflections).
-   Developers looking to dive deep into zero-knowledge systems or implement strong cryptographic practices.

By using the zero-knowledge principle with end-to-end encryption and client-side authentication, InkHeart ensures that **only you** can read your entries. Not even the system administrators can.

## Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3
* **Database:** H2 (In-Memory Mode)
* **Security:** Spring Security
    * **Cryptographic Primitives:** Bouncy Castle
    * **SRP6a Implementation:** NimbusDS
* **Build Tool:** Maven
* **Key Protocols:** SRP6a, AES-256-GCM, Argon2id

## The Core Principle: Zero Knowledge

In a world where data is a commodity, InkHeart gives you a private space that's truly yours. It leverages **zero-knowledge architecture**, a paradigm where 
the server cannot access user data even if it wants to. This is not merely an implementation detail but a fundamental architectural principle.

**What Zero-Knowledge Means in Practice**:

The term "zero-knowledge" originates from cryptographic proof systems where one party (the prover) can prove to another party (the verifier) that they 
know a value without revealing the value itself. InkHeart extends this concept to data storage and authentication:

1. **Data Encryption Without Server Keys**: All journal entries are encrypted on the client device before transmission. The encryption key is derived from 
  the user's password using a memory-hard key derivation function (Argon2id). This key never leaves the client device, never travels over the network (even encrypted), 
  and is never stored anywhere except in volatile memory during the user's session. The server receives only ciphertext, which is computationally infeasible to decrypt without the key.

2. **Authentication Without Password Transmission**: Traditional authentication systems require users to send their password to the server, which then 
 verifies it against a stored hash. This creates a window of vulnerability where the server sees the plaintext or hashed password. InkHeart uses the Secure Remote Password (SRP) protocol, 
 a Password-Authenticated Key Exchange (PAKE) mechanism that allows the server to verify the user without password ever leaving the client device.

3. **Irrecoverable by Design**: If a user forgets their password, their data cannot be recovered. This is not a limitation but a feature that proves the 
 zero-knowledge guarantee. If data were recoverable, it would mean the service provider retained some mechanism to decrypt user data, violating the zero-knowledge 
 principle. This trade-off explicitly prioritizes privacy over convenience.

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
    git clone https://github.com/rshmie/InkHeart.git
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

1. Start the Application:
   Run the starter script from the project root
   ```sh
      ./start-inkHeart.sh
   ```
   This script will perform the following actions:
   - Build the entire project.
   - Start the Spring Boot backend server in the background.
   - Launch the interactive Command-Line Interface (CLI) where you can register a new account or log in.
   - Prompt you to shut down the backend server after your CLI session ends.

    <br/>To run only the CLI (assuming the backend is already running):
    ``` sh
       ./start-inkHeart.sh --only-cli
    ```
      

2. Stopping the Server:
   If you need to manually stop the backend server, use the provided script:
   ```sh
      ./stop-backend.sh
   ```

*Note: These scripts assume a Unix-like shell (macOS/Linux/Git Bash on Windows).
If you're on Windows and using CMD or PowerShell, consider using WSL or Git Bash.*

**Optional: Manual Build and Run**

If you prefer to run the components separately, you can do so from the project's root directory.

1.  **Build the Project:**
    First, build both the backend and CLI modules.
    ```sh
    ./mvnw clean install
    ```
    This command compiles the source code, runs tests, and packages the application into executable JAR files.

    <br/>**JWT Secret Handling**:
    During the build process, a `jwt.properties` file is automatically generated under the `inkHeart-backend` directory if one does not already exist.
    This file contains a secure JWT secret and is used internally by the backend for authentication.
    To enhance security, this file is explicitly excluded from version control (via `.gitignore`), eliminating the need for manual secret configuration, especially in environments without a dedicated secret manager.


2.  **Run the Application (in separate terminals):**
    You will need two separate terminal windows, both at the project's root.

    * **Terminal 1: Start the Backend Server**
      Run the following command to start the server. It will launch on port 8080 by default.
        ```sh
        java -jar inkHeart-backend/target/inkHeart-*.jar
        ```

    * **Terminal 2: Run the CLI Client**
      In your second terminal, run this command to start the interactive command-line interface.
        ```sh
        java -jar inkHeart-cli/target/inkHeart-cli-*.jar
        ```


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
