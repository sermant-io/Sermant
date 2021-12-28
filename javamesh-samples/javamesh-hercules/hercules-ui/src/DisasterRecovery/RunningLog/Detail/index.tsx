import React, { useEffect, useRef, useState } from "react"
import { Button, Divider, Form, message, Modal, Radio, Steps, Table } from "antd"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import socket from "../../socket"
import "./index.scss"
import axios from "axios"
import { useLocation } from "react-router-dom"
import MonacoEditor from "react-monaco-editor"
import { debounce } from 'lodash';

type Scena = { key: string, scena_name: string, scena_id: string, status: 'wait' | 'process' | 'finish' | 'error', status_label: string }
type Task = { key: string, status: string }
export default function App() {
    let submit = false
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const history_id = urlSearchParams.get("history_id") || ""
    const [scenaList, setScenaList] = useState<Scena[]>([])
    const [taskList, setTaskList] = useState<Task[]>()
    const [loading, setLoading] = useState(false)
    const [current, setCurrent] = useState(0)
    const scenaKeysRef = useRef<string[]>([])
    const taskKeysRef = useRef<string[]>([])
    const scenaIdRef = useRef<string>()

    async function loadTask(history_id: string) {
        setLoading(true)
        try {
            const res = await axios.get("/argus-emergency/api/history/scenario/task", { params: { history_id, scena_id: scenaIdRef.current } })
            const data = res.data.data
            taskKeysRef.current = data.map(function (item: { key: string }) { return "/task/" + item.key })
            setTaskList(data)
        } catch (error: any) {

        }
        setLoading(false)
    }
    useEffect(function () {
        async function loadScena(history_id: string) {
            try {
                const res = await axios.get("/argus-emergency/api/history/scenario", { params: { history_id } })
                const data = res.data.data
                scenaKeysRef.current = data.map(function (item: { key: string }) { return "/scena/" + item.key })
                setScenaList(data)
                if (!scenaIdRef.current) scenaIdRef.current = data[0]?.scena_id
                loadTask(history_id)
            } catch (error: any) {

            }
        }
        loadScena(history_id);

        const dbLoadScena = debounce(loadScena, 1000)
        const dbLoadTask = debounce(loadTask, 1000)
        function handleSocket(event: MessageEvent<any>) {
            const message = event.data
            if (scenaKeysRef.current.includes(message)) {
                dbLoadScena(history_id)
                // 停止响应任务更新
                taskKeysRef.current.length = 0
            } else if (taskKeysRef.current.includes(message)) {
                dbLoadTask(history_id);
            }
        }
        socket.addEventListener("message", handleSocket)
        return function () {
            socket.removeEventListener("message", handleSocket)
        }
    }, [history_id])
    return <div className="RunningLogDetail">
        <Breadcrumb label="执行记录" sub={{ label: "详细信息", parentUrl: "/DisasterRecovery/RunningLog" }} />
        <Card>
            <Steps current={current} className="Steps" size="small" type="navigation" onChange={function (current) {
                setCurrent(current)
                scenaIdRef.current = scenaList[current]!.scena_id
                loadTask(history_id)
            }}>{scenaList.map(function (item, index) {
                return <Steps.Step key={item.key} status={item.status} title={item.scena_name} description={item.status_label} />
            })}</Steps>
            <Divider />
            <Table size="middle" rowKey="key" loading={loading} dataSource={taskList} pagination={false}
                columns={[
                    {
                        title: "编号", dataIndex: "task_no", ellipsis: true
                    },
                    {
                        title: "子任务名称", dataIndex: "task_name", ellipsis: true
                    },
                    {
                        title: "操作员", dataIndex: "operator", ellipsis: true
                    },
                    {
                        title: "开始时间", dataIndex: "start_time", ellipsis: true
                    },
                    {
                        title: "结束时间", dataIndex: "end_time", ellipsis: true
                    },
                    {
                        title: "执行状态", dataIndex: "status_label", ellipsis: true
                    },
                    {
                        title: "操作", width: 200, dataIndex: "key", align: "center", render(key, record) {
                            return <>
                                <Button type="primary" disabled={record.status !== "error"} size="small" onClick={async function () {
                                    if (submit) return
                                    submit = false
                                    try {
                                        await axios.post("/argus-emergency/api/history/scenario/task/runAgain", { history_id, key })
                                        message.success("执行成功")
                                        loadTask(history_id)
                                    } catch (error: any) {
                                        message.error(error.message)
                                    }
                                    submit = true
                                }}>重新执行</Button>
                                <TaskConfirm record={record} load={function () {
                                    loadTask(history_id)
                                }} />
                            </>
                        }
                    },
                    {
                        title: "执行方式", dataIndex: "sync", ellipsis: true
                    },
                    {
                        title: "执行日志", width: 100, render(_, record) {
                            return <TaskLog record={record} />
                        }
                    }
                ]}
            />
        </Card>
    </div>
}

function TaskConfirm(props: { record: Task, load: () => void }) {
    let submit = false
    const [isModalVisible, setIsModalVisible] = useState(false);
    return <>
        <Button type="primary" disabled={props.record.status !== "error"} size="small" onClick={function () { setIsModalVisible(true) }}>人工确认</Button>
        <Modal className="TaskConfirm" title="人工确认" width={400} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form requiredMark={false} initialValues={{confirm: "成功"}} onFinish={async function (values) {
                if (submit) return
                submit = true
                try {
                    const data = { ...values, key: props.record.key }
                    await axios.post("/argus-emergency/api/history/scenario/task/ensure", data)
                    message.success("提交成功")
                    setIsModalVisible(false)
                } catch (error: any) {
                    message.error(error.message)
                }
                submit = false
            }}>
                <Form.Item name="confirm" label="执行结果">
                    <Radio.Group options={["成功", "失败"]} />
                </Form.Item>
                <Form.Item className="Buttons">
                    <Button type="primary" htmlType="submit">提交</Button>
                    <Button onClick={function () {
                        setIsModalVisible(false)
                    }}>取消</Button>
                </Form.Item>
            </Form>
        </Modal>
    </>
}

function TaskLog(props: { record: Task }) {
    let submit = false
    const [debug, setDebug] = useState({ isModalVisible: false, timeInterval: undefined })
    const [data, setData] = useState<string[]>([])
    async function load(key: string, line?: number) {
        try {
            const params = { key, line }
            const res = await axios.get('/argus-emergency/api/history/scenario/task/log', { params })
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
            isModalVisible: false,
            timeInterval: undefined
        })
    }
    useEffect(function () {
        return function () {
            clearInterval(debug.timeInterval)
        }
    }, [debug.timeInterval])
    return <>
        <Button type="primary" size="small" onClick={async function () {
            if (submit) return
            submit = true
            let line = await load(props.record.key)
            setDebug({
                isModalVisible: true,
                timeInterval: setInterval(async function () {
                    line = await load(props.record.key, line)
                    if (!line) clear()
                }, 1000) as any
            })
            submit = false
        }}>查看日志</Button>
        {debug.isModalVisible && <Modal className="LogButton" title="查看日志" width={1200} visible={true} maskClosable={false} footer={null} onCancel={clear}>
            <MonacoEditor height="620" language="plaintext" options={{ readOnly: true }} value={data.join("\n")} />
        </Modal>}
    </>
}
