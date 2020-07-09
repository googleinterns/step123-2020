function init() {
    // Calls the updateChat servlet every second for new messages
    setInterval(() => {
    const messagesContainer = document.getElementById("messages-container");
    const currNumMessages = document.getElementsByClassName("message").length;
    const urlString = "/updateChat?currMessages=" + currNumMessages;
    fetch(urlString).then(response => response.json()).then(json => {
        for (let i = 0; i < json.length; i++) {
            const p = document.createElement("P");
            p.innerText = json[i];
            p.classList.add("message");
            messagesContainer.appendChild(p);
        }
    });

    }, 1000);
}

// I'll use this code later once I figure out why it's being so 
// slow but it does the same thing as above just with the Closure
// library

// goog.require('goog.dom');
// goog.require('goog.dom.TagName');
// goog.require('goog.dom.classlist');

// function init() {
//     // Calls the updateChat servlet every second for new messages
//     setInterval(() => {
//     const messagesContainer = goog.dom.getElement("messages-container");
//     const currNumMessages = goog.dom.getElementsByClass("message").length;
//     const urlString = "/updateChat?currMessages=" + currNumMessages;

//     fetch(urlString).then(response => response.json()).then(json => {
//         for (let i = 0; i < json.length; i++) {
//             const p = goog.dom.createDom(goog.dom.TagName.P);
//             goog.dom.setTextContent(p, json[i]);
//             goog.dom.classlist.add(p, "message");
//             goog.dom.appendChild(messagesContainer, p);
//         }
//     });

//     }, 1000);
// }
