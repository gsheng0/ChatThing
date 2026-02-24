package org.chatassistant.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.chatassistant.config.GoogleApiConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Lazy
@Component
public class GoogleCalendar {
    private final Calendar calendar;
    private final JsonFactory jsonFactory;

    public GoogleCalendar(final GoogleApiConfigurationProperties config) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            jsonFactory = GsonFactory.getDefaultInstance();
            calendar = new Calendar.Builder(httpTransport, jsonFactory, getCredentials(httpTransport, config))
                    .setApplicationName("Google Calendar API")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Event> getUpcomingEvents(String calendarId, int maxResults) {
        try {
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = calendar.events().list(calendarId)
                    .setMaxResults(maxResults)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            return events.getItems();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving events: " + e.getMessage(), e);
        }
    }

    public Event createEvent(String calendarId, Event event) {
        try {
            return calendar.events().insert(calendarId, event).execute();
        } catch (Exception e) {
            throw new RuntimeException("Error creating event: " + e.getMessage(), e);
        }
    }

    public void deleteEvent(String calendarId, String eventId) {
        try {
            calendar.events().delete(calendarId, eventId).execute();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting event " + eventId + ": " + e.getMessage(), e);
        }
    }

    public Event updateEvent(String calendarId, String eventId, Event event) {
        try {
            return calendar.events().update(calendarId, eventId, event).execute();
        } catch (Exception e) {
            throw new RuntimeException("Error updating event " + eventId + ": " + e.getMessage(), e);
        }
    }

    public List<Event> getEventsInRange(String calendarId, DateTime timeMin, DateTime timeMax) {
        try {
            Events events = calendar.events().list(calendarId)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            return events.getItems();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving events in range: " + e.getMessage(), e);
        }
    }

    public Event getEvent(String calendarId, String eventId) {
        try {
            return calendar.events().get(calendarId, eventId).execute();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving event " + eventId + ": " + e.getMessage(), e);
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, final GoogleApiConfigurationProperties config) throws IOException {
        try (final FileInputStream in = new FileInputStream(config.getCredentialsPath())) {
            final GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

            final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, jsonFactory, clientSecrets, List.of(CalendarScopes.CALENDAR))
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(config.getCalendar().getTokensDir())))
                    .setAccessType("offline")
                    .build();
            final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(config.getCalendar().getOauthPort()).build();
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }
    }
}
