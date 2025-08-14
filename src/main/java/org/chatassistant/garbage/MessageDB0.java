package org.chatassistant.garbage;

import org.chatassistant.entities.Message;
import org.chatassistant.garbage.typedstream.ProcessTypedStreamDecoder;
import org.chatassistant.garbage.typedstream.TypedStreamDecoder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageDB0 {
    private static MessageDB0 instance;
    private static TypedStreamDecoder decoder;
    private static final int TIMESTAMP = 1;
    private static final int SENDER = 2;
    private static final int TEXT = 3;
    private static final int TYPED_STREAM = 4;
    private static Map<String, String> contactMap;
    private static final String PATH = "jdbc:sqlite:/Users/georgesheng/Library/Messages/chat.db"; // PATH to your SQLite DB file
    private static final String NEW_QUERY = new StringBuilder()
        .append("WITH LatestMessageVersions AS (\n")
        .append("    SELECT\n")
        .append("        m.ROWID,\n")
        .append("        m.guid,\n")
        .append("        m.text,\n")
        .append("        m.attributedBody,\n")
        .append("        m.date,\n")
        .append("        m.is_from_me,\n")
        .append("        m.handle_id,\n")
        .append("        COALESCE(m.associated_message_guid, m.guid) AS effective_message_guid,\n")
        .append("        ROW_NUMBER() OVER(PARTITION BY COALESCE(m.associated_message_guid, m.guid) ORDER BY m.date DESC) as rn\n")
        .append("    FROM\n")
        .append("        message m\n")
        .append("    WHERE\n")
        .append("        m.associated_message_type IS NULL\n")
        .append("        OR m.associated_message_type = 0\n")
        .append("        OR m.associated_message_type = 10000\n")
        .append(")\n")
        .append("SELECT\n")
        .append("    lmv.text AS message_content,\n")
        .append("    lmv.attributedBody AS attributed_body_hex,\n")
        .append("    datetime(lmv.date + 978307200, 'unixepoch', 'localtime') AS message_timestamp,\n")
        .append("    CASE\n")
        .append("        WHEN lmv.is_from_me = 1 THEN 'Me'\n")
        .append("        ELSE h.id\n")
        .append("    END AS sender_id\n")
        .append("FROM\n")
        .append("    LatestMessageVersions lmv\n")
        .append("    JOIN chat_message_join cmj ON lmv.ROWID = cmj.message_id\n")
        .append("    JOIN chat c ON cmj.chat_id = c.ROWID\n")
        .append("    LEFT JOIN handle h ON lmv.handle_id = h.ROWID\n")
        .append("WHERE\n")
        .append("    lmv.rn = 1\n")
        .append("    AND c.display_name = 'Founding Fathers'\n")
        .append("ORDER BY\n")
        .append("    lmv.date DESC\n")
        .append("LIMIT 15;\n").toString();


    private static final String QUERY =
            new StringBuilder()
                    .append("SELECT\n")
                    .append("    message.date,\n")
                    .append("    handle.id AS sender,\n")
                    .append("    message.text,\n")
                    .append("    message.attributedBody\n")
                    .append("FROM\n")
                    .append("    message\n")
                    .append("LEFT JOIN\n")
                    .append("    handle ON message.handle_id = handle.ROWID\n")
                    .append("JOIN\n")
                    .append("    chat_message_join ON message.ROWID = chat_message_join.message_id\n")
                    .append("JOIN\n")
                    .append("    chat ON chat.ROWID = chat_message_join.chat_id\n")
                    .append("WHERE\n")
                    .append("    chat.display_name = 'Founding Fathers'\n")
                    .append("ORDER BY\n")
                    .append("    message.date DESC\n")
                    .append("LIMIT 15;").toString();
    static{
        contactMap = new HashMap<>();
        contactMap.put("+16092162812", "Aparna");
        contactMap.put("+12018893435", "Cavin");
        contactMap.put("+18482340935", "Rashmika");
        contactMap.put("+17329304474", "Nawrin");
        contactMap.put("+18565059695", "Shubh");
        contactMap.put("Me", "George");
        contactMap.put("+12012684971", "Eshita");
        contactMap.put("+19732349392", "Supreet");
        contactMap.put("sobyrajesh@gmail.com", "Aparna");
        contactMap.put("+12017908762", "Khoa");
        contactMap.put("eshitajain194@yahoo.com", "Eshita");
        contactMap.put("kvtran3546@gmail.com", "Khoa");
        contactMap.put("+17322936119", "George");
    }

    public static MessageDB0 getInstance() {
        if (instance == null) {
            instance = new MessageDB0();
        }
        return instance;
    }

    public MessageDB0() {
        decoder = ProcessTypedStreamDecoder.getInstance();
    }

    public static void main(String[] args) {
        final ProcessTypedStreamDecoder decoder = ProcessTypedStreamDecoder.getInstance();
        final List<Message> messages = new ArrayList<>();
        System.out.println("HERE");
        System.out.println(decoder.decode("soemthing"));
        System.out.println("asdfHERE");
//        try (Connection conn = DriverManager.getConnection(PATH)){
//            PreparedStatement stmt = conn.prepareStatement(QUERY);
//            ResultSet rs = stmt.executeQuery();
//            while(rs.next()){
////                for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
////                    System.out.println(rs.getMetaData().getColumnName(i) + ": " + rs.getString(i));
////                }
//                final String timestamp = parseAppleTimestamp(Long.parseLong(rs.getString(TIMESTAMP)));
//                final String sender = contactMap.get(rs.getString(SENDER));
//                final String text = rs.getString(TEXT);
//                final String decodedTypedStream = decoder.decode(rs.getString(TYPED_STREAM));
//                final Message message = new Message(timestamp, sender, text == null || text.isEmpty() ? decodedTypedStream : text);
//                messages.add(message);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        messages.forEach(System.out::println);
        System.out.println(QUERY);
    }
    public static String parseAppleTimestamp(long nsSince2001) {
        double seconds = nsSince2001 / 1e9;
        Instant appleEpoch = Instant.parse("2001-01-01T00:00:00Z");
        Instant resultInstant = appleEpoch.plusNanos((long)(seconds * 1e9));

        ZonedDateTime easternTime = resultInstant.atZone(ZoneId.of("America/New_York"));
        return easternTime.toString();
    }

    public static Map<String, String> getContactsDict() {
        Map<String, String> contactsDict = new HashMap<>();
        contactsDict.put("+16092162812", "Aparna");
        contactsDict.put("+12018893435", "Cavin");
        contactsDict.put("+18482340935", "Rashmika");
        contactsDict.put("+17329304474", "Nawrin");
        contactsDict.put("+18565059695", "Shubh");
        contactsDict.put("Me", "George");
        contactsDict.put("+12012684971", "Eshita");
        contactsDict.put("+19732349392", "Supreet");
        contactsDict.put("sobyrajesh@gmail.com", "Aparna");
        contactsDict.put("+12017908762", "Khoa");
        contactsDict.put("eshitajain194@yahoo.com", "Eshita");
        contactsDict.put("kvtran3546@gmail.com", "Khoa");
        contactsDict.put("+17322936119", "George");
        return contactsDict;
    }
}
