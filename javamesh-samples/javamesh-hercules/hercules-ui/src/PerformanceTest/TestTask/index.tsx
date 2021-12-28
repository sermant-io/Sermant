import { Button, Checkbox, DatePicker, Form, Input, message, Modal, Table } from "antd"
import React, { useEffect, useRef, useState } from "react"
import Breadcrumb from "../../component/Breadcrumb"
import Card from "../../component/Card"
import "./index.scss"
import { PlusOutlined, CloseOutlined, SearchOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import { Link, Route, useRouteMatch } from "react-router-dom"
import Create from "./Create"
import CacheRoute, { CacheSwitch, useDidRecover } from "react-router-cache-route"
import axios from "axios"
import Detail from "./Detail"
import ServiceSelect from "../../component/ServiceSelect"
import moment, { Moment } from "moment"
import PageInfo from "../../component/PageInfo"
import socket from "../socket"

export default function App() {
    const { path } = useRouteMatch();
    return <CacheSwitch>
        <CacheRoute exact path={path} component={Home} />
        <Route exact path={`${path}/Create`}><Create /></Route>
        <Route exact path={`${path}/Detail`}><Detail /></Route>
    </CacheSwitch>
}

type Data = {
    status: string,
    test_name: string,
    test_type: string,
    script_path: string,
    owner: string,
    test_id: string,
    status_label: string
}

function Home() {
    let submit = false
    let { path } = useRouteMatch();
    const [data, setData] = useState<{ data: Data[], total: number }>({ data: [], total: 0 })
    const [loading, setLoading] = useState(false)
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
    const stateRef = useRef<any>({})
    const keysRef = useRef<string[]>([])
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
            const res = await axios.get("/argus/api/task", { params })
            setData(res.data)
            // 需要监听的任务列表
            keysRef.current.length = 0
            res.data.data.forEach(function (item: Data) {
                keysRef.current.push("/task/" + item.test_id)
            })
        } catch (e: any) {

        }
        setLoading(false)
    }
    function batchDelete(selectedRowKeys: React.Key[]) {
        if (submit) return
        submit = true
        Modal.confirm({
            title: '是否删除？',
            icon: <ExclamationCircleOutlined />,
            content: '删除后无法恢复，请谨慎操作',
            okType: 'danger',
            async onOk() {
                try {
                    await axios.delete("/argus/api/task", { params: { test_id: selectedRowKeys.join(",") } })
                    message.success("删除成功")
                    load()
                } catch (e: any) {
                    message.error(e.message)
                    throw e
                }
            },
        })
        submit = false
    }
    useEffect(function () {
        stateRef.current = {}
        load()
        function handleSocket(event: MessageEvent<any>) {
            const message = event.data
            if (keysRef.current.includes(message)) {
                keysRef.current.length = 0
                load()
            }
        }
        socket.addEventListener("message", handleSocket)
        return function () {
            socket.removeEventListener("message", handleSocket)
        }
    }, [])
    useDidRecover(load)
    const statusMap = new Map<string, string>()
    statusMap.set("running", "#1A99FE")
    statusMap.set("pending", "#8090B0")
    statusMap.set("success", "#2BBF2A")
    statusMap.set("fail", "#FF4E4E")
    return <div className="TestTask">
        <Breadcrumb label="压测任务" />
        <Card>
            <div className="ToolBar">
                <Link to={`${path}/Create`}><Button className="Add" type="primary" icon={<PlusOutlined />}>创建任务</Button></Link>
                <Button icon={<CloseOutlined />} onClick={function () {
                    if (selectedRowKeys.length === 0) {
                        return
                    }
                    batchDelete(selectedRowKeys)
                }}>批量删除</Button>
                <Form className="Form" layout="inline" onFinish={function (values) {
                    stateRef.current.search = values
                    load()
                }}>
                    <Form.Item name="status">
                        <Checkbox.Group options={[{ label: '正在运行', value: "running" }, { label: '已预约', value: "pending" }]} onChange={function (value) {
                            stateRef.current.search = { ...stateRef.current.search, status: value }
                            load()
                        }} />
                    </Form.Item>
                    <div className="Space"></div>
                    <Form.Item style={{ width: "150px" }} name="label">
                        <ServiceSelect allowClear placeholder="标签" url={"/argus/api/task/tags"} />
                    </Form.Item>
                    <Form.Item name="keywords">
                        <Input className="Input" placeholder="名称或描述" />
                    </Form.Item>
                    <Button htmlType="submit" icon={<SearchOutlined />}>查找</Button>
                </Form>
            </div>
            <Table size="middle" loading={loading} dataSource={data.data} rowKey="test_id"
                rowSelection={{
                    selectedRowKeys, onChange(selectedRowKeys) {
                        setSelectedRowKeys(selectedRowKeys)
                    }
                }}
                onChange={function (pagination, filters, sorter) {
                    stateRef.current = { ...stateRef.current, pagination, filters, sorter }
                    load()
                }}
                pagination={{ total: data.total, size: "small", showTotal() { return `共 ${data.total} 条` }, showSizeChanger: true }}
                columns={[
                    {
                        title: "状态",
                        dataIndex: "status",
                        render(_, record) {
                            return <div title={record.status_label}>
                                <span className="icon md"
                                    style={{ fontSize: 24, color: statusMap.get(record.status) }}>lightbulb_outline</span>
                            </div>
                        },
                        align: "center",
                        width: 50,
                        ellipsis: true
                    },
                    {
                        title: "测试名称",
                        width: 120,
                        ellipsis: true,
                        dataIndex: "test_name",
                        sorter: true,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.test_name)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }()
                    },
                    {
                        title: "压测类型",
                        dataIndex: "test_type",
                        width: 100,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.test_type)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "脚本文件名",
                        dataIndex: "script_path",
                        width: 100,
                        ellipsis: true,
                        render(data) {
                            return <Link to={`/PerformanceTest/ScriptManage/Detail?path=${data}`}>{data}</Link>
                        },
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.script_path)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                    },
                    {
                        title: "所有者",
                        dataIndex: "owner",
                        width: 80,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.owner)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "标签",
                        dataIndex: "label",
                        width: 100,
                        ellipsis: true,
                        render(value) {
                            return value?.join(",")
                        },
                    },
                    {
                        title: "描述",
                        dataIndex: "desc",
                        ellipsis: true,
                    },
                    {
                        title: "开始时间",
                        dataIndex: "start_time",
                        sorter: true,
                        width: 150,
                        ellipsis: true
                    },
                    {
                        title: "持续阈值",
                        width: 80,
                        dataIndex: "duration",
                        ellipsis: true
                    },
                    {
                        title: "TPS",
                        width: 80,
                        dataIndex: "tps",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "MTT",
                        width: 80,
                        dataIndex: "mtt",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "出错率",
                        width: 80,
                        dataIndex: "fail_rate",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "操作",
                        width: 150,
                        align: "center",
                        render(_, record) {
                            return <div className="Operation">
                                {record.status === "running" ? <Button type="primary" size="small" onClick={async function () {
                                    try {
                                        await axios.post("/argus/api/task/stop", { test_id: record.test_id })
                                        message.success("停止成功")
                                    } catch (e: any) {
                                        message.error(e.message)
                                    }
                                    load()
                                }} style={{ backgroundColor: "#FF4E4E", borderColor: "#FF4E4E", marginRight: 10 }}
                                >停止</Button> : <StartButton load={load} test_id={record.test_id} />}
                                <Link to={`${path}/Detail?test_id=${record.test_id}`}>查看</Link>
                                <Button type="link" size="small" onClick={function () {
                                    batchDelete([record.test_id])
                                }}
                                >删除</Button>
                            </div>
                        }
                    }
                ]} />
        </Card>
    </div>
}

function StartButton(props: { load: () => void, test_id: string }) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    return <>
        <Button type="primary" size="small" onClick={function () {
            setIsModalVisible(true)
        }} style={{ backgroundColor: "#41C170", borderColor: "#41C170", marginRight: 10 }}
        >启动</Button>
        <Modal className="StartButton" title="启动代理" width={500} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form labelCol={{ span: 6 }} requiredMark={false} onFinish={async function (values) {
                const start_time = values.start_time
                if (start_time) {
                    values.start_time = start_time.format("YYYY-MM-DD HH:mm:ss")
                }
                try {
                    const data = { ...values, test_id: props.test_id }
                    await axios.post("/argus/api/task/start", data)
                    setIsModalVisible(false)
                    message.success("提交成功")
                    props.load()
                } catch (e: any) {
                    message.error(e.message)
                }
            }}>
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