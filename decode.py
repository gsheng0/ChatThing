import typedstream
import sys
import json
import sqlite3
from typing import List, Dict
from datetime import datetime, timedelta, timezone
from zoneinfo import ZoneInfo
import base64


class MessageEntry:
    def __init__(self, row):
        self.timestamp: str = str(parse_apple_timestamp(row[0]).astimezone(ZoneInfo("America/New_York")))
        self.sender: str = row[1] if row[1] != None else "Me"
        self.text: str = "" if row[2] == None else row[2]
        self.text_backup: str = self.extract_text(row[3])
        self.image_path: str = row[4]

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

db_path = "/Users/georgesheng/Library/Messages/chat.db";
query = (
    "SELECT\n"
    "    message.date,\n"
    "    handle.id AS sender,\n"
    "    message.text,\n"
    "    message.attributedBody,\n"
    "    attachment.filename AS image_path\n"
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
    "WHERE\n"
    "    chat.display_name = ?\n"
    "ORDER BY\n"
    "    message.date DESC\n"
    "LIMIT 5;"
)



def get_all_messages(gc_name: str) -> List[MessageEntry]:
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute(query, (gc_name,))

    messages: List[MessageEntry] = []
    for row in cursor.fetchall():
        messages.append(MessageEntry(row))
    conn.close()
    return messages

for line in sys.stdin:
    try:
        messages = get_all_messages("Sewerslide Pack")
        for message in messages:
            print(base64.b64encode((str)(message).encode("utf-8")), end="````%")
        # print("[E`N`D`]")
        print()
        sys.stdout.flush()
    except Exception as e:
        sys.stdout.flush()
