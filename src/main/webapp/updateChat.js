goog.require('goog.dom');
goog.require('templates.message');

function init() {
    // Calls the updateChat servlet every second for new messages
    setInterval(function() {
    const messagesContainer = document.getElementById("messages-container");
    const currNumMessages = document.getElementsByClassName("message").length;
    const urlString = "/updateChat?currMessages=" + currNumMessages;

    fetch(urlString).then(response => response.json()).then(json => {
        for (let i = 0; i < json.length; i++) {
            const messageTemplate = templates.message.messageBubble({
                "message": json[i]
            });
            const messageHtmlNode = goog.dom.constHtmlToNode(goog.string.Const.from(messageTemplate));
            messagesContainer.appendChild(messageHtmlNode);
        }
    });

    }, 1000);
}
