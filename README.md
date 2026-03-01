# WireMock HMAC Request Matcher Extension

This project is a WireMock extension that provides an HMAC request matcher. It allows you to match incoming requests based on an HMAC signature, typically provided in a header.

## Features

- Matches requests based on HMAC signatures matching against the request body.
- Configurable hashing algorithms (e.g., `HmacSHA256`).
- Supports an optional prefix in the signature header (e.g., matching `sha256=123...` against just the signature portion `123...`).

## Prerequisites

- Java 17 or higher
- [Task](https://taskfile.dev/) (optional, for using `Taskfile.yml`)
- Gradle (provided via wrapper `./gradlew`)

## Compilation

You can compile the project and build the final `.jar` used for the extension.

### Using Task

```bash
task build
```

This will run Gradle and output the compiled `.jar` file to the `build/libs/` directory.

### Using Gradle

```bash
./gradlew clean jar
```

## Using in WireMock Standalone Server

To use this extension in a WireMock standalone server:

1. Build the `.jar` as described above.
2. Run the WireMock standalone jar, specifying the extensions via classpath and explicitly loading the extension class.

Example if your WireMock standalone jar and the extension jar are available in the same directory:

```bash
java -cp "wiremock-standalone-X.Y.Z.jar:build/libs/wiremock-hmac-1.0.0-SNAPSHOT.jar" \
    com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
    --extensions com.github.gersonmartins.wiremock.extension.HmacRequestMatcherExtension
```

_(Note: Adjust the file names according to the specific WireMock version you are using and the exact filename in `build/libs/`)_

## Example Usage

When defining a stub in WireMock, you can use the custom matcher like this:

```json
{
  "request": {
    "method": "POST",
    "urlPath": "/api/secure",
    "customMatcher": {
      "name": "hmac-matcher",
      "parameters": {
        "secret": "your-secret-key",
        "header": "X-Hmac-Signature",
        "algorithm": "HmacSHA256",
        "prefix": "sha256="
      }
    }
  },
  "response": {
    "status": 200,
    "body": "Authenticated successfully!"
  }
}
```

### Parameters

- `secret` (Required): The secret key used to generate the HMAC for the request body.
- `header` (Optional): The name of the header containing the signature. Defaults to `X-Hmac-Signature`.
- `algorithm` (Optional): The hashing algorithm to use (e.g., `HmacSHA256`, `HmacMD5`, `HmacSHA512`). Defaults to `HmacSHA256`.
- `prefix` (Optional): A prefix string to strip from the incoming header signature before validation.
