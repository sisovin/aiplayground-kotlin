# AI Playground - Android Learning Platform

An innovative Android application built with Kotlin and Jetpack Compose that provides an interactive learning platform for mastering AI agent development. Explore, learn, and experiment with 9 different AI agent types through hands-on courses and real-time playground experiences.

## 🌟 Features

### Core Functionality

- **🔐 User Authentication**: Secure sign-in/sign-up with Firebase Auth and Google Sign-In
- **🛒 Smart Cart System**: Add learning plans to cart with trial periods and course access
- **📚 Interactive Courses**: 9 comprehensive AI agent courses with detailed content
- **🎮 Live Playground**: Test AI agents in real-time with Ollama integration
- **📱 Modern UI**: Material3 design with smooth animations and responsive layout

### Learning Plans

- **Free Plan**: Access to 3 AI agents with basic features
- **Pro Plan**: Full access to all agents with advanced features ($9.99/month)
- **Enterprise Plan**: Team collaboration and premium features ($29.99/month)
- **Trial Access**: 14-30 day free trials for premium plans

### AI Agent Courses

1. **Multi-Agent Systems with Memory** (CrewAI)
2. **Foundations of Prompt Engineering** (AWS)
3. **Introduction to LangGraph**
4. **Large Language Model Agents MOOC**
5. **Building AI Agents in LangGraph**
6. **Building RAG Agents with LLMs**
7. **AI Agentic Design Patterns with AutoGen**
8. **LLMs as Operating Systems: Agent Memory**
9. **Building Agentic RAG with LlamaIndex**

## 🏗️ Architecture

This app follows the **MVVM (Model-View-ViewModel)** architecture pattern:

- **Model**: Data classes and repositories for data management
- **View**: Jetpack Compose UI components with Material3 design
- **ViewModel**: Business logic and reactive state management

### Key Components

- **Authentication System**: Firebase Auth with Google Sign-In integration
- **Cart Management**: Reactive cart system with user-specific persistence
- **Course Content**: Structured course data with agent configurations
- **Playground Integration**: Real-time AI agent testing with Ollama

## 🛠️ Technologies Used

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with ViewModels and LiveData/Flow
- **Backend**: Firebase (Auth, Firestore, Analytics)
- **Async Programming**: Kotlin Coroutines and Flow
- **Dependency Injection**: Manual DI with ViewModel factories
- **Local AI**: Ollama integration for offline AI testing
- **Build System**: Gradle with Kotlin DSL

## 📱 Screenshots & UI

### Key Screens

- **Splash Screen**: Welcome experience with branding
- **Home Screen**: Course overview with pricing plans and cart integration
- **Authentication**: Sign-in/Sign-up with Google and email options
- **Cart**: Shopping cart with trial access and course previews
- **Main Dashboard**: Course grid with interactive agent cards
- **Playground**: Real-time AI agent testing interface
- **Profile**: User account management and progress tracking

### Design System

- **Primary Colors**: Deep tech blues with vibrant yellow/gold accents
- **Typography**: Clear hierarchy for learning content
- **Components**: Card-based layouts with smooth transitions
- **Animations**: Subtle fades, slides, and hover effects

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Arctic Fox (2020.3.1) or later
- **JDK**: Version 11 or higher
- **Android SDK**: API 24+ (Android 7.0)
- **Firebase Project**: For authentication and backend services

### Installation

1. **Clone the Repository**

   ```bash
   git clone https://github.com/sisovin/aiplayground-kotlin.git
   cd aiplayground-kotlin
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory and select it

3. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication with Email/Password and Google providers
   - Add your `google-services.json` to the `app/` directory
   - Configure Firestore database if needed

4. **Build and Run**
   - Sync project with Gradle files
   - Build the project (Build → Make Project)
   - Run on emulator or physical device (Run → Run 'app')

### Ollama Integration (Optional)

For offline AI testing in the playground:

1. **Install Ollama**

   ```bash
   # macOS/Linux
   curl https://ollama.ai/install.sh | sh

   # Windows (WSL)
   curl https://ollama.ai/install.sh | bash
   ```

2. **Pull Models**

   ```bash
   ollama pull llama2
   ollama pull mistral
   ollama pull codellama
   ```

3. **Start Ollama Server**

   ```bash
   ollama serve
   ```

The playground will automatically connect to `http://localhost:11434` when available.

