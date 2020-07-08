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

// Sample Closure library code. This will eventually
// replace the code above, but I'm just waiting on Dylan
// to merge the Closure library into master, since he's already
// downloaded it into our project

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
//             const p = goog.dom.createDOM(goog.com.TagName.P);
//             goog.dom.setTextContent(p, json[i]);
//             goog.dom.classlist.add(p, "message");
//             goog.dom.appendChild(messagesContainer, p);
//         }
//     });

//     }, 1000);
// }
