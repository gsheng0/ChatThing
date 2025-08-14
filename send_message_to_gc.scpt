on run {groupName, messageText}
  tell application "Messages"
    set targetChat to first chat whose name is groupName
    send messageText to targetChat
  end tell
end run