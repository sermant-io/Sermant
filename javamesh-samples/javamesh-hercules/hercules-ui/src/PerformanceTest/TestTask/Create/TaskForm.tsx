import { Button, Checkbox, DatePicker, Form, FormInstance, Input, InputNumber, message, Modal, Radio, Select, Switch, Table, Tabs, Tooltip } from "antd"
import React, { useEffect, useRef, useState } from "react"
import ServiceSelect from "../../../component/ServiceSelect"
import HostForm from "./HostForm"
import { Area, AreaConfig } from '@ant-design/charts'
import "./TaskForm.scss"
import axios from "axios"
import { debounce } from "@antv/util"
import { useHistory } from "react-router"
import moment, { Moment } from "moment"
import PageInfo from "../../../component/PageInfo"

let params: any = {}
export default function App(props: { scenarioName?: string }) {
    let submit = false
    const history = useHistory()
    const [activeKey, setActiveKey] = useState("1")
    const [form] = Form.useForm()
    async function save(values: any, run?: boolean) {
        if (submit) return
        submit = true
        try {
            const data = { ...values, run }
            await axios.post("/argus/api/task", data)
            message.success("提交成功")
            if (props.scenarioName) {
                history.replace("/TestTask")
            } else {
                history.goBack()
            }
        } catch (e: any) {
            message.error(e.message)
        }
        submit = false
    }
    const [data, setData] = useState([])
    async function load() {
        try {
            const res = await axios.post('/argus/api/task/pressurePrediction', { params })
            setData(res.data.data)
        } catch (error: any) {
            
        }
    }
    const debounceRef = useRef(debounce(load, 1000))
    useEffect(function () {
        load()
        params = {}
    }, [])
    const config: AreaConfig = {
        smooth: true,
        data,
        xField: 'time',
        yField: 'pressure',
        xAxis: {
            range: [0, 1]
        },
        point: {},
        color: '#31baf3',
        areaStyle: () => {
            return {
                fill: 'l(270) 0:#ffffff 0.5:#e4f4fb 1:#31baf3',
            };
        },
    }
    return <Form className="TaskForm" form={form} requiredMark={false} labelCol={{ span: 4 }}
        initialValues={{
            scenario_name: props.scenarioName, is_monitor: true, sampling_interval: 2, sampling_ignore: 0,
            jvm_monitor: [
                "GC", "Thread", "Memory",
                "ClassLoading", "MemoryPool", "CPU"
            ]
        }}
        onFinish={save}
        onFinishFailed={function () {
            setActiveKey("1")
        }} >
        <Tabs activeKey={activeKey} onChange={setActiveKey}>
            <Tabs.TabPane tab="压测配置" key="1">
                <div className="Title">任务信息</div>
                <div className="Group">
                    <div className="Line">
                        <Form.Item label="任务名称" name="test_name" rules={[
                            { max: 64, required: true, whitespace: true },
                        ]}>
                            <Input placeholder="请输入任务名称" />
                        </Form.Item>
                        <Form.Item labelCol={{ span: 2 }} label="标签" name="label" rules={[
                            {
                                async validator(_, value?: string[]) {
                                    if (!value) return
                                    if (value.length > 4) {
                                        throw new Error("标签不能多于4个")
                                    }
                                    if (value.find(function (item) {
                                        return item.length > 16
                                    })) {
                                        throw new Error("单个标签不能长于16")
                                    }
                                }
                            },
                        ]}>
                            <Select mode="tags" placeholder="请输入标签" />
                        </Form.Item>
                    </div>
                    <Form.Item labelCol={{ span: 2 }} label="描述" name="desc">
                        <Input.TextArea showCount maxLength={256} autoSize={{ minRows: 1, maxRows: 1 }} placeholder="请输入描述" />
                    </Form.Item>
                </div>
                <div className="Line">
                    <div className="Left">
                        <div className="Title">压测配置</div>
                        <div className="Group">
                            <Form.Item label="代理数" name="agent" rules={[{ type: "integer", required: true }]}>
                                <AgentInput />
                            </Form.Item>
                            <Form.Item label="虚拟用户数" name="vuser" rules={[{ type: "integer", required: true }]}>
                                <InputNumber className="InputNumber" min={1} max={3000} addonAfter="最大值：3000" onChange={function (value) {
                                    params = { ...params, vuser: value }
                                    load()
                                }} />
                            </Form.Item>
                            <Form.Item label="场景" name="scenario_name" rules={[{ required: true }]}>
                                {props.scenarioName ? <Input disabled /> : <ServiceSelect url={"/argus/api/scenario/search"} />}
                            </Form.Item>
                            <Form.Item label="目标主机" name="hosts" rules={[{
                                async validator(_, values: []) {
                                    if (!values || values.length === 0) {
                                        throw new Error("请输入目标主机")
                                    }
                                }
                            }]}>
                                <TaskHosts />
                            </Form.Item>
                            <BasicScenario />
                            <div className="Line">
                                <Form.Item labelCol={{ span: 8 }} label="采样间隔" name="sampling_interval" rules={[{ type: "integer", required: true }]}>
                                    <InputNumber className="InputNumber" min={0} />
                                </Form.Item>
                                <Form.Item labelCol={{ span: 8 }} label="忽略采样数量" name="sampling_ignore" rules={[{ type: "integer", required: true }]}>
                                    <InputNumber className="InputNumber" min={0} />
                                </Form.Item>
                            </div>
                            <Form.Item labelCol={{ span: 4 }} name="test_param" label="测试参数" rules={[{
                                pattern: /^[\w,.|]+$/,
                                message: "格式错误"
                            }]}>
                                <Input.TextArea showCount maxLength={50} autoSize={{ minRows: 2, maxRows: 2 }}
                                    placeholder="测试参数可以在脚本中通过System.getProperty('param')取得，参数只能为数字、字母、下划线、逗号、圆点（.）或竖线(|)组成，禁止输入空格，长度在0-50之间。" />
                            </Form.Item>
                            <Form.Item labelCol={{ span: 5 }} valuePropName="checked" name="is_safe" label={
                                <span>安全文件分发
                                    <Tooltip title="如果以安全模式分发文件，速度较慢">
                                        <span className="Question icon fa fa-question-circle"></span>
                                    </Tooltip>
                                </span>
                            }>
                                <Checkbox />
                            </Form.Item>
                        </div>
                    </div>
                    <div className="TaskRight">
                        <div>
                            <div className="Title">
                                <span>压力递增</span>
                                <Form.Item className="Switch" valuePropName="checked" name="is_increased">
                                    <Switch size="small" onChange={function (value) {
                                        params = { ...params, is_increased: value }
                                        load()
                                    }} />
                                </Form.Item>
                            </div>
                        </div>
                        <div className="Group">
                            <Form.Item label="并发量" name="concurrency" initialValue="线程">
                                <Input disabled />
                            </Form.Item>
                            <Form.Item label="初始数" name="init_value" rules={[{ type: "integer" }]}>
                                <InputNumber className="InputNumber" min={0} onChange={function (value: any) {
                                    params = { ...params, init_value: value }
                                    debounceRef.current()
                                }} />
                            </Form.Item>
                            <Form.Item label="增量" name="increment" rules={[{ type: "integer" }]} >
                                <InputNumber className="InputNumber" min={0} onChange={function (value: any) {
                                    params = { ...params, increment: value }
                                    debounceRef.current()
                                }} />
                            </Form.Item>
                            <Form.Item label="初始等待时间" name="init_wait" rules={[{ type: "integer" }]}>
                                <InputNumber className="InputNumber" min={0} addonAfter="MS" onChange={function (value: any) {
                                    params = { ...params, init_wait: value }
                                    debounceRef.current()
                                }} />
                            </Form.Item>
                            <Form.Item label="进程增长间隔" name="growth_interval" rules={[{ type: "integer" }]}>
                                <InputNumber addonAfter="MS" min={0} className="InputNumber" onChange={function (value: any) {
                                    params = { ...params, growth_interval: value }
                                    debounceRef.current()
                                }} />
                            </Form.Item>
                            <div className="Title">压力预估图</div>
                            <Area className="Chart" height={250} {...config} />
                        </div>
                    </div>
                </div>
            </Tabs.TabPane>
            <Tabs.TabPane tab="监控配置" key="2">
                <Form.Item valuePropName="checked" name="is_monitor" label="机器监控" labelCol={{ span: 2 }}>
                    <Switch />
                </Form.Item>
                <Form.Item name="jvm_monitor" label="JVM监控" labelCol={{ span: 2 }}>
                    <Checkbox.Group style={{ width: "100%" }} options={[
                        "GC", "Thread", "Memory",
                        "ClassLoading", "MemoryPool", "CPU"
                    ]} />
                </Form.Item>
            </Tabs.TabPane>
        </Tabs>
        <Form.Item className="Buttons">
            <Button type="primary" htmlType="submit">保存</Button>
            <SaveRun form={form} onFinish={function (values) {
                const start_time = values.start_time
                if (start_time) {
                    values.start_time = start_time.format("YYYY-MM-DD HH:mm:ss")
                }
                save({ ...values, ...form.getFieldsValue() }, true)
            }} />
        </Form.Item>
    </Form>
}

