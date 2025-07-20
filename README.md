# 🧠 AffectaChat – Emotion-Aware AI Companion

AffectaChat is an emotionally intelligent Android app that blends real-time emotion recognition with AI-driven conversation to support mental wellness. It uses facial expression and voice tone analysis to understand user mood and responds empathetically using Mistral LLM.

## 📱 Features

- 🎥 **Real-time Emotion Detection** – Detects mood using facial expressions via ML Kit  
- 🧠 **Emotion-Aware AI Replies** – Mistral LLM generates empathetic, personalized responses  
- 🎤 **Voice Interaction** – Talk to the app using a mic button with speech-to-text and TTS  
- 🗣️ **Multilingual Support** – Responds in your system language with mood-sensitive voice  
- 📓 **Mood Journal** – Save, edit, delete, export emotion-tagged notes (voice or text)  
- 🎨 **Modern UI** – Built with Jetpack Compose featuring animated mic, TTS, and chat bubbles  
- 🔒 **Local-Only Storage** – No external databases, fully private

## 🧰 Built With

- **Kotlin + Jetpack Compose** – modern Android UI
- **ML Kit** – for face emotion detection
- **Android SpeechRecognizer & TextToSpeech** – voice input/output
- **Mistral LLM** – AI-generated conversational replies
- **Material3** – clean UI components


## 🚀 Getting Started

1. Clone the repository:
   git clone https://github.com/Preetikaa-g/affectachat.git
2. Open in **Android Studio**
3. Run on a physical device (camera & mic permissions required)

## 📂 Folder Structure

```
├── app
│   ├── ui               # Compose screens and interactions
│   ├── network          # Mistral AI prompt handler
│   ├── journal          # Voice/text entry logic
│   └── utils            # Emotion mapping and models
```

## 📄 License

MIT License – use freely with attribution.
