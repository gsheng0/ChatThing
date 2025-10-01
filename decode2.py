import typedstream
import sys
import json
import sqlite3
from typing import List, Dict
from datetime import datetime, timedelta, timezone
from zoneinfo import ZoneInfo
import base64
import time


class MessageEntry:
    def __init__(self, row):
        self.timestamp: str = str(parse_apple_timestamp(row[0]).astimezone(ZoneInfo("America/New_York")))
        self.sender: str = row[1] if row[1] != None else "Me"
        self.text: str = "" if row[2] == None else row[2]
        self.text_backup: str = self.extract_text(row[3])
        self.image_path: str = row[4]
        self.chat_name: str = row[5]

    def __str__(self):
        return f"{self.timestamp}|{self.sender}|{self.text}|{self.text_backup}|{self.image_path}"

    def __hash__(self):
        return hash(hash(self.timestamp) + hash(self.sender) + hash(self.text) + hash(self.text_backup) + hash(self.image_path))

    def __eq__(self, other):
        return hash(self) == hash(other)

    def extract_text(self, blob):
        if blob == None:
            return "[None]"
        return typedstream.unarchive_from_data(blob).contents[0].value.value

    def to_dict(self):
        out = {}
        out["timestamp"] = self.timestamp
        out["sender"] = self.sender
        out["content"] = self.text if self.text != None else self.text_backup

def parse_apple_timestamp(ns_since_2001):
    seconds = ns_since_2001 / 1e9
    apple_epoch = datetime(2001, 1, 1, tzinfo=timezone.utc)
    return apple_epoch + timedelta(seconds=seconds)


chat_names = ["Sewerslide Pack", "Founding Fathers"]
db_path = "/Users/georgesheng/Library/Messages/chat.db";
placeholders = ', '.join('?' for _ in chat_names)

# Build your SQL query string
query = (
    "SELECT\n"
    "    message.date,\n"
    "    handle.id AS sender,\n"
    "    message.text,\n"
    "    message.attributedBody,\n"
    "    attachment.filename AS image_path,\n"
    "    chat.display_name AS chat_name\n"
    "FROM\n"
    "    message\n"
    "LEFT JOIN\n"
    "    handle ON message.handle_id = handle.ROWID\n"
    "JOIN\n"
    "    chat_message_join ON message.ROWID = chat_message_join.message_id\n"
    "JOIN\n"
    "    chat ON chat.ROWID = chat_message_join.chat_id\n"
    "LEFT JOIN\n"
    "    message_attachment_join ON message.ROWID = message_attachment_join.message_id\n"
    "LEFT JOIN\n"
    "    attachment ON message_attachment_join.attachment_id = attachment.ROWID\n"
    f"WHERE\n"
    f"    chat.display_name IN ({placeholders})\n"
    "ORDER BY\n"
    "    message.date DESC\n"
    "LIMIT 5;"
)

def get_chat_ids(group_names: List[str]) -> List[int]:
    if not group_names:
        return []

    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Create placeholders for the IN clause
    placeholders = ', '.join('?' for _ in group_names)

    query = f"""
    SELECT ROWID
    FROM chat
    WHERE display_name IN ({placeholders})
    """

    cursor.execute(query, group_names)
    rows = cursor.fetchall()

    # Extract just the ROWID values
    chat_ids = [row[0] for row in rows]
    print(chat_ids)

    conn.close()
    return chat_ids

def get_all_messages() -> List[MessageEntry]:
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute(query)

    messages: List[MessageEntry] = []
    for row in cursor.fetchall():
        messages.append(MessageEntry(row))
    conn.close()
    return messages


chat_ids = get_chat_ids(chat_names)
def get_current_apple_timestamp_ns() -> int:
    """Returns current time in Apple Messages format (ns since 2001-01-01 UTC)"""
    now = datetime.now(timezone.utc)
    apple_epoch = datetime(2001, 1, 1, tzinfo=timezone.utc)
    delta = now - apple_epoch
    ns_since_2001 = int(delta.total_seconds() * 1e9)
    return ns_since_2001

# Initialize at application startup
last_seen_date = get_current_apple_timestamp_ns()
def get_messages() -> List[MessageEntry]:
    global last_seen_date
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    placeholders = ', '.join('?' for _ in chat_ids)
    query = f"""
    SELECT
        m.ROWID AS message_id,
        m.date,
        h.id AS sender,
        m.text,
        m.attributedBody,
        a.filename AS image_path,
        c.display_name AS chat_name
    FROM
        message m
    LEFT JOIN handle h ON m.handle_id = h.ROWID
    JOIN chat_message_join cmj ON m.ROWID = cmj.message_id
    JOIN chat c ON cmj.chat_id = c.ROWID
    LEFT JOIN message_attachment_join maj ON m.ROWID = maj.message_id
    LEFT JOIN attachment a ON maj.attachment_id = a.ROWID
    WHERE
        c.ROWID IN ({placeholders})
        AND m.date > ?
    ORDER BY
        m.date ASC
    LIMIT 50;
    """

    cursor.execute(query, chat_ids + [last_seen_date])
    new_messages = cursor.fetchall()

    if not new_messages:
        return

    # Process new messages
    for msg in new_messages:
        message_id, date, sender, text, attributed, image_path, chat_name = msg
        print(f"[{chat_name}] {sender}: {text or '[media]'}")

    # Update the last seen message timestamp
    last_seen_date = new_messages[-1][1]
    print("LAST SEEN DATE: " + str(last_seen_date))


for line in sys.stdin:
    try:
        messages = get_messages()
        for message in messages:
            print(base64.b64encode((str)(message).encode("utf-8")), end="````%")
        print()
        sys.stdout.flush()
    except Exception as e:
        sys.stdout.flush()