function AgentInput(props: { onChange?: (value: number) => void }) {
    const [max, setMax] = useState(1)
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get('/argus/api/task/maxAgent')
                setMax(res.data.data)
            } catch (error: any) {

            }
        })()
    }, [])
    return <InputNumber className="InputNumber" min={1} max={max} addonAfter={"最大值：" + max} onChange={props.onChange} />
}

function SaveRun(props: { form: FormInstance<any>, onFinish: (values: any) => void }) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    return <>
        <Button type="primary" onClick={async function () {
            try {
                await props.form.validateFields()
                setIsModalVisible(true)
            } catch (error: any) {

            }
        }}>保存并运行</Button>
        <Modal className="SaveRun" title="启动代理" width={500} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form labelCol={{ span: 6 }} requiredMark={false} onFinish={props.onFinish}>
                <PageInfo>预约时间为空则立即执行</PageInfo>
                <Form.Item name="start_time" label="预约启动时间" rules={[{
                    async validator(_, value: Moment | null) {
                        if (value && value.isBefore(moment())) {
                            throw new Error("启动时间不得早于当前时间")
                        }
                    }
                }]}>
                    <DatePicker showTime />
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

function TaskHosts({ onChange }: { onChange?: (value: any) => void }) {
    type Hosts = { id: string }[]
    const [hosts, setHosts] = useState<Hosts>([])
    function updateHosts(hosts: Hosts) {
        onChange?.(hosts)
        setHosts(hosts)
    }
    return <div className="TaskHosts">
        <HostForm onFinish={async function (values) {
            updateHosts(hosts.concat({ ...values, id: (Math.random() * 1000000).toFixed(0) }))

        }}>
            <Button type="primary">添加目标主机</Button>
        </HostForm>
        <Table size="small" dataSource={hosts} rowKey="id" pagination={false} columns={[
            { dataIndex: "domain", title: "域名" },
            { dataIndex: "ip", title: "IP" },
            {
                dataIndex: "id", title: "操作", width: 100, render(id) {
                    return <span onClick={function () {
                        updateHosts(hosts.filter(function (item) { return item.id !== id }))
                    }} className="icon fa fa-trash-o"></span>
                }
            }
        ]} />
    </div>
}

