function init() {
    // Calls the updateChat servlet every second for new messages
    setInterval(function() {
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
