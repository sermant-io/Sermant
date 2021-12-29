const loc = window.location
let protocol = "ws";
if (loc.protocol === "https:") {
    protocol = "wss";
}
let wsUrl = protocol + '://' + loc.host + "/argus/ws"
if (process.env.NODE_ENV === 'development') {
    wsUrl = "ws://localhost:8080/argus/ws"
}
const socket = new WebSocket(wsUrl);
export default socket