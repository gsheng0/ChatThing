# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew build          # Build the project
./gradlew test           # Run tests
./gradlew bootRun        # Run the Spring Boot application
./gradlew run            # Alternative run command
```

The JVM is configured with `-Djava.library.path=/opt/homebrew/lib` and `--enable-native-access=ALL-UNNAMED` for JEP (Java Embedded Python) and native library support.

## What This Project Does

An AI-powered expense tracking assistant for group chats. It monitors a macOS Messages.app group chat, interprets expense-related messages using Google Gemini, and manages a shared expense ledger in Google Sheets. Responses are sent back to the group chat via AppleScript.

## Architecture

### Threading Model

Three async threads managed by `TaskRunner` communicate via `BlockingDeque`s:

- **`PollingTask`** — polls `MessageDB` (Python subprocess) for new messages every 1–5s, pushes batches to a queue
- **`ProcessingTask`** — consumes message batches, runs them through AI agents, invokes tools
- **`LoggingTask`** — consumes log entries from a queue, writes to timestamped files

### AI Agent Layer (`org.chatassistant.ai.agent`)

- `AiAgent` interface: `ask()` and `kill()`
- `GeminiAgent` — image parsing; multi-turn conversation with tool invocation
- `AgenticGeminiAgent` — chat-based agent using persistent Gemini Chat sessions for ongoing expense tracking conversations

Tools are discovered via reflection using `@AiAgentTool` / `@AiAgentTestTool` annotations. `ToolHolder` dynamically loads annotated methods from a configurable tool set.

### Tools (`org.chatassistant.ai.tools`)

Each tool class has methods annotated with `@AiAgentTool`. Active tools:
- `RecordExpense`, `RecordPayment`, `RecordSplitExpense` — write to Google Sheets
- `SendTextMessage` — sends iMessage via AppleScript
- `GetSummary`, `Settle` — read expense state and compute settlements

### Data Layer

- `MessageDB` — singleton that spawns `decode.py` as a Python subprocess (via JEP) to read macOS Messages SQLite DB
- `GoogleSheets` — singleton managing OAuth and the expense spreadsheet (ID hardcoded in class)
- `Contact` — maps user phone/IDs to names and spreadsheet columns

### Configuration (`application.yaml`)

```yaml
agent:
  prompt-path: src/main/resources/expenseTrackingPrompt2
  model-name: gemini-3-flash-preview
  realToolSet: true   # false uses @AiAgentTestTool instead of @AiAgentTool
logger:
  output-folder: dev
```

`AiAgentConfigurationProperties` and `LoggingConfigurationProperties` are the Spring config property classes.

### External Requirements

- `credentials.json` in project root for Google Sheets OAuth
- ImageMagick at `/opt/homebrew/lib` for HEIC→JPEG conversion (via im4java)
- macOS with Messages.app access for the Python subprocess and AppleScript sending
- JEP 4.2.0 native library must be on the Java library path
