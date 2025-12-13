# Tiko – ULBS Student Assistant

**Tiko** is the official AI-powered student assistant for the “Lucian Blaga” University of Sibiu (ULBS). It is a modern Android application designed to provide students with quick, reliable answers to their most common questions about university life.

## What Problem Does Tiko Solve?

Navigating university bureaucracy can be challenging. Tiko solves this by offering a single, friendly point of contact for information that is often scattered across different websites, documents, and departments. It saves students time and reduces their frustration by providing instant answers to questions about academic procedures, scholarships, and more.

## Key Features

- **AI-Powered Chat**: A modern chat interface where students can ask questions in natural language.
- **ULBS-Focused Scope**: Tiko is specifically trained to answer questions about ULBS, including topics like the academic calendar, scholarships, faculties, and administrative procedures.
- **Multi-Language Support**: Automatically detects the user's language and responds in the same language (e.g., Romanian, English, German).
- **Smart Error Handling**: Gracefully handles network errors, API failures, and rate limiting with friendly, user-facing messages.
- **Polished User Experience**: Features a custom app icon, a quick splash screen, a typing indicator, and subtle animations for a smooth, modern feel.
- **Robust and Secure**: Built with a focus on security and production readiness, including disabled logging in release builds and secure handling of the OpenAI API key.

## Getting Started

To build and run the project, follow these steps:

1.  **Clone the repository**:
    ```sh
    git clone https://github.com/your-username/TikoULBS.git
    ```

2.  **Add Your OpenAI API Key**:
    Create a file named `local.properties` in the root of the project and add your OpenAI API key:
    ```properties
    OPENAI_API_KEY="YOUR_API_KEY_HERE"
    ```

3.  **Build and Run**:
    Open the project in Android Studio and run the `app` configuration.

## Technology Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/) design principles.
- **Architecture**: A single-activity architecture using [Android Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel).
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) for managing background threads and API calls.
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) for making API calls to the OpenAI service, with [OkHttp 4](https://square.github.io/okhttp/) as the HTTP client.
- **Data Serialization**: [Gson](https://github.com/google/gson) for parsing API responses.

## Disclaimer

Tiko provides information based on publicly available data. For critical or personal matters, always verify the information with official ULBS sources, such as the university website or your faculty's secretariat.
