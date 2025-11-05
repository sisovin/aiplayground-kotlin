# Aiagents Playapp# Aiagents Playapp# 9 AI Agents Playground Project

An Android application built with Kotlin and Jetpack Compose that provides interactive learning courses for building AI agents.

## FeaturesAn Android application built with Kotlin and Jetpack Compose that provides interactive learning courses for building AI agents.Here's a comprehensive playground for 9 AI Agents with Ollama integration! This is a comprehensive AI Agent learning platform with 9 courses. Let me create a modern, interactive playground based on this design with Ollama integration

- **Splash Screen**: Welcome screen on app launch

- **Dashboard**: Main screen with course grid and top section

- **Course Cards**: 9 interactive course cards similar to the web version## Features---

- **Course Details**: Detailed view for each course

- **Cart**: Shopping cart functionality

- **Bottom Navigation**: Easy navigation between sections

- **Splash Screen**: Welcome screen on app launchTable of Contents

## Architecture

- **Dashboard**: Main screen with course grid and top section- [Project Vision](#project-vision)

This app follows the MVVM (Model-View-ViewModel) architecture pattern:

- **Course Cards**: 9 interactive course cards similar to the web version- [Planned Features for V1](#planned-features-for-v1)

- **Model**: Data classes and repositories

- **View**: Jetpack Compose UI components- **Course Details**: Detailed view for each course- [Design Approach](#design-approach)

- **ViewModel**: Business logic and state management

- **Cart**: Shopping cart functionality- [What I'll Build (UI & UX)](#what-ill-build-ui--ux)

## Technologies Used

- **Bottom Navigation**: Easy navigation between sections- [The 9 AI Agents](#the-9-ai-agents)

- **Kotlin**: Primary programming language

- **Jetpack Compose**: Modern UI toolkit for Android- [Quick Start — Using with Ollama (Local LLMs)](#quick-start--using-with-ollama-local-llms)

- **Firebase**: Backend services (Analytics, Firestore, Auth)

- **MVVM**: Architecture pattern## Architecture- [Model Mapping & Configuration](#model-mapping--configuration)

- **Coroutines & Flow**: Asynchronous programming

- [Playground Usage](#playground-usage)

## Project Structure

This app follows the MVVM (Model-View-ViewModel) architecture pattern:- [Developer Setup & Local Development](#developer-setup--local-development)

```

app/- [Project Structure (suggested)](#project-structure-suggested)

├── src/main/java/com/playapp/aiagents/

│   ├── data/- **Model**: Data classes and repositories- [Design System & Theming](#design-system--theming)

│   │   ├── model/          # Data classes

│   │   └── repository/     # Data access layer- **View**: Jetpack Compose UI components- [Roadmap & What's Next](#roadmap--whats-next)

│   └── ui/

│       ├── splash/         # Splash screen- **ViewModel**: Business logic and state management- [Contributing](#contributing)

│       ├── main/           # Main dashboard

│       ├── detail/         # Course details- [Troubleshooting](#troubleshooting)

│       └── cart/           # Shopping cart

└── build.gradle            # App-level dependencies## Technologies Used- [License & Acknowledgements](#license--acknowledgements)

```

## Setup Instructions

- **Kotlin**: Primary programming language---

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
