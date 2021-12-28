import React, { ReactNode, useState } from "react"
import Card from "../../component/Card"
import { DynamicCard, ScriptCard, TCPCopyCard } from "./Scenario"
import Steps from "./Steps"
import "./StepForm.scss"
import { Button, Drawer, Form, Input, message } from "antd"
import ScenarioFormItems from "./ScenarioFormItems"
import PageInfo from "../../component/PageInfo"
import axios from "axios"
import ServiceSelect from "../../component/ServiceSelect"
import MonacoEditor from "react-monaco-editor"
import { useHistory } from "react-router"
import TaskForm from "../TestTask/Create/TaskForm"

let params = { scenario_name: "" }
export default function App(props: { pageInfo: ReactNode, breadcrumb: ReactNode, quickStart?: boolean }) {
    let submit = false
    let scenario = "Script"
    const [index, setIndex] = useState(0)
    const [scriptForm] = Form.useForm();
    const history = useHistory();
    const [scenarioName, setScenarioName] = useState<string>()
    const [script, setScript] = useState<{ script: string, script_resource: string }>()
    return <div className="QuickStart">
        <div className="BreadcrumbWarp">
            {props.breadcrumb}
            {scenario === "Dynamic" && index === 1 && <>
                <ParamList />
                <SystemFunc />
                <SystemParam />
            </>}
        </div>
        <PageInfo>{props.pageInfo}</PageInfo>
        <Card className="TypeChoose">
            <Steps keys={function () {
                const array = ["选择场景类型"]
                switch (scenario) {
                    case "Dynamic":
                        array.push("动态编排")
                        break
                    case "TCPCopy":
                        array.push("选择流量", "流量模型配置")
                        break
                    case "Script":
                        array.push("选择脚本")
                        break
                }
                if (props.quickStart) {
                    array.push("任务配置")
                }
                return array
            }()} activeIndex={index} />
            {index === 0 && <>
                <div className="Label">选择场景类型</div>
                <DynamicCard scenario={scenario} />
                <TCPCopyCard scenario={scenario} />
                <ScriptCard scenario={scenario} />
                <div className="Label">场景信息</div>
                <Form className="Form"
                    onFinish={function (values) {
                        params = { ...values }
                        setIndex(1)
                    }}
                    requiredMark={false}
                    labelCol={{ span: 2 }}>
                    <div className="Inputs">
                        <ScenarioFormItems />
                    </div>
                    <Form.Item className="Buttons">
                        <Button type="primary" htmlType="submit">下一步</Button>
                        <Button htmlType="reset">重置</Button>
                    </Form.Item>
                </Form>
            </>}
            {scenario === "Script" && index === 1 && <Form form={scriptForm}
                onFinish={async function (values) {
                    if (submit) return
                    submit = true
                    try {
                        const data = { ...params, ...values }
                        await axios.post('/argus/api/scenario', data)
                        message.success("压测场景创建成功")
                        if (!props.quickStart) {
                            history.goBack()
                        } else {
                            setScenarioName(params.scenario_name)
                            setIndex(2)
                        }
                    } catch (e: any) {
                        message.error(e.message)
                    }
                    submit = false
                }}
                className="ScriptForm" requiredMark={false} labelCol={{ span: 2 }} >
                <Form.Item label="选择脚本" name="script_path" rules={[{ required: true }]}>
                    <ServiceSelect url={"/argus/api/script/search"} onChange={async function (value) {
                        try {
                            const res = await axios.get("/argus/api/script/get", { params: { path: value } })
                            setScript(res.data.data)
                        } catch (error: any) {

                        }
                    }} />
                </Form.Item>
                <Form.Item label="脚本相关资源">
                    <Input.TextArea value={script?.script_resource} readOnly autoSize={{ minRows: 2, maxRows: 2 }} />
                </Form.Item>
                <Form.Item className="Editor" label="脚本详细">
                    <MonacoEditor options={{ readOnly: true }} value={script?.script} language="python" height="620" />
                </Form.Item>
                <Form.Item className="Buttons">
                    <Button type="primary" onClick={function () { setIndex(0) }}>上一步</Button>
                    <Button type="primary" htmlType="submit">{props.quickStart ? "下一步" : "提交"}</Button>
                </Form.Item>
            </Form>}
            {scenarioName && <TaskForm scenarioName={scenarioName} />}
        </Card>
    </div>
}

function ParamList() {
    const [data, setData] = useState<{ param_name: string, param_value: string }[]>()
    async function toggle() {
        if (data) {
            setData(undefined)
        } else {
            setData([])
        }
    }
    return <div className="Param">
        <span className="Icon icon mb icon-cubes"></span>
        <span className="ParamList">
            <span className="Main" onClick={toggle}>参数列表</span>
            {data && <div className="PopUp">
                <div className="Header">
                    <div className="Title">全局参数</div>
                    <span className="Close icon md" onClick={toggle}>clear</span>
                </div>
                <div className="Body">{data.map(function (item, index) {
                    return <div className="Item" key={index}>{item.param_name} - {item.param_value}</div>
                })}</div>
            </div>}
        </span>
    </div>
}

function SystemFunc() {
    const [visible, setVisible] = useState(false);
    return <div className="Param">
        <span className="Icon icon mb icon-ungroup"></span>
        <span className="SystemFunc">
            <span className="Main" onClick={function () { setVisible(true) }}>系统函数</span>
            <Drawer
                title="系统函数"
                onClose={function () { setVisible(false) }}
                visible={visible}
                width={720}
            >
                <p>敬请期待。。。</p>
            </Drawer>
        </span>
    </div>
}

function SystemParam() {
    const [visible, setVisible] = useState(false);
    return <div className="Param">
        <span className="Icon icon mb icon-delete-path"></span>
        <span className="SystemParam">
            <span className="Main" onClick={function () { setVisible(true) }}>全局自定义参数</span>
            <Drawer
                title="全局自定义参数"
                onClose={function () { setVisible(false) }}
                visible={visible}
                width={1000}
            >
                <p>敬请期待。。。</p>
            </Drawer>
        </span>
    </div>
}