## 📁 Project Structure

```
app/src/main/java/com/playapp/aiagents/
├── data/
│   ├── model/           # Data classes (Agent, Cart, PricePlan, etc.)
│   └── repository/      # Data access layer (AgentRepository, CartRepository)
├── ui/                  # UI layer
│   ├── auth/           # Authentication screens (SigninActivity, SignupActivity)
│   ├── cart/           # Cart functionality (CartActivity, CartViewModel)
│   ├── home/           # Home screen with pricing (HomeActivity)
│   ├── main/           # Main dashboard (MainActivity, DashboardScreen)
│   ├── playground/     # AI playground (PlaygroundActivity)
│   ├── profile/        # User profile (ProfileActivity)
│   ├── settings/       # App settings (SettingsActivity)
│   ├── notifications/  # Notifications (NotificationsActivity)
│   ├── courses/        # Course browsing (CoursesActivity)
│   ├── detail/         # Course details (CourseDetailActivity)
│   ├── splash/         # Splash screen (SplashActivity)
│   └── viewmodel/      # Shared ViewModels (AgentViewModel, CartViewModel)
└── utils/              # Utility classes and helpers
```

## 🔧 Configuration

### Firebase Configuration

Update `app/build.gradle` with your Firebase configuration:

```gradle
dependencies {
    // Firebase
    implementation 'com.google.firebase:firebase-auth:21.1.0'
    implementation 'com.google.firebase:firebase-firestore:24.1.2'
    implementation 'com.google.android.gms:play-services-auth:20.4.1'
    // ... other dependencies
}
```

### Ollama Configuration

The app automatically detects Ollama at `localhost:11434`. To customize:

```kotlin
// In PlaygroundActivity or relevant ViewModel
private const val OLLAMA_BASE_URL = "http://your-ollama-host:11434"
```

## 🎯 Usage

### For Learners

1. **Sign Up**: Create an account or sign in with Google
2. **Browse Courses**: Explore the 9 AI agent courses on the main dashboard
3. **Choose Plan**: Select Free, Pro, or Enterprise plan from the home screen
4. **Add to Cart**: Add your chosen plan to cart with trial access
5. **Start Learning**: Access courses and test agents in the playground

### For Developers

1. **Clone & Setup**: Follow installation steps above
2. **Firebase Config**: Set up authentication and database
3. **Ollama Setup**: Install local AI models for testing
4. **Build & Test**: Run on emulator/device and test all features
5. **Customize**: Modify course content, pricing, or add new agents

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Guidelines

- Follow Kotlin coding standards
- Use MVVM architecture for new features
- Add tests for business logic
- Update documentation for API changes
- Ensure Material3 design consistency

## 📋 Roadmap

### Version 1.1 (Next Release)

- [ ] Enhanced playground with model switching
- [ ] Course progress tracking
- [ ] Offline course downloads
- [ ] Advanced user analytics

### Future Versions

- [ ] Multi-language support
- [ ] Social learning features
- [ ] Advanced AI agent configurations
- [ ] Integration with popular AI frameworks
- [ ] Cloud sync for progress and settings

## 🐛 Troubleshooting

### Common Issues

**Build Failures**

- Ensure JDK 11+ is installed and configured in Android Studio
- Check that all Firebase dependencies are correctly added
- Verify `google-services.json` is in the correct location

**Authentication Issues**

- Confirm Firebase project is properly configured
- Check Google Sign-In SHA-1 fingerprint in Firebase console
- Verify internet connection for authentication

**Ollama Connection**

- Ensure Ollama is running: `ollama serve`
- Check that models are pulled: `ollama ls`
- Verify firewall allows local connections on port 11434

**UI Issues**

