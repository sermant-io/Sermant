import { Button, DatePicker, Form, Input, message, Modal, Popconfirm, Table } from "antd"
import React, { useContext, useEffect, useRef, useState } from "react"
import { Link, Route, useHistory, useRouteMatch } from "react-router-dom"
import Breadcrumb from "../../component/Breadcrumb"
import Card from "../../component/Card"
import { SearchOutlined, PlusOutlined } from '@ant-design/icons'
import "./index.scss"
import axios from "axios"
import Editor from "./Editor"
import CacheRoute, { CacheSwitch, useDidRecover } from "react-router-cache-route"
import Context from "../../ContextProvider"
import ServiceSelect from "../../component/ServiceSelect"
import PageInfo from "../../component/PageInfo"
import moment, { Moment } from "moment"
import ApproveFormItems from "../ApproveFormItems"
import { useForm } from "antd/lib/form/Form"

export default function App() {
    const { path } = useRouteMatch();
    return <CacheSwitch>
        <CacheRoute exact path={path} component={Home} />
        <Route exact path={path + '/Editor'}><Editor /></Route>
    </CacheSwitch>
}

type Data = { plan_id: string, expand: { key: string, scena_no: string, task_no: string }[], status_label: string, status: string, history_id: string }
function Home() {
    const { path } = useRouteMatch()
    const { auth } = useContext(Context)
    const [data, setData] = useState<{ data: Data[], total: number }>()
    const [loading, setLoading] = useState(false)
    const stateRef = useRef<any>({})
    async function load() {
        setLoading(true)
        try {
            const params = {
                pageSize: stateRef.current.pagination?.pageSize || 10,
                current: stateRef.current.pagination?.current,
                sorter: stateRef.current.sorter?.field,
                order: stateRef.current.sorter?.order,
                ...stateRef.current.search,
                ...stateRef.current.filters
            }
            const res = await axios.get("/argus-emergency/api/plan", { params })
            setData(res.data)
        } catch (error: any) {

        }
        setLoading(false)
    }
    useEffect(function () {
        load()
    }, [])
    useDidRecover(load)
    return <div className="PlanManage">
        <Breadcrumb label="预案管理" />
        {data && <Card>
            <div className="ToolBar">
                <AddPlan />
                <div className="Space"></div>
                <Form layout="inline" onFinish={function (values) {
                    stateRef.current.search = values
                    load()
                }}>
                    <Form.Item name="plan_name_no">
                        <Input placeholder="预案名称、编号" />
                    </Form.Item>
                    <Form.Item name="scena_name_no">
                        <Input placeholder="场景名称、编号" />
                    </Form.Item>
                    <Form.Item name="task_name_no">
                        <Input placeholder="任务名称、编号" />
                    </Form.Item>
                    <Form.Item name="script_name">
                        <Input placeholder="脚本名称" />
                    </Form.Item>
                    <Form.Item className="Input" name="status_label">
                        <ServiceSelect placeholder="预案状态" url="/argus-emergency/api/plan/search/status_label" allowClear />
                    </Form.Item>
                    <Button htmlType="submit" icon={<SearchOutlined />}>查找</Button>
                </Form>
            </div>
            <Table size="middle" dataSource={data.data} loading={loading} rowKey="plan_id"
                onChange={function (pagination, filters, sorter) {
                    stateRef.current = { ...stateRef.current, pagination, filters, sorter }
                    load()
                }}
                pagination={{ total: data.total, size: "small", showTotal() { return `共 ${data.total} 条` }, showSizeChanger: true }}
                expandable={{
                    defaultExpandedRowKeys: data.data.slice(0, 1).map(function (item) { return item.plan_id }),
                    expandedRowRender(record) {
                        const scenaRowSpans: Map<string, number> = new Map()
                        const taskRowSpans: Map<string, number> = new Map()
                        if (record.expand.length > 0) {
                            scenaRowSpans.set(record.expand[0].key, 1)
                            record.expand.reduce(function (prev, curr) {
                                if (curr.scena_no && curr.scena_no === prev.scena_no) {
                                    scenaRowSpans.set(prev.key, (scenaRowSpans.get(prev.key) || 0) + 1)
                                    scenaRowSpans.set(curr.key, 0)
                                    return prev
                                }
                                scenaRowSpans.set(curr.key, 1)
                                return curr
                            })
                            taskRowSpans.set(record.expand[0].key, 1)
                            record.expand.reduce(function (prev, curr) {
                                if (curr.task_no && curr.task_no === prev.task_no) {
                                    taskRowSpans.set(prev.key, (taskRowSpans.get(prev.key) || 0) + 1)
                                    taskRowSpans.set(curr.key, 0)
                                    return prev
                                }
                                taskRowSpans.set(curr.key, 1)
                                return curr
                            })
                        }
                        return <Table size="small" className="TreeTable" rowKey="key" pagination={false} dataSource={record.expand}
                            columns={[
                                {
                                    title: "场景编号", dataIndex: "scena_no", ellipsis: true,
                                    render(value, row) {
                                        return { children: value, props: { rowSpan: scenaRowSpans.get(row.key) }, }
                                    }
                                },
                                {
                                    title: "场景名称", dataIndex: "scena_name", ellipsis: true,
                                    render(value, row) {
                                        return { children: value, props: { rowSpan: scenaRowSpans.get(row.key) }, }
                                    }
                                },
                                {
                                    title: "任务编号", dataIndex: "task_no", ellipsis: true,
                                    render(value, row) {
                                        return { children: value, props: { rowSpan: taskRowSpans.get(row.key) }, }
                                    }
                                },
                                {
                                    title: "任务名称", dataIndex: "task_name", ellipsis: true,
                                    render(value, row) {
                                        return { children: value, props: { rowSpan: taskRowSpans.get(row.key) }, }
                                    }
                                },
                                { title: "子任务编号", dataIndex: "subtask_no", ellipsis: true },
                                { title: "子任务名称", dataIndex: "subtask_name", ellipsis: true },
                                { title: "通道类型", dataIndex: "channel_type", ellipsis: true },
                                { title: "脚本名称", dataIndex: "script_name", ellipsis: true },
                                { title: "脚本用途", dataIndex: "submit_info", ellipsis: true },
                            ]}
                        />
                    }
                }}
                columns={[
                    { title: "预案编号", dataIndex: "plan_no", sorter: true, ellipsis: true },
                    { title: "预案名称", dataIndex: "plan_name", ellipsis: true },
                    { title: "预案状态", dataIndex: "status_label", ellipsis: true },
                    { title: "创建时间", dataIndex: "create_time", sorter: true, ellipsis: true },
                    { title: "创建人", dataIndex: "creator", ellipsis: true },
                    { title: "备注", dataIndex: "comment", ellipsis: true },
                    {
                        title: "操作", width: 350, dataIndex: "plan_id", render(plan_id, record) {
                            return <>
                                {auth.includes("operator") && <CopyPlan plan_id={plan_id}/>}
                                {record.status !== "running" && auth.includes("operator") && <Link to={path + "/Editor?plan_id=" + plan_id}>
                                    <Button type="link" size="small">修改</Button>
                                </Link>}
                                {record.status === "approving" && auth.includes("approver") && <ApprovePlan plan_id={plan_id} load={load} />}
                                {record.status === "unapproved" && auth.includes("operator") && <Popconfirm title="是否提交审核？" onConfirm={async function () {
                                    try {
                                        await axios.post('/argus-emergency/api/plan/submitReview', { plan_id })
                                        message.success("提交成功")
                                        load()
                                    } catch (error: any) {
                                        message.error(error.message)
                                    }
                                }} >
                                    <Button type="link" size="small">提审</Button>
                                </Popconfirm>}
                                {(record.status === "approved" || record.status === "ran") && auth.includes("operator") && <Button type="link" size="small">立即执行</Button>}
                                {(record.status === "approved" || record.status === "ran") && auth.includes("operator") && <RunPlan plan_id={plan_id} load={load} />}
                                {record.status === "wait" && auth.includes("operator") && <Popconfirm title="是否取消预约？" onConfirm={async function () {
                                    try {
                                        await axios.post("/argus-emergency/api/plan/cancel", { plan_id })
                                        message.success("取消成功")
                                        load()
                                    } catch (error: any) {
                                        message.error(error.message)
                                    }
                                }}>
                                    <Button type="link" size="small">取消预约</Button>
                                </Popconfirm>}
                                {(record.status === "running" || record.status === "ran") && <Link to={"/DisasterRecovery/RunningLog/Detail?history_id=" + record.history_id}>
                                    <Button type="link" size="small">日志</Button>
                                </Link>}
                            </>
                        }
                    }
                ]}
            />
        </Card>}
    </div>
}