function BasicScenario() {
    const [basic, setBasic] = useState(true)
    return <Form.Item className="BasicScenario" label="基础场景" name="basic" initialValue="by_time">
        <Radio.Group onChange={function (e) {
            setBasic(e.target.value === "by_time")
        }}>
            <Radio value="by_time">测试时长</Radio>
            <div>
                <Form.Item label="小时" className="WithoutLabel" name="by_time_h" rules={[{ type: "integer", required: basic }]}>
                    <InputNumber className="Time" min={0} />
                </Form.Item>
                <span className="Sep">:</span>
                <Form.Item label="分钟" className="WithoutLabel" name="by_time_m" rules={[{ type: "integer", required: basic }]}>
                    <InputNumber className="Time" min={0} max={60} />
                </Form.Item>
                <span className="Sep">:</span>
                <Form.Item label="秒" className="WithoutLabel" name="by_time_s" rules={[{ type: "integer", required: basic }]}>
                    <InputNumber className="Time" min={0} max={60} />
                </Form.Item>
                <span className="Format">HH:MM:SS</span>
            </div>
            <Radio value="by_count">测试次数</Radio>
            <div>
                <Form.Item label="次数" className="WithoutLabel" name="by_count" rules={[{ type: "integer", required: !basic }]}>
                    <InputNumber className="Count" min={0} max={10000} addonAfter="最大值：10000" />
                </Form.Item>
            </div>
        </Radio.Group>
    </Form.Item>
}