const loc = window.location
let protocol = "ws";
if (loc.protocol === "https:") {
    protocol = "wss";
}
let wsUrl = protocol + '://' + loc.host + "/argus-emergency/ws"
if (process.env.NODE_ENV === 'development') {
    wsUrl = "ws://localhost:8080/argus-emergency/ws"
}
const socket = new WebSocket(wsUrl);
export default socket