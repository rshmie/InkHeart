# InkHeart Security Design

## Core Cryptographic Design Decisions

### 1. Zero-Knowledge Design

* Encryption happens client-side or with a key that is never stored on the server.
* Server never sees unencrypted data or user keys.
* End-to-End Encryption, i.e., encryption happens before the data leaves the device.
* There is no recovery mechanism by design. If users lose their password, data is unrecoverable.
* Metadata is encrypted not just the content
* Medias like photo, voice notes etc too are encrypted with the same protection
* For TTL, it would be Tamper-proof deletion i.e., deleted entries can't be undeleted and would be unrecoverable.

### 2. Encryption Key Derivation

* **Chosen Algorithm:** `Argon2id`
* **Why:**

    * Recommended by OWASP as the most secure and modern key derivation function.
    * Defends effectively against brute-force and GPU/ASIC-based attacks.
    * Offers configurable parameters to fine-tune for various threat models and hardware capabilities.

### Argon2id Parameters:

```java
ITERATIONS = 3           // OWASP-recommended baseline
MEMORY = 65536 KB (64MB) // High enough to be memory-hard, deters GPU-based attacks
PARALLELISM = 1          // Maintains cross-platform compatibility
```

* These values can be adapted depending on user environment (e.g., mobile vs desktop).

### 3. Encryption Cipher

* **Chosen Algorithm:** `AES-256-GCM`
* **Why:**

    * AEAD cipher (Authenticated Encryption with Associated Data).
    * Provides both confidentiality and integrity.
    * Highly performant and widely adopted in secure applications (TLS 1.3, Signal, etc.).

### Implementation Considerations:

* **IV (Initialization Vector):**

    * 12-byte (96-bit) random value generated for each encryption.
    * Ensures uniqueness per entry; critical for AES-GCM's security.
* **AAD (Additional Authenticated Data):**

    * Optional metadata (e.g., timestamps or entry IDs) that is bound to the ciphertext for authenticity.
    * Prevents tampering with critical metadata without affecting confidentiality.



