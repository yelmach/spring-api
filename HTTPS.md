# Enabling HTTPS (SSL/TLS) for Local Development

This guide explains what HTTPS is and how to enable it for this Spring Boot application using a self-signed certificate for local development and testing.


## 1. What is HTTPS?

**HTTPS** stands for **HTTP Secure**. It is the standard HTTP protocol (used for web communication) wrapped in a layer of security. This security layer is called **SSL/TLS**.

The "S" in HTTPS provides two critical things:

### Encryption
All data sent between the client (your browser or Postman) and the server is scrambled. This prevents "man-in-the-middle" attacks where an attacker on the same Wi-Fi network could otherwise read your passwords, API keys, or JWTs.

### Authentication
The client can verify that the server is actually the server it claims to be (e.g., that you are really talking to `google.com` and not an impersonator).

This is all made possible by an **SSL Certificate**.


## 2. Core Concepts

### SSL/TLS
The protocol that provides the encryption.

### SSL Certificate
A digital "ID card" for a server. It contains the server's domain name (e.g., `localhost`) and its public key.

### Certificate Authority (CA)
A globally trusted third party (like Let's Encrypt or Verisign) that issues and verifies these "ID cards". Your browser has a built-in list of trusted CAs.

### Self-Signed Certificate (What we are using)
This is a "fake ID" that we create for ourselves.

- **Pro**: It provides the exact same powerful encryption as a real certificate, which is perfect for development.
- **Con**: It is not signed by a trusted CA. This means browsers and tools will show a big security warning, which is expected.

### Java KeyStore (PKCS12)
This is a password-protected file (a "digital wallet") that Java uses to store the certificate and, most importantly, its secret private key. Our file is `keystore.p12`.


## 3. How This Project is Configured for HTTPS

We are using Java's built-in `keytool` to generate a self-signed certificate and store it in a PKCS12 keystore.

### Step 1: Create the Keystore

We use this `keytool` command to generate our certificate and keystore file:

```bash
keytool -genkeypair -alias springboot -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 365
```

**Parameters Explained:**

- `-genkeypair`: Generate a new key pair (public and private).
- `-alias springboot`: The "name" of this specific certificate inside the keystore.
- `-keyalg RSA`: Use the standard RSA encryption algorithm.
- `-keysize 2048`: A secure 2048-bit key size.
- `-storetype PKCS12`: Create a modern, standard keystore.
- `-keystore keystore.p12`: The name of the file to create.
- `-validity 365`: Makes the certificate valid for 365 days.

This command will create the `keystore.p12` file. For this project, it must be placed in the `src/main/resources` folder.

---

### Step 2: Configure Spring Boot

We then tell Spring Boot how to use this keystore by adding these properties to `application.properties`:

```properties
# --- HTTPS (SSL/TLS) Configuration ---

# 1. Change the server port to 8443 (the standard for HTTPS dev)
server.port=8443

# 2. Enable SSL
server.ssl.enabled=true

# 3. Tell Spring where to find the keystore file
# 'classpath:' means it will look in 'src/main/resources'
server.ssl.key-store=classpath:keystore.p12

# 4. Tell Spring what type of file it is
server.ssl.key-store-type=PKCS12

# 5. Tell Spring the password for the keystore
server.ssl.key-store-password=password

# 6. Tell Spring which certificate to use inside the keystore (our alias)
server.ssl.key-alias=springboot
```

**Configuration Breakdown:**

| Property | Value | Purpose |
|----------|-------|---------|
| `server.port` | `8443` | Standard HTTPS port for development |
| `server.ssl.enabled` | `true` | Enables SSL/TLS |
| `server.ssl.key-store` | `classpath:keystore.p12` | Location of the keystore file |
| `server.ssl.key-store-type` | `PKCS12` | Type of keystore format |
| `server.ssl.key-store-password` | `password` | Password to unlock the keystore |
| `server.ssl.key-alias` | `springboot` | Which certificate to use from the keystore |

---

## 4. How to Test (Handling "Self-Signed" Warnings)

Your application now runs on `https://localhost:8443`. Because the certificate is self-signed, you must tell your client to trust it.

### In Postman

1. Change your request URLs from `http://localhost:8080/` to `https://localhost:8443/`.

2. When you send your first request, it will likely fail with an **"SSL Error"**.

3. **The Fix**: Go to **File > Settings > General** and turn **OFF** "SSL certificate verification".

4. Send the request again. It will now work over a secure HTTPS connection.

**Visual Guide:**
```
Postman Settings → General → SSL certificate verification → OFF
```

---

### In a Web Browser

1. Navigate to a public endpoint, e.g., `https://localhost:8443/api/products`.

2. You will see a large security warning: **"Your connection is not private"**. This is expected.

3. **The Fix**:
   - Click the **"Advanced"** button.
   - Click the link that says **"Proceed to localhost (unsafe)"**.

4. The browser will now load the JSON from your secure server.

**Example Warning Messages:**

- **Chrome**: "Your connection is not private" (NET::ERR_CERT_AUTHORITY_INVALID)
- **Firefox**: "Warning: Potential Security Risk Ahead"
- **Safari**: "This Connection Is Not Private"

All of these are **expected and normal** for self-signed certificates in development.


## 5. What Data is Now Protected?

With HTTPS enabled, the following data is now **encrypted** during transmission:

### Authentication Data
- ✅ User passwords during registration and login
- ✅ JWT tokens in Authorization headers
- ✅ User credentials (email, name)

### API Request/Response Data
- ✅ Product information (name, description, price)
- ✅ User management data
- ✅ All JSON payloads
- ✅ Query parameters and path variables

### Headers
- ✅ Authorization headers
- ✅ Content-Type headers
- ✅ All custom headers

**Without HTTPS** (Plain HTTP):
```
POST /api/auth/login HTTP/1.1
Content-Type: application/json

{"email":"user@test.com","password":"MySecretPassword"}  ← READABLE!
```

**With HTTPS** (Encrypted):
```
POST /api/auth/login HTTP/1.1
Content-Type: application/json

�j3k��2k4�2�k3j4k�2j3k4�2j3k4�2j3k4�  ← ENCRYPTED!
```
