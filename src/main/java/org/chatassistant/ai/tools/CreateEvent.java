package org.chatassistant.ai.tools;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.chatassistant.google.GoogleCalendar;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.ai.tools.annotation.ToolMethod;
import org.chatassistant.config.GoogleApiConfigurationProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@AiAgentTool
public class CreateEvent {
    private final GoogleCalendar calendar;
    private final GoogleApiConfigurationProperties config;

    public CreateEvent(final GoogleCalendar calendar, final GoogleApiConfigurationProperties config) {
        this.calendar = calendar;
        this.config = config;
    }

    /**
     * Creates a new Google Calendar event. Supports both timed and all-day events.
     * For timed events, pass startTime/endTime in ISO 8601 datetime format: "2026-03-01T14:00:00"
     * For all-day events (time TBD), pass startTime/endTime as date-only: "2026-03-01"
     *   — for a single all-day event, endTime should be the next day, e.g. "2026-03-02"
     * @param title the title/summary of the event
     * @param startTime start in "YYYY-MM-DD" (all-day) or "YYYY-MM-DDTHH:MM:SS" (timed)
     * @param endTime end in "YYYY-MM-DD" (all-day, exclusive) or "YYYY-MM-DDTHH:MM:SS" (timed)
     * @param description optional event description; pass empty string to omit
     * @param location optional location; pass empty string to omit
     * @return the created event ID on success, or an error message prefixed with "Error:"
     */
    @ToolMethod
    public String createEvent(String title, String startTime, String endTime,
                              String description, String location) {
        try {
            Event event = new Event().setSummary(title);

            if (startTime.contains("T")) {
                ZoneId zone = ZoneId.of(config.getCalendar().getTimeZone());
                long startMillis = LocalDateTime.parse(startTime).atZone(zone).toInstant().toEpochMilli();
                long endMillis = LocalDateTime.parse(endTime).atZone(zone).toInstant().toEpochMilli();
                event.setStart(new EventDateTime().setDateTime(new DateTime(startMillis)));
                event.setEnd(new EventDateTime().setDateTime(new DateTime(endMillis)));
            } else {
                event.setStart(new EventDateTime().setDate(new DateTime(startTime)));
                event.setEnd(new EventDateTime().setDate(new DateTime(endTime)));
            }

            if (!description.isEmpty()) event.setDescription(description);
            if (!location.isEmpty()) event.setLocation(location);

            Event created = calendar.createEvent(config.getCalendar().getCalendarId(), event);
            return created.getId();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
