module.exports = function (app) {
    app.post('/argus-emergency/api/resource', function(req, res) {
        res.json({data: {
            uid: "001"
        }})
    })
    const ws = require('ws');
    const wss = new ws.WebSocketServer({ port: 8080 });

    wss.on('connection', function connection(ws) {
        setTimeout(function(){
            ws.send('/task/0');
        }, 10000)
        setTimeout(function(){
            ws.send('/task/T0');
        }, 20000)
    });
};