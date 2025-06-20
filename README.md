# InkHeart 🖋️❤️
A secure, private, zero-knowledge journal application.
InkHeart is a journaling application built for those who value absolute privacy. It leverages a zero-knowledge architecture, which means your
data is encrypted on your device before it's ever stored on our servers. We, the service providers, have no way to access, read, or decrypt your entries. 
Your journal remains yours and yours alone.

## Why InkHeart?

Most journaling apps today rely on centralized cloud storage, leaving your personal data vulnerable to breaches or misuse. InkHeart was built for users who demand complete control over their data.

It’s ideal for:
- Personal journaling with absolute privacy
- Secure note-taking (e.g., for therapy, health records, or private reflections)
- Developers looking to study zero-knowledge systems or implement strong cryptographic practices

By using end-to-end encryption and client-side authentication, InkHeart ensures that **only you** can read your entries—even we can't.

## Tech Stack
* **Language:** Java 17
* **Framework:** Spring Boot
* **Security:** Spring Security, Bouncy Castle (or other crypto libraries)
* **Build Tool:** Maven
* **Key Protocols:** SRP, AES-256-GCM, Argon2id

## The Core Principle: Zero Knowledge
In a world where data is a commodity, InkHeart is designed to be a sanctuary. Our promise is simple:
We can't see your data. Your entries, tags, and even moods are encrypted client-side using a key derived from your master password.
We don't know your password. Authentication is handled using SRP (Secure Remote Password), a protocol that verifies your identity without your password ever leaving your device.
Data loss is the trade-off for privacy. Because we can't recover your password, we also can't recover your data if you lose it. This is the ultimate proof of our zero-knowledge commitment. 
(A secure user-controlled recovery mechanism is planned for a future).


## Features
* 🔐 **Secure User Registration & Login:** Using the SRP protocol.
* ✍️ **Full CRUD Operations:** Create, View, Edit, and Delete journal entries.
* 🔍 **Powerful Search:** Filter entries by date range or keyword.
* 🏷️ **Mood Tagging:** Tag each entry with a mood for easy reflection.
* 🔒 **End-to-End Encryption:** All features operate on client-side encrypted data.

## Basic Security Design Deep Dive
This project was built with a security-first mindset, using modern, peer-reviewed cryptography.

### 1. Authentication: Secure Remote Password (SRP)
   * SRP is used for password verification. This is a Password-Authenticated Key Exchange (PAKE) protocol.
   * **Why?** 
     * It allows the server to authenticate a user without ever storing a password hash or having the password transmitted across the network, even in an encrypted form. This protects against offline password-cracking attempts on a stolen database.

### 2. Key Derivation: Argon2id
   * The client derives your unique encryption key from your master password.
   * Algorithm: Argon2id
   * **Why?** 
      * It's the current OWASP recommendation for password hashing and key derivation. It is a memory-hard function designed to be highly resistant to both GPU and ASIC-based cracking attempts.
   * Currently these Argon2id parameters are used. These are strong baseline values that can be tuned based on various threat models and hardware capabilities
   ```java
      ITERATIONS = 3           // OWASP-recommended baseline
      MEMORY = 65536 KB (64MB) // High enough to be memory-hard, deters GPU-based attacks
      PARALLELISM = 1          // Maintains cross-platform compatibility
   ```

### 3. Encryption: AES-256-GCM
   * Journal entries and metadata are encrypted using a high-performance, secure cipher.
   * Algorithm: AES-256-GCM
   * **Why?**  
      * It's an Authenticated Encryption with Associated Data (AEAD) cipher. This means it provides not only confidentiality (your data is secret) but also integrity and authenticity (your data cannot be secretly tampered with).
        It is highly performant and widely adopted in secure applications.
      * Implementation: A unique 96-bit (12-byte) Initialization Vector (IV) is generated for every single encrypted entry, which is a critical requirement for AES-GCM's security.

   
## Getting Started
**Prerequisites:**
* JDK 17 or higher
* Maven

**Installation & Running:**
1. Clone the repository:
   `git clone https://github.com/your-username/inkheart.git`
2. Navigate to the project directory:
   `cd inkheart`
3. Build the project:
   `mvn clean install`
4. Run the application:
   `java -jar target/inkheart-0.0.1-SNAPSHOT.jar`

## What's Next
- Containerize with Docker
- Add PostgreSQL support
- Web client 
- AI-based emotional response system to provide warmth and support in emotionally tagged entries.
- Time capsule
- Secure password recovery mechanism
