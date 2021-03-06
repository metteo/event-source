if (typeof (EventSource) !== "undefined") {
	var source = new EventSource(location.href + "/chat");
	source.onmessage = function(event) {
		document.getElementById("chat").innerHTML += event.data + "<br>";
		console.log(event);
	};
	
	source.onerror = function(event) {
		document.getElementById("chat").innerHTML += "--- Connection Error ---<br>";
		console.log(event);
	};
	
	source.onopen = function(event) {
		document.getElementById("chat").innerHTML += "--- Connection Open ---<br>";
		console.log(event);
	};
} else {
	document.getElementById("chat").innerHTML = "Sorry, your browser does not support server-sent events...";
}

var sendMsg = function() {
	var msg = input.value;
	
	var xhr = new XMLHttpRequest();
	xhr.open("GET", "chat?message=" + msg);
	xhr.send();
	
	input.value = "";
};

var form = document.getElementById("chatForm");
form.onsubmit = function(evt) {
	evt.preventDefault();
	sendMsg();
};

var input = document.getElementById("chatInput");
var send = document.getElementById("chatSend");
send.onclick = sendMsg;