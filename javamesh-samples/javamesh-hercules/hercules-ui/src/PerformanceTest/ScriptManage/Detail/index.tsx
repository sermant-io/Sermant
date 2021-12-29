import { Button, Form, Input, message, Modal } from "antd"
import React, { useEffect, useRef, useState } from "react"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import PageInfo from "../../../component/PageInfo"
import MonacoEditor from 'react-monaco-editor'
import { useLocation } from "react-router-dom"
import axios from "axios"
import CloseCircleOutlined from "@ant-design/icons/lib/icons/CloseCircleOutlined"
import HostForm from "../../TestTask/Create/HostForm"
import { debounce } from 'lodash';
import "./index.scss"
import modal from "antd/lib/modal"
import UsageCharts from "./UsageCharts"
import TreeOrchestrate, { Values } from "../../../component/TreeOrchestrate"

export default function App() {
    let submit = false
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const [form] = Form.useForm()
    const path = urlSearchParams.get("path") || ""
    return <div className="ScriptDetail">
        <Breadcrumb label="脚本管理" sub={{ label: "详情", parentUrl: "/PerformanceTest/ScriptManage" }} />
        <PageInfo>如需下载代理，请在右上角菜单栏点击选择<Button type="link" size="small"> “下载代理” </Button>。</PageInfo>
        <Card>
            <Form initialValues={{ path }} form={form} requiredMark={false} labelCol={{ span: 2 }} onFinish={async function (values) {
                if (submit) return
                submit = true
                try {
                    await axios.put('/argus/api/script', values, { params: { path } })
                    message.success("更新成功")
                } catch (e: any) {
                    message.error(e.message)
                }
                submit = false
            }}>
                <div className="Form">
                    <div className="Line">
                        <Form.Item className="ScriptName" name="path" label="脚本名">
                            <Input disabled />
                        </Form.Item>
                        <Button className="Save" htmlType="submit" type="primary">保存</Button>
                        <Button type="primary" onClick={async function () {
                            if (submit) return
                            submit = true
                            try {
                                const script = form.getFieldValue("script")
                                const res = await axios.post('/argus/api/script/check', { script, path })
                                if (res.data.data) {
                                    modal.info({
                                        width: 700, content: res.data.data.split("\n").map(function (item: any) {
                                            return <p>{item}</p>
                                        })
                                    });
                                } else {
                                    message.success("验证成功")
                                }
                            } catch (e: any) {
                                message.error(e.message)
                            }
                            submit = false
                        }}>验证脚本</Button>
                    </div>
                    <div className="Line">
                        <Form.Item label="提交信息" name="commit" rules={[{ required: true }]}>
                            <Input.TextArea maxLength={256} showCount autoSize={{ minRows: 2, maxRows: 2 }} />
                        </Form.Item>
                        <ScriptHosts />
                    </div>
                </div>
                <Form.Item className="Editor" name="script" rules={[{ required: true, max: 5000 }]}>
                    <ScriptEditor path={path}/>
                </Form.Item>
            </Form>
        </Card>
    </div>
}

type Host = { host_id?: string, domain?: string }
function ScriptHosts() {
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const path = urlSearchParams.get("path")
    const [hosts, setHosts] = useState<Host[]>([])
    async function load(path: string | null) {
        try {
            const res = await axios.get('/argus/api/script/host', { params: { path } })
            setHosts(res.data.data)
        } catch (error: any) {

        }
    }
    useEffect(function () {
        load(path)
    }, [path])
    return <div className="ScriptHost">
        <div className="Hosts">{hosts.map(function (item) {
            return <ScriptHost key={item.host_id} data={item} onClick={async function () {
                try {
                    await axios.delete('/argus/api/script/host', { params: { host_id: item.host_id, path } })
                } catch (e: any) {
                    message.error(e.message)
                }
                load(path)
            }} />
        })}</div>
        <HostForm onFinish={async function ({ ip, domain }) {
            try {
                await axios.post("/argus/api/script/host", { ip, domain, path })
            } catch (error: any) {

            }
            load(path)
        }}>
            <div className="Button">添加</div>
        </HostForm>
    </div>
}

function ScriptHost(props: { data: Host, onClick: () => Promise<void> }) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    return <span>
        <Button type="link" size="small" onClick={function () { setIsModalVisible(true) }}>{props.data.domain}</Button>
        <CloseCircleOutlined onClick={props.onClick} />
        <Modal title="监控" width={820} visible={isModalVisible} onCancel={function () { setIsModalVisible(false) }} footer={null}>
            <UsageCharts url={"/argus/api/script/host/chart"} params={{ host_id: props.data.host_id }} />
        </Modal>
    </span>
}

function ScriptEditor(props: { onChange?: (value: any) => void, path: string }) {
    const [script, setScript] = useState("")
    useEffect(function () {
        props.onChange?.(script)
    }, [props, script])
    useEffect(function () {
        (async function load() {
            try {
                const res = await axios.get('/argus/api/script/get', { params: { path: props.path } })
                setScript(res.data.data.script)
            } catch (error: any) {

            }
        })()
    }, [props.path])
    const debounceRef = useRef(debounce(function (value: any) {
        setScript(value)
    }, 1000))
    return <>
        {props.path.endsWith(".groovy") && <ScriptOrchestrate setScript={setScript} path={props.path}/>}
        <MonacoEditor height="620" language="python" value={script} onChange={debounceRef.current} />
    </>
}

function ScriptOrchestrate(props: {setScript: (script: string)=> void, path: string}) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [data, setData] = useState<Values>()
    const valuesRef = useRef<Values>()
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get("/argus-emergency/api/script/argus/orchestrate", { params: { path: props.path } })
                const tree = res.data.data.tree
                const mapData = res.data.data.map
                const map = new Map()
                for (const key in mapData) {
                    map.set(key, mapData[key])
                }
                setData({ tree, map })
            } catch (error: any) {

            }
        })()
    }, [props.path])
    return <>
        <Button type="primary" onClick={function () {
            setIsModalVisible(true)
        }}>脚本编排</Button>
        <Modal className="ScriptDetailOrchestrate" title="新建脚本" width={1200} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            {data && <TreeOrchestrate initialValues={data} onSave={async function (values) {
                valuesRef.current = values
            }} />}
            <div className="Buttons">
                <Button type="primary" onClick={async function () {
                    try {
                        const map: any = {}
                        valuesRef.current?.map.forEach(function (value, key) {
                            map[key] = value
                        })
                        const res = await axios.put("/argus-emergency/api/script/argus/orchestrate", {path: props.path, tree: valuesRef.current?.tree, map})
                        props.setScript(res.data.data.script)
                        setIsModalVisible(false)
                    } catch (error: any) {
                        message.error(error.message)
                    }
                }}>保存</Button>
                <Button onClick={function () {
                    setIsModalVisible(false)
                }}>取消</Button>
            </div>
        </Modal>
    </>
}