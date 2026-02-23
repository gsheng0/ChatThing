# Roadmap

## Planned Features

### Scheduling Agent
A new capability that monitors one or more group chats and automatically creates Google Calendar events when scheduling-related messages appear.

- Listens to group chats (configured via `tasks.capabilities` in `application.yaml`, same as the expenses agent)
- Uses `GoogleCalendar` (already implemented) to create/update/delete events
- Needs a new tool set annotated with `@AiAgentTool` for calendar operations (e.g. `CreateEvent`, `UpdateEvent`, `DeleteEvent`, `GetEvents`)
- Needs a new system prompt focused on identifying and parsing scheduling intent from natural conversation

### Budgeting Agent
A personal agent that interacts only with the owner (not a group chat), providing expense analysis and summaries from the Google Sheets ledger.

- Reads from the existing expense spreadsheet via `GoogleSheets`
- Responds to direct messages from the owner only (filtered by sender ID in `tasks.capabilities.chats`)
- Capabilities to implement:
  - Expense summaries by time range (day / week / month / custom)
  - Breakdown by category (requires adding a category column or a separate sheet to the spreadsheet)
  - Trend analysis (spending over time)
  - Comparisons (this month vs last month, etc.)
- Likely read-only — no writes to the spreadsheet, just analysis

---

## Architecture Notes (for future contributors)

### Multi-capability system
Each entry under `tasks.capabilities` in `application.yaml` is an independent agent with its own AI provider, prompt, tool set, and list of monitored chats. Adding a new agent means adding a new capability block — no code changes needed in `TaskRunner`.

### Tool discovery
Tool classes are annotated with `@AiAgentTool` (production) or `@AiAgentTestTool` (test mode). These annotations are Spring stereotype annotations, so tool classes are Spring-managed beans with constructor-injected dependencies. `ToolHolder` discovers them via `ApplicationContext.getBeansWithAnnotation()`, lazily on first use.

### `@Lazy` is load-bearing
`GoogleSheets`, `GoogleCalendar`, and `Contact` are all `@Lazy` because their constructors do network I/O (OAuth + Google API calls). Tool beans (`@AiAgentTool`, `@AiAgentTestTool`) are also lazy so they don't force `GoogleSheets`/`Contact` to initialize at Spring context startup. `MessagePoller` uses `@Lazy` on its `Contact` constructor parameter specifically to get a CGLIB proxy — this avoids the eager initialization chain at startup while still resolving sender IDs at runtime.

### `Contact` exposes data via methods, not fields
`Contact.getNameToColMap()` and `Contact.getIdToNameMap()` must remain methods (not public fields) because `Contact` is injected as a CGLIB lazy proxy in some places. CGLIB can only intercept method calls, not field access — public fields on a lazy proxy are always `null`.
