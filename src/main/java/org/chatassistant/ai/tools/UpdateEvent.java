package org.chatassistant.ai.tools;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.chatassistant.google.GoogleCalendar;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.ai.tools.annotation.ToolMethod;
import org.chatassistant.config.GoogleApiConfigurationProperties;

import java.time.LocalDateTime;
import java.time.ZoneId;

@AiAgentTool
public class UpdateEvent {
    private final GoogleCalendar calendar;
    private final GoogleApiConfigurationProperties config;

    public UpdateEvent(final GoogleCalendar calendar, final GoogleApiConfigurationProperties config) {
        this.calendar = calendar;
        this.config = config;
    }

    /**
     * Updates an existing calendar event. Pass an empty string for any field to keep its current value.
     * @param eventId the ID of the event to update (obtained from getEvents)
     * @param title new title, or empty string to keep existing
     * @param startTime new start datetime in ISO 8601, or empty string to keep existing
     * @param endTime new end datetime in ISO 8601, or empty string to keep existing
     * @param description new description, or empty string to keep existing
     * @param location new location, or empty string to keep existing
     * @return empty string on success, or an error message
     */
    @ToolMethod
    public String updateEvent(String eventId, String title, String startTime, String endTime,
                              String description, String location) {
        try {
            String calendarId = config.getCalendar().getCalendarId();
            Event event = calendar.getEvent(calendarId, eventId);

            if (!title.isEmpty()) event.setSummary(title);
            if (!description.isEmpty()) event.setDescription(description);
            if (!location.isEmpty()) event.setLocation(location);

            ZoneId zone = ZoneId.of(config.getCalendar().getTimeZone());
            if (!startTime.isEmpty()) {
                long millis = LocalDateTime.parse(startTime).atZone(zone).toInstant().toEpochMilli();
                event.setStart(new EventDateTime().setDateTime(new DateTime(millis)));
            }
            if (!endTime.isEmpty()) {
                long millis = LocalDateTime.parse(endTime).atZone(zone).toInstant().toEpochMilli();
                event.setEnd(new EventDateTime().setDateTime(new DateTime(millis)));
            }

            calendar.updateEvent(calendarId, eventId, event);
            return "";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
