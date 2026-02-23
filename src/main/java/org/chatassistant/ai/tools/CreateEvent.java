package org.chatassistant.ai.tools;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.chatassistant.GoogleCalendar;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.config.GoogleApiConfigurationProperties;

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
     * Creates a new Google Calendar event.
     * @param title the title/summary of the event
     * @param startTime start datetime in ISO 8601 format, e.g. "2026-03-01T14:00:00"
     * @param endTime end datetime in ISO 8601 format, e.g. "2026-03-01T15:00:00"
     * @param description optional event description; pass empty string to omit
     * @param location optional location; pass empty string to omit
     * @return the created event ID on success, or an error message prefixed with "Error:"
     */
    public String createEvent(String title, String startTime, String endTime,
                              String description, String location) {
        try {
            ZoneId zone = ZoneId.of(config.getCalendar().getTimeZone());
            long startMillis = LocalDateTime.parse(startTime).atZone(zone).toInstant().toEpochMilli();
            long endMillis = LocalDateTime.parse(endTime).atZone(zone).toInstant().toEpochMilli();

            Event event = new Event().setSummary(title);
            event.setStart(new EventDateTime().setDateTime(new DateTime(startMillis)));
            event.setEnd(new EventDateTime().setDateTime(new DateTime(endMillis)));
            if (!description.isEmpty()) event.setDescription(description);
            if (!location.isEmpty()) event.setLocation(location);

            Event created = calendar.createEvent(config.getCalendar().getCalendarId(), event);
            return created.getId();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
