goog.require('goog.dom');
goog.require('templates.message');

function init() {
    // Calls the updateChat servlet every second for new messages
    setInterval(() => {
        const messagesContainer = goog.dom.getElement('messages-container');
        const currNumMessages = goog.dom.getElementsByClass('message').length;
        const groupId = goog.dom.getElement('groupId').value;
        const urlString = '/updateChat?groupId=' + groupId + 
            '&currMessages=' + currNumMessages;

        fetch(urlString).then(response => response.json()).then(json => {
            for (let i = 0; i < json.length; i++) {
                const messageTemplate = 
                    templates.message.messageBubble({'message': json[i]});
                const messageHtmlNode = goog.dom.constHtmlToNode(goog.string.Const.from(messageTemplate));
                goog.dom.appendChild(messagesContainer, messageHtmlNode);
            }
        });
        
        }, 1000
    );
}