- Test on different screen sizes and orientations
- Check Material3 theme configuration
- Verify Compose version compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Ollama**: For providing local AI model hosting
- **Firebase**: For backend services and authentication
- **Material Design**: For UI design system inspiration
- **Android Jetpack**: For modern Android development tools

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/sisovin/aiplayground-kotlin/issues)
- **Discussions**: [GitHub Discussions](https://github.com/sisovin/aiplayground-kotlin/discussions)
- **Email**: For business inquiries or support

---

**Built with ❤️ for AI enthusiasts and developers worldwide**

*Transform your AI learning journey with hands-on experience and real-time experimentation.*

1. **Prerequisites**:

   - Android Studio Arctic Fox or later- **Jetpack Compose**: Modern UI toolkit for Android

   - JDK 11 or higher

   - Android SDK API 24+- **Firebase**: Backend services (Analytics, Firestore, Auth)## Project Vision

2. **Clone and Open**:- **MVVM**: Architecture pattern

   - Open the project in Android Studio

   - Sync Gradle files- **Coroutines & Flow**: Asynchronous programmingA modern, responsive learning platform where users can:

3. **Firebase Setup**:- Explore 9 different AI Agent types through interactive tab cards

   - Add your `google-services.json` to `app/` directory

   - Configure Firebase project in Firebase Console## Project Structure- Learn to build each agent with step-by-step guidance

4. **Build and Run**:- Test agents locally using Ollama (no internet required)

   - Build the project

   - Run on emulator or device```- See real-time demonstrations of each agent's capabilities

## Package Nameapp/

`com.playapp.aiagents`├── src/main/java/com/playapp/aiagents/This project is both a learning site and a developer playground — perfect for engineers, researchers, and learners who want hands-on experience building agentic systems locally.

## App Icon│   ├── data/

Please provide your own 1024x1024 app icon and place it in the appropriate drawable folders.│   │   ├── model/          # Data classes---

## Courses│   │   └── repository/     # Data access layer

The app features 9 AI agent courses:│   └── ui/## Planned Features for V1

1. LLMs as Operating Systems: Agent Memory│       ├── splash/         # Splash screen

2. Foundations of Prompt Engineering (AWS)

3. Introduction to LangGraph│       ├── main/           # Main dashboard1. Interactive Tab Navigation — 9 cards representing different AI agents

4. Large Language Model Agents MOOC

5. Building AI Agents in LangGraph│       ├── detail/         # Course details2. Agent Playground — Live testing interface for each agent

6. Building RAG Agents with LLMs

7. AI Agentic Design Patterns with AutoGen│       └── cart/           # Shopping cart3. Learning Modules — Step-by-step tutorials for building agents

8. LLMs as Operating Systems: Agent Memory (Advanced)

9. Building Agentic RAG with LlamaIndex└── build.gradle            # App-level dependencies4. Ollama Integration — Local LLM connection (no API keys needed)

## Contributing```5. Code Examples — Reusable snippets for each agent type

1. Fork the repository6. Responsive Design — Works on mobile, tablet, and desktop

2. Create a feature branch

3. Make your changes## Setup Instructions

4. Test thoroughly

5. Submit a pull request---

## License1. **Prerequisites**

This project is licensed under the MIT License - see the LICENSE file for details.   - Android Studio Arctic Fox or later## Design Approach

- JDK 11 or higher

- Android SDK API 24+- Colors: Deep tech blues with a vibrant yellow/gold accent

- Style: Modern, clean, subtle gradients, and smooth animations

2. **Clone and Open**:- Typography: Clear hierarchy for learning content (headings, subheads, code)

   - Open the project in Android Studio- Components: Card-based layout with smooth transitions and badges

   - Sync Gradle files

Primary palette (suggested)

3. **Firebase Setup**:- Header / Primary Accent: #F4C542 (vibrant yellow/gold)

   - Add your `google-services.json` to `app/` directory- Cards Background: #E8F4F8 (soft blue-gray)

   - Configure Firebase project in Firebase Console- Primary Text: #1E40AF (deep blue)

- Accent Colors: Varied per agent (use distinct hues for easy scanning)

4. **Build and Run**:

   - Build the projectAnimations & UX

   - Run on emulator or device- Hover elevation on cards

- Smooth tab transitions (fade + slide)

## Package Name- Streaming response rendering in playground (like a terminal / chat stream)

`com.playapp.aiagents`---

## App Icon## What I'll Build (UI & UX)

Please provide your own 1024x1024 app icon and place it in the appropriate drawable folders.Design Inspiration:

- Bold yellow header banner with high contrast

## Courses- 3x3 grid of interactive course cards (responsive)

- Each card with course preview, provider, duration, and instructor

The app features 9 AI agent courses:- Light, airy backgrounds with numbered badges

- Clean, professional layout and accessible color contrast

1. LLMs as Operating Systems: Agent Memory

2. Foundations of Prompt Engineering (AWS)Features:

3. Introduction to LangGraph1. 9 Interactive Course Cards — Numbered badges, provider info, duration, instructor

4. Large Language Model Agents MOOC2. Playground Interface — Click any card to test the agent with Ollama

5. Building AI Agents in LangGraph3. Ollama Integration — Local LLM connection (no API keys)

6. Building RAG Agents with LLMs4. Responsive Grid — Adapts to mobile, tablet, and desktop

7. AI Agentic Design Patterns with AutoGen5. Learning Resources — Course details and tutorials for each agent

8. LLMs as Operating Systems: Agent Memory (Advanced)

9. Building Agentic RAG with LlamaIndex---

## Contributing## The 9 AI Agents

1. Fork the repository1. Multi-Agent Systems with Memory (CrewAI)  

2. Create a feature branch2. Prompt Engineering (AWS)  

3. Make your changes3. LangGraph Introduction  

4. Test thoroughly4. LLM Agent Fundamentals  

5. Submit a pull request5. Building LangGraph Agents  

6. RAG with NVIDIA  

## License7. AutoGen Design Patterns  

8. Agent Memory Systems  

This project is licensed under the MIT License - see the LICENSE file for details.9. LlamaIndex Agentic RAG

Each agent card will include:

- Agent name & short description
- Provider / inspiration
- Duration / difficulty
- Instructor or author
- Model badge (which Ollama model is recommended)

---

## Quick Start — Using with Ollama (Local LLMs)

Prerequisites:

- macOS / Linux (or WSL on Windows)
- Node.js (v16+ recommended) and npm / pnpm / yarn
- Ollama (local LLM runner)

Install Ollama

```bash
curl https://ollama.ai/install.sh | sh
```

Pull a model (example)

```bash
ollama pull llama2
```

Serve Ollama locally

```bash
ollama serve
```

Now open the playground in your browser (after starting the dev server — see Developer Setup) and click any course card to test the agent locally. The playground will connect to Ollama at the default endpoint (<http://localhost:11434> or as configured).

Notes:

- Ollama runs locally and does not require Internet-based API keys once a model is installed.
- Depending on the model you pull, you may need more disk space and RAM.

---

## Model Mapping & Configuration

This playground will support multiple Ollama models and allow per-agent model suggestions and a runtime model switcher.

Suggested default mapping (examples):

- Multi-Agent Systems — neural-chat
- Prompt Engineering — codellama
- LangGraph Introduction — mistral
- LLM Agent Fundamentals — llama2
- Building LangGraph Agents — codellama
- RAG with NVIDIA — llama2
- AutoGen Design Patterns — neural-chat
- Agent Memory Systems — mistral
- LlamaIndex Agentic RAG — llama2

Example model configuration (JSON)

```json
{
  "agents": [
    { "id": "multi-agent-memory", "name": "Multi-Agent Systems with Memory", "recommendedModel": "neural-chat" },
    { "id": "prompt-engineering", "name": "Prompt Engineering", "recommendedModel": "codellama" }
    // ...
  ],
  "models": ["llama2", "mistral", "codellama", "neural-chat"]
}
```

Model Switcher

- The UI will provide a model selector inside the playground so users can switch models on the fly for experimentation.

Streaming Mode

- Use streaming API (Ollama supports streaming-like behavior) to render token-by-token responses in the chat area for a real-time feel.

---

## Playground Usage

1. Start Ollama and ensure at least one model is available.
2. Run the local dev server (see below).
3. Open the app and browse the 3x3 course grid.
4. Click a course card to open the playground for that agent.
5. Type a prompt or choose a sample prompt and run it.
6. Observe the streamed response and any agent-specific multi-step behavior.

Sample prompts (per-agent examples)

- Multi-Agent Systems: "Design a 3-agent workflow to analyze product feedback and summarize key issues."
- Prompt Engineering: "Rewrite this prompt to be more deterministic and concise."
- RAG: "Retrieve information from the sample docs and produce an answer with source citations."

Save Chat History

- The playground will support local (browser) saving of chat history and downloadable session transcripts (JSON/MD).
- Future: user account-based saves with sync (Lovable Cloud or similar).

---

## Developer Setup & Local Development

Clone

```bash
git clone https://github.com/sisovin/tabcard-ai-play.git
cd tabcard-ai-play
```

Install dependencies

```bash
# using npm
npm install

# or pnpm
pnpm install

# or yarn
yarn
```

Environment

- No remote API keys are required for basic functionality when using Ollama locally. Optionally, provide a REACT_APP_OLLAMA_URL or similar env var to target a non-default Ollama host.

Start dev server

```bash
npm run dev
# or
pnpm dev
# or
yarn dev
```

Build for production

```bash
npm run build
```

Run tests (if present)

```bash
npm test
```

Local config example (.env.local)

```env
REACT_APP_OLLAMA_URL=http://localhost:11434
REACT_APP_DEFAULT_MODEL=llama2
```

Note: adapt variable names to your framework (Next.js, Vite, CRA, etc.)

---

## Project Structure (suggested)

- /src
  - /components — UI components (Card, Playground, Header, ModelSwitcher)
  - /pages — routes / views
  - /styles — CSS / Tailwind / tokens
  - /lib — Ollama client, helpers
  - /data — agent metadata and sample prompts
- /public — assets and images
- README.md — this file
- package.json — scripts & deps

---

## Design System & Theming

- Primary: #F4C542 (vibrant yellow/gold)
- Card background: #E8F4F8
- Primary text: #1E40AF
- Accents: A palette per agent for quick identification

Accessibility:

- Large hit targets for cards
- Sufficient contrast (AA / ideally AAA for headings)
- Keyboard navigation for selecting cards & operating the playground

Animations:

- Smooth hover/active states
- Subtle fades for modal/playground open/close

---

## Roadmap & What's Next

- Add More Models: Configure different Ollama models for each agent type
- Save Progress: Add user progress tracking with Lovable Cloud or similar
- Code Examples: Include downloadable starter code & repo templates for each agent
- Video Tutorials: Embed course videos in the playground
- Authentication & Profiles: Save user progress across devices
- Example Notebooks: Provide runnable examples (Colab / local notebooks)
- End-to-end tests and CI

---

## Contributing

Contributions, issues, and feature requests are welcome!

- Fork the repo
- Create a feature branch (feature/your-feature)
- Open a PR against main with a clear description

Please follow standard PR etiquette:

- Build & test locally
- Keep changes scoped
- Include screenshots or gifs for UI changes

---

## Troubleshooting

Ollama not reachable?

- Ensure `ollama serve` is running.
- Verify correct model is pulled: `ollama ls` or `ollama pull <model>`.
- Confirm the URL: default is <http://localhost:11434>. Set REACT_APP_OLLAMA_URL if needed.

Model errors or out-of-memory?

- Choose a smaller model or ensure your machine meets memory requirements.
- Use swap or a cloud-hosted Ollama instance for heavy models.

Streaming isn't working?

- Ensure the client is using a streaming-compatible endpoint or websocket and the model supports streaming tokens.

---

## What's Included (Summary)

✅ 9 Interactive Course Cards (3x3 grid)  
✅ Ollama Integration — local model testing (no API keys)  
✅ Responsive Design — mobile, tablet, desktop  
✅ Modern UI — yellow header banner, card grid, animations  
✅ Agent Playground — interactive chat interface per agent  
✅ Model Switcher, Streaming Mode, Sample Prompts, Save Chat History (planned / in-progress)

---

## License & Acknowledgements

This project is open-source. Please add an appropriate license (MIT, Apache-2.0, etc.) in a LICENSE file.

Acknowledgements:

- Ollama — for local LLM hosting and easy local model usage
- Design inspiration and course topics from CrewAI, AWS, LangGraph, NVIDIA, LlamaIndex, and AutoGen patterns

---

If you'd like, I can:

- Create starter UI components and a minimal working playground that connects to Ollama
- Scaffold sample agent configurations and sample prompts
- Add a demo configuration for streaming responses and a model switcher
- Produce downloadable starter code for each of the 9 agents

Happy to generate code scaffolding, example components, or a PR with the first UI and Ollama client integration next. Which would you like me to do first?
