package org.chatassistant.ai.tools;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import org.chatassistant.google.GoogleCalendar;
import org.chatassistant.ai.tools.annotation.AiAgentTool;
import org.chatassistant.ai.tools.annotation.ToolMethod;
import org.chatassistant.config.GoogleApiConfigurationProperties;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AiAgentTool
public class GetEvents {
    private final GoogleCalendar calendar;
    private final GoogleApiConfigurationProperties config;

    public GetEvents(final GoogleCalendar calendar, final GoogleApiConfigurationProperties config) {
        this.calendar = calendar;
        this.config = config;
    }

    /**
     * Returns calendar events within the given time range.
     * @param startTime range start in ISO 8601 format, e.g. "2026-03-01T00:00:00"
     * @param endTime range end in ISO 8601 format, e.g. "2026-03-07T23:59:59"
     * @return list of events, each with fields: id, title, start, end, location, description
     */
    @ToolMethod
    public List<Map<String, String>> getEvents(String startTime, String endTime) {
        ZoneId zone = ZoneId.of(config.getCalendar().getTimeZone());
        long startMillis = LocalDateTime.parse(startTime).atZone(zone).toInstant().toEpochMilli();
        long endMillis = LocalDateTime.parse(endTime).atZone(zone).toInstant().toEpochMilli();

        List<Event> events = calendar.getEventsInRange(
                config.getCalendar().getCalendarId(),
                new DateTime(startMillis),
                new DateTime(endMillis));

        List<Map<String, String>> result = new ArrayList<>();
        for (Event event : events) {
            Map<String, String> m = new HashMap<>();
            m.put("id", event.getId() != null ? event.getId() : "");
            m.put("title", event.getSummary() != null ? event.getSummary() : "");
            m.put("start", event.getStart() != null && event.getStart().getDateTime() != null
                    ? event.getStart().getDateTime().toString() : "");
            m.put("end", event.getEnd() != null && event.getEnd().getDateTime() != null
                    ? event.getEnd().getDateTime().toString() : "");
            m.put("location", event.getLocation() != null ? event.getLocation() : "");
            m.put("description", event.getDescription() != null ? event.getDescription() : "");
            result.add(m);
        }
        return result;
    }
}
