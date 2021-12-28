import { Button, Form, Input, InputNumber, message, Modal, Radio, Table } from "antd"
import React, { useEffect, useRef, useState } from "react"
import Breadcrumb from "../../component/Breadcrumb"
import Card from "../../component/Card"
import { SearchOutlined, PlusOutlined, ExclamationCircleOutlined, CloseOutlined, PauseOutlined } from '@ant-design/icons'
import "./index.scss"
import ServiceSelect from "../../component/ServiceSelect"
import axios from "axios"
import { useForm } from "antd/lib/form/Form"

type Data = { server_id: string, status: string, status_label: string }

export default function App() {
    let submit = false
    const [data, setData] = useState<{ data: Data[], total: number }>({ data: [], total: 0 })
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
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
            const res = await axios.get("/argus-emergency/api/host", { params })
            setData(res.data)
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
                    await axios.delete("/argus-emergency/api/host", { params: { server_id: selectedRowKeys } })
                    message.success("删除成功")
                    load()
                } catch (e: any) {
                    message.error(e.message)
                }
                load()
            },
        })
        submit = false
    }
    async function batchStop(selectedRowKeys: React.Key[]) {
        try {
            await axios.post("/argus-emergency/api/host/stop", { server_id: selectedRowKeys })
            message.success("停止成功")
        } catch (e: any) {
            message.error(e.message)
        }
        load()
    }
    useEffect(function () {
        load()
    }, [])
    const statusMap = new Map<string, string>()
    statusMap.set("running", "#1A99FE")
    statusMap.set("pending", "#8090B0")
    statusMap.set("success", "#2BBF2A")
    statusMap.set("fail", "#FF4E4E")
    return <div className="HostManage">
        <Breadcrumb label="主机管理" />
        <Card>
            <div className="ToolBar">
                <AddHost load={load} />
                <Button className="Delete" icon={<CloseOutlined />} onClick={function () {
                    if (selectedRowKeys.length === 0) {
                        return
                    }
                    batchDelete(selectedRowKeys)
                }}>批量删除</Button>
                <Button icon={<PauseOutlined />} onClick={function () {
                    if (selectedRowKeys.length === 0) {
                        return
                    }
                    batchStop(selectedRowKeys)
                }}>停止</Button>
                <div className="Space"></div>
                <Form layout="inline" onFinish={function (values) {
                    stateRef.current.search = values
                    load()
                }}>
                    <Form.Item name="keywords">
                        <Input placeholder="Keywords" />
                    </Form.Item>
                    <Button htmlType="submit" icon={<SearchOutlined />}>查找</Button>
                </Form>
            </div>
            <Table size="middle" rowKey="server_id" loading={loading} dataSource={data.data}
                rowSelection={{ selectedRowKeys, onChange(selectedRowKeys) { setSelectedRowKeys(selectedRowKeys) } }}
                pagination={{ total: data.total, size: "small", showTotal() { return `共 ${data.total} 条` }, showSizeChanger: true }}
                onChange={function (pagination, filters, sorter) {
                    stateRef.current = { ...stateRef.current, pagination, filters, sorter }
                    load()
                }}
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
                        width: 80,
                        sorter: true,
                        ellipsis: true
                    },
                    { ellipsis: true, title: "主机名称", dataIndex: "server_name" },
                    { ellipsis: true, title: "服务器IP", dataIndex: "server_ip" },
                    { ellipsis: true, title: "SSH用户", dataIndex: "server_user" },
                    { ellipsis: true, title: "有无密码", dataIndex: "have_password" },
                    { ellipsis: true, title: "密码获取", dataIndex: "password_mode" },
                    { ellipsis: true, title: "Agent端口", dataIndex: "agent_port" },
                    {
                        title: "许可",
                        dataIndex: "licensed",
                        width: 200,
                        render(licensed, record) {
                            if (licensed === undefined) return null
                            return <span className={`Licensed${licensed === true ? " active" : " deactive"}`} onClick={async function () {
                                try {
                                    await axios.post('/argus-emergency/api/host/license', { server_id: record.server_id, licensed: !licensed })
                                    message.success("修改成功")
                                    load()
                                } catch (error: any) {
                                    message.error(error.message)
                                }
                            }}>
                                <span>未许可</span>
                                <span>已许可</span>
                            </span>
                        }
                    },
                ]}
            />
        </Card>
    </div>
}

function AddHost(props: { load: () => void }) {
    const [isModalVisible, setIsModalVisible] = useState(false)
    const [hasPwd, setHasPwd] = useState(false)
    const [isLocal, setIsLocal] = useState(true)
    const [form] = useForm()
    return <>
        <Button type="primary" icon={<PlusOutlined />} onClick={function () { setIsModalVisible(true) }}>添加主机</Button>
        <Modal className="AddHost" title="添加主机" visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () { setIsModalVisible(false) }}>
            <Form form={form} requiredMark={false} labelCol={{ span: 4 }}
                initialValues={{ have_password: "无", password_mode: "本地", server_port: 22 }}
                onFinish={async function (values) {
                    try {
                        await axios.post("/argus-emergency/api/host", values)
                        form.resetFields()
                        setIsModalVisible(false)
                        props.load()
                        message.success("创建成功")
                    } catch (error: any) {
                        message.error("创建失败")
                    }
                }}
            >
                <Form.Item name="server_name" label="主机名称" rules={[{ required: true, max: 32 }]}>
                    <Input />
                </Form.Item>
                <div className="Line">
                    <Form.Item labelCol={{ span: 8 }} name="server_ip" label="服务器IP" rules={[{
                        required: true,
                        pattern: /^((25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))$/,
                        message: "请输入IP地址"
                    }]}>
                        <Input />
                    </Form.Item>
                    <Form.Item labelCol={{ span: 6 }} name="server_port" label="端口" rules={[{ type: "integer", required: true }]}>
                        <InputNumber min={0} max={65535} />
                    </Form.Item>
                </div>
                <Form.Item name="have_password" label="有无密码">
                    <Radio.Group options={["无", "有"]} onChange={function (e) {
                        setHasPwd(e.target.value === "有")
                    }} />
                </Form.Item>
                {hasPwd && <Form.Item name="password_mode" label="密码获取">
                    <Radio.Group options={["本地", "平台"]} onChange={function (e) {
                        setIsLocal(e.target.value === "本地")
                    }} />
                </Form.Item>}
                {hasPwd && !isLocal && <Form.Item name="password_uri" label="密码平台" rules={[{ required: true }]}>
                    <ServiceSelect url="/argus-emergency/api/host/search/password_uri" />
                </Form.Item>}
                {hasPwd && isLocal && <Form.Item name="server_user" label="SSH用户" rules={[{ required: true, max: 32 }]}>
                    <Input />
                </Form.Item>}
                {hasPwd && isLocal && <Form.Item name="password" label="密码" rules={[{ required: true, max: 32 }]}>
                    <Input />
                </Form.Item>}
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