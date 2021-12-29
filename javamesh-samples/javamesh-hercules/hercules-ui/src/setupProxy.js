module.exports = function (app) {
    app.post('/argus/api/script/upload', function (req, res) {
        res.json({msg: "上传失败！"})
    });
    app.post('/argus-emergency/api/script/upload', function (_, res) {
        res.json({ msg: "上传失败！" })
    });
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