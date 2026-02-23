package org.chatassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("google")
public class GoogleApiConfigurationProperties {
    private String credentialsPath;
    private String tokensDir;
    private int oauthPort;
    private Sheets sheets = new Sheets();
    private Calendar calendar = new Calendar();

    public String getCredentialsPath() { return credentialsPath; }
    public void setCredentialsPath(String credentialsPath) { this.credentialsPath = credentialsPath; }

    public String getTokensDir() { return tokensDir; }
    public void setTokensDir(String tokensDir) { this.tokensDir = tokensDir; }

    public int getOauthPort() { return oauthPort; }
    public void setOauthPort(int oauthPort) { this.oauthPort = oauthPort; }

    public Sheets getSheets() { return sheets; }
    public void setSheets(Sheets sheets) { this.sheets = sheets; }

    public Calendar getCalendar() { return calendar; }
    public void setCalendar(Calendar calendar) { this.calendar = calendar; }

    public static class Sheets {
        private String spreadsheetId;
        private String expenseSheet;
        private String contactSheet;

        public String getSpreadsheetId() { return spreadsheetId; }
        public void setSpreadsheetId(String spreadsheetId) { this.spreadsheetId = spreadsheetId; }

        public String getExpenseSheet() { return expenseSheet; }
        public void setExpenseSheet(String expenseSheet) { this.expenseSheet = expenseSheet; }

        public String getContactSheet() { return contactSheet; }
        public void setContactSheet(String contactSheet) { this.contactSheet = contactSheet; }
    }

    public static class Calendar {
        private String tokensDir;
        private int oauthPort;
        private String calendarId;
        private String timeZone;

        public String getTokensDir() { return tokensDir; }
        public void setTokensDir(String tokensDir) { this.tokensDir = tokensDir; }

        public int getOauthPort() { return oauthPort; }
        public void setOauthPort(int oauthPort) { this.oauthPort = oauthPort; }

        public String getCalendarId() { return calendarId; }
        public void setCalendarId(String calendarId) { this.calendarId = calendarId; }

        public String getTimeZone() { return timeZone; }
        public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    }
}