function AddPlan() {
    let submit = false
    const { auth } = useContext(Context)
    const history = useHistory();
    const { path } = useRouteMatch()
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [form] = useForm()
    return <>
        <Button disabled={!auth.includes("operator")} type="primary" icon={<PlusOutlined />} onClick={function () { setIsModalVisible(true) }}>添加预案</Button>
        <Modal className="AddPlan" title="添加预案" width={700} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form form={form} requiredMark={false} onFinish={async function (values) {
                if (submit) return
                submit = true
                try {
                    const res = await axios.post("/argus-emergency/api/plan", values)
                    setIsModalVisible(false)
                    form.resetFields()
                    history.push(path + "/Editor?plan_id=" + res.data.data.plan_id)
                } catch (error: any) {
                    message.error(error.message)
                }
                submit = false
            }}>
                <Form.Item label="预案名称" name="plan_name" rules={[{ required: true, max: 64 }]}><Input /></Form.Item>
                <Form.Item className="Buttons">
                    <Button type="primary" htmlType="submit">创建</Button>
                    <Button onClick={function () {
                        setIsModalVisible(false)
                    }}>取消</Button>
                </Form.Item>
            </Form>
        </Modal>
    </>
}

function CopyPlan({plan_id}: { plan_id: string}) {
    let submit = false
    const history = useHistory();
    const { path } = useRouteMatch()
    const [isModalVisible, setIsModalVisible] = useState(false);
    return <>
        <Button type="link" onClick={function () { setIsModalVisible(true) }}>克隆</Button>
        <Modal className="CopyPlan" title="克隆" width={700} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form requiredMark={false} onFinish={async function (values) {
                if (submit) return
                submit = true
                try {
                    const res = await axios.post("/argus-emergency/api/plan/copy", {...values, plan_id })
                    setIsModalVisible(false)
                    history.push(path + "/Editor?plan_id=" + res.data.data.plan_id)
                } catch (error: any) {
                    message.error(error.message)
                }
                submit = false
            }}>
                <Form.Item label="预案名称" name="plan_name" rules={[{ required: true, max: 64 }]}><Input /></Form.Item>
                <Form.Item className="Buttons">
                    <Button type="primary" htmlType="submit">创建</Button>
                    <Button onClick={function () {
                        setIsModalVisible(false)
                    }}>取消</Button>
                </Form.Item>
            </Form>
        </Modal>
    </>
}

