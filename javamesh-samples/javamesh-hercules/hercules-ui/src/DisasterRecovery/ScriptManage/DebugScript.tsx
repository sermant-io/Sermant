import { Button, Form, FormInstance, message } from "antd"
import axios from "axios";
import React, { useEffect, useState } from "react"
import ServiceSelect from "../../component/ServiceSelect"
import "./DebugScript.scss"

export default function App({ form }: { form: FormInstance }) {
    const [data, setData] = useState<string[]>([])
    const [debug, setDebug] = useState({ debugId: undefined, timeInterval: undefined })
    async function load(debug_id: string, line?: number) {
        try {
            const params = { debug_id, line }
            const res = await axios.get('/argus-emergency/api/script/debugLog', { params })
            setData(function (data) {
                return data.concat(res.data.data).slice(-10000)
            })
            return res.data.line as number
        } catch (error: any) {

        }
    }
    function clear() {
        clearInterval(debug.timeInterval)
        setDebug({
            debugId: undefined,
            timeInterval: undefined
        })
    }
    useEffect(function () {
        return function () {
            clearInterval(debug.timeInterval)
        }
    }, [debug.timeInterval])
    return <div className="DebugScript">
        <Form.Item className="ServerName" name="server_name">
            <ServiceSelect url="/argus-emergency/api/host/search" placeholder="调试主机名称" />
        </Form.Item>
        {debug.debugId ? <Button type="primary" onClick={function () {
            clear()
            axios.post('/argus-emergency/api/script/debugStop', { debug_id: debug.debugId })
        }}>停止</Button> : <Button type="primary" onClick={async function () {
            const server_name = form.getFieldValue("server_name")
            if (!server_name) {
                message.error("请输入调试主机名称")
                return
            }
            setData([])
            try {
                const content = form.getFieldValue("content")
                const res = await axios.post('/argus-emergency/api/script/debug', { content, server_name })
                const debugId = res.data.data.debug_id
                let line = await load(debugId)
                setDebug({
                    debugId,
                    timeInterval: setInterval(async function () {
                        line = await load(debugId, line)
                        if (!line) clear()
                    }, 1000) as any
                })
            } catch (error: any) {
                message.error(error.message)
            }
        }}>调试</Button>}
        <ul className="Log">{data.map(function (item, index) {
            return <li key={index}>{item}</li>
        })}</ul>
    </div>
}