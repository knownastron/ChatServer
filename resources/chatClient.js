// WebSocket
let chatRoom;
let username;
let joinButton
let mySocket;
let sendButton;
let messageArea;
let entireChatDiv;
let welcomeToRoom;
let leaveButton;
let emptyMessage;

window.onload = function() {
    loadLoginPage();
}

function loadLoginPage() {
  let xhr = new XMLHttpRequest();
  xhr.open("GET", "loginPage.txt");
  xhr.send();
  xhr.addEventListener("load", function() {
      document.body.innerHTML= this.responseText;
      joinButtonHandling();
  })
}

function loadChatRoomPage() {
  let xhr = new XMLHttpRequest();
  xhr.open("GET", "chatRoom.txt");
  xhr.send();
  xhr.addEventListener("load", function() {
      document.body.innerHTML= this.responseText;

      sendButton = document.getElementById("sendButton");
      messageArea = document.getElementById("messageArea");
      leaveButton = document.getElementById("leaveButton");
      welcomeToRoom = document.getElementById("welcomeToRoom");
      welcomeToRoom.innerHTML = "Welcome to: " + chatRoom;
      sendButtonHandling();
      leaveButtonHandling();
      webSocketHandling();
  })
}

function sendButtonHandling() {
  document.getElementById("textArea").addEventListener("keyup", function(event) {
    event.preventDefault();
    if (event.keyCode === 13) {
        document.getElementById("sendButton").click();
    }
  });
    sendButton.onclick = function() {
      let message = document.getElementById("textArea").value;
      if (message !== "") {
        document.getElementById("textArea").value = "";
        let sendMessage = JSON.stringify({command: "post", username: username,
                                          message: message});
        // let sendMessage = username + " " + message;
        mySocket.send(sendMessage);
        console.log("sending message");
      }
    }
}

function leaveButtonHandling() {
  leaveButton.onclick = function() {
    loadLoginPage();
    mySocket.close();
  }
}

function joinButtonHandling() {
  joinButton = document.getElementById("joinButton");
  emptyInput = document.getElementById("emptyMessage");
  joinButton.onclick = function() {
      // get username and chatroom
      username = document.getElementsByName("username")[0].value;
      chatRoom = document.getElementsByName("chatRoom")[0].value;
      emptyInput.innerHTML = '';
      if (chatRoom === "") {
        emptyInput.append(document.createTextNode("Chatroom cannot be empty"));
        joinButton.after(emptyInput);
      } else if (username === "" ) {
        emptyInput.append(document.createTextNode("Username cannot be empty"));
        joinButton.after(emptyInput);
      } else {
        document.getElementsByName("username")[0].value = "";
        document.getElementsByName("chatRoom")[0].value = "";
        loadChatRoomPage();
      }

  }
}

function webSocketHandling() {
  // Create and open socket
  console.log(location.host);
  mySocket = new WebSocket("ws://" + location.host);
  mySocket.onopen = function(e) {
      console.log("Socket is Open!");
      let joinRoomMessage = JSON.stringify({command: "join", message: chatRoom.toLowerCase()});
      console.log(joinRoomMessage);
      mySocket.send(joinRoomMessage);
  }

  // handle return messages
  mySocket.onmessage = function(e) {
    console.log("Message Received from Server!");

    let returnData = JSON.parse(e.data);
    let returnUser = returnData.username;
    let returnMessage = returnData.message;
    let returnTime = returnData.time;
    let messageTable = document.getElementById("messageTable");
    let theMessage = document.createElement("table");
    if (returnUser === username) {
      theMessage.setAttribute("class", "myMessage");
    } else {
      theMessage.setAttribute("class", "elseMessage");
    }


    // the user
    let userRow = document.createElement("tr");
    userRow.setAttribute("class", "userRow");
    let userData = document.createElement("td");
    userData.appendChild(document.createTextNode(returnUser + ":"));
    userData.setAttribute("class", "userData");
    userRow.appendChild(userData);

    //the message
    let messageRow = document.createElement("tr");
    messageRow.setAttribute("class", "messageRow");
    let messageData = document.createElement('td');

    messageData.appendChild(document.createTextNode(returnMessage));
    messageRow.appendChild(messageData);

    //the time
    let timeRow = document.createElement("tr");
    timeRow.setAttribute("class", "timeRow");
    let timeData = document.createElement('td');
    timeData.appendChild(document.createTextNode(returnTime));
    timeRow.appendChild(timeData);

    theMessage.appendChild(userRow);
    theMessage.appendChild(messageRow);
    theMessage.appendChild(timeRow);
    messageTable.appendChild(theMessage);
    messageArea.scrollTop = messageArea.scrollHeight;
  }
}