function ApprovePlan(props: { plan_id: string, load: () => {} }) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    return <>
        <Button type="link" size="small" onClick={function () { setIsModalVisible(true) }}>审核</Button>
        <Modal className="ApprovePlan" title="审核预案" width={400} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form className="Form" requiredMark={false} onFinish={async function (values) {
                try {
                    await axios.post("/argus-emergency/api/plan/approve", { ...values, plan_id: props.plan_id })
                    setIsModalVisible(false)
                    message.success("提交成功")
                    props.load()
                } catch (error: any) {
                    message.error(error.message)
                }
            }}>
                <ApproveFormItems />
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

function RunPlan(props: { plan_id: string, load: () => {} }) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const history = useHistory();
    return <>
        <Button type="link" size="small" onClick={function () {
            setIsModalVisible(true)
        }} >预约执行</Button>
        <Modal className="RunPlan" title="预约执行" width={500} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form labelCol={{ span: 6 }} requiredMark={false} onFinish={async function (values) {
                const start_time = values.start_time
                const plan_id = props.plan_id
                try {
                    if (start_time) {
                        values.start_time = start_time.format("YYYY-MM-DD HH:mm:ss")
                        const data = { ...values, plan_id }
                        await axios.post("/argus-emergency/api/plan/schedule", data)
                        setIsModalVisible(false)
                        props.load()
                    } else {
                        const res = await axios.post("/argus-emergency/api/plan/run", { plan_id })
                        history.push("/DisasterRecovery/RunningLog/Detail?history_id=" + res.data.data.history_id)
                    }
                    message.success("提交成功")
                } catch (e: any) {
                    message.error(e.message)
                }
            }}>
                <PageInfo>预约时间为空则立即执行</PageInfo>
                <Form.Item name="start_time" label="预约启动时间" rules={[
                    { required: true },
                    {
                        async validator(_, value: Moment | null) {
                            if (value && value.isBefore(moment())) {
                                throw new Error("启动时间不得早于当前时间")
                            }
                        }
                    }
                ]}>
                    <DatePicker showTime showNow={false} />
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