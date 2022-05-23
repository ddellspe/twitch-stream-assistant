var stompClient = null;

function setConnected(connected) {
    $("#messages").html("");
}

function connect() {
    var socket = new SockJS('/twitch-stream-assistant');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/chat', (chatMessage) => displayMessage(chatMessage));
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function displayMessage(chatMessage) {
    let message = JSON.parse(chatMessage.body);
    let messageRow = "<div class='row message'><div class='inline-message'>"
    messageRow += "<span class='username' "
    messageRow += "style='color: " + message.tags.color + "'>"
    if (message.badges && message.badges.length > 0) {
        message.badges.forEach(
            badge => messageRow += "<img src=\"/img/badges/" +
                                   badge.replace("/", "_") + "\" alt=\"" +
                                   badge.split("/")[0] + "\" class=\"chat-badge\"> ")
    }
    messageRow += "<span class='user'>" + message.username + "</span>"
    messageRow += "</span><span class='text-fragment'>: </span><span class='message-content'>"
    let msg = ""
    let pos = 0
    if (message.emotes && message.emotes.length > 0) {
        for(let i = 0 ; i < message.emotes.length ; i++) {
            let emoteLoc = message.emotes[i];
            if (emoteLoc.startIndex > pos) {
                msg += "<span class='text'>" + message.message.substring(pos, emoteLoc.startIndex) + "</span>"
            }
            let emoteName = msg.slice(emoteLoc.startIndex, emoteLoc.endIndex + 1)
            msg += "<img src=\"/img/emotes/" +
                  emoteLoc.emoteId +
                  "\" alt=\"" +
                  emoteName +
                  "\" class=\"chat-emote\">"
            pos = emoteLoc.endIndex + 1
        }
    }
    if (message.message.length > pos) {
        msg += "<span class='text'>" + message.message.substring(pos) + "</span>"
    }
    messageRow += msg
    messageRow += "</span></div></div>";

    $("#messages").append(messageRow);
}

$(function () {
    connect();
});