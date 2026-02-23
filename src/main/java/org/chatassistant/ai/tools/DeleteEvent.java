package org.chatassistant.ai.tools;

import org.chatassistant.GoogleCalendar;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.config.GoogleApiConfigurationProperties;

@AiAgentTool
public class DeleteEvent {
    private final GoogleCalendar calendar;
    private final GoogleApiConfigurationProperties config;

    public DeleteEvent(final GoogleCalendar calendar, final GoogleApiConfigurationProperties config) {
        this.calendar = calendar;
        this.config = config;
    }

    /**
     * Deletes a calendar event.
     * @param eventId the ID of the event to delete (obtained from getEvents)
     * @return empty string on success, or an error message
     */
    public String deleteEvent(String eventId) {
        try {
            calendar.deleteEvent(config.getCalendar().getCalendarId(), eventId);
            return "";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
