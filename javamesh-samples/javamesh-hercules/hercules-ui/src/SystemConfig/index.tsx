import { Button, Form, Input, message, Modal, Popconfirm, Select, Table } from "antd"
import React, { useEffect, useRef, useState } from "react"
import { PlusOutlined, CloseOutlined, SearchOutlined, UpOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import Card from "../component/Card"
import "./index.scss"
import axios from "axios"

type Data = { role: string, username: string, status: string }
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
            const res = await axios.get("/argus-user/api/user", { params })
            setData(res.data)
        } catch (e: any) {
            
        }
        setLoading(false)
    }
    async function activeUser() {
        if (selectedRowKeys.length === 0) {
            return
        }
        if (data.data.find(function(item){
            if (item.status === "正常") {
                return selectedRowKeys.includes(item.username)
            }
            return false
        })) {
            message.error("无法启用正常用户")
            return
        }
        if (submit) return
        submit = true
        Modal.confirm({
            title: '是否启用这些用户？',
            icon: <ExclamationCircleOutlined />,
            okType: 'danger',
            async onOk() {
                try {
                    await axios.post("/argus-user/api/user/batchActive", { username: selectedRowKeys })
                    message.success("启用成功")
                    load()
                } catch (e: any) {
                    message.error(e.message)
                }
            }
        })
        submit = false
        
    }
    async function deactiveUser() {
        if (selectedRowKeys.length === 0) {
            return
        }
        if (data.data.find(function(item){
            if (item.status === "失效") {
                return selectedRowKeys.includes(item.username)
            }
            return false
        })) {
            message.error("无法禁用失效用户")
            return
        }
        if (submit) return
        submit = true
        Modal.confirm({
            title: '是否禁用这些用户？',
            icon: <ExclamationCircleOutlined />,
            okType: 'danger',
            async onOk() {
                try {
                    await axios.post("/argus-user/api/user/batchDeactive", { username: selectedRowKeys })
                    message.success("禁用成功")
                    load()
                } catch (e: any) {
                    message.error(e.message)
                }
            }
        })
        submit = false
        
    }

    useEffect(function () {
        stateRef.current = {}
        load()
    }, [])
    return <div className="UserManagement">
        <Card>
            <div className="ToolBar">
                <AddUser load={load} />
                <Button className="Button" icon={<CloseOutlined />} onClick={deactiveUser}>禁用账号</Button>
                <Button icon={<UpOutlined />} onClick={activeUser}>启用账号</Button>
                <div className="Space"></div>
                <Form layout="inline" onFinish={function (values) {
                    stateRef.current.search = values
                    load()
                }}>
                    <Form.Item name="username">
                        <Input className="Input" placeholder="登录账号" />
                    </Form.Item>
                    <Form.Item name="nickname">
                        <Input className="Input" placeholder="用户名称" />
                    </Form.Item>
                    <Form.Item name="role">
                        <Select placeholder="用户角色" allowClear style={{ width: 150 }} options={[{ value: "操作员" }, { value: "审核员" }, { value: "管理员" }]} />
                    </Form.Item>
                    <Form.Item name="status">
                        <Select placeholder="用户状态" allowClear style={{ width: 150 }} options={[{ value: "正常" }, { value: "失效" }]} />
                    </Form.Item>
                    <Button htmlType="submit" icon={<SearchOutlined />}>查找</Button>
                </Form>
            </div>
            <Table size="middle" rowKey="username" dataSource={data.data} loading={loading}
                onChange={function (pagination, filters, sorter) {
                    stateRef.current = { ...stateRef.current, pagination, filters, sorter }
                    load()
                }}
                rowSelection={{
                    selectedRowKeys, onChange(selectedRowKeys) {
                        setSelectedRowKeys(selectedRowKeys)
                    }
                }}
                pagination={{ total: data.total, size: "small", showTotal() { return `共 ${data.total} 条` }, showSizeChanger: true }}
                columns={[
                    {
                        title: "登录账号",
                        dataIndex: "username",
                        ellipsis: true
                    },
                    {
                        title: "用户名称",
                        dataIndex: "nickname",
                        ellipsis: true
                    },
                    {
                        title: "用户角色",
                        dataIndex: "role",
                        ellipsis: true
                    },
                    {
                        title: "用户状态",
                        dataIndex: "status",
                        ellipsis: true
                    },
                    {
                        title: "更新时间",
                        dataIndex: "update_time",
                        width: 200,
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "操作",
                        width: 150,
                        dataIndex: "username",
                        render(username, record) {
                            return <>
                                <UpdateUser data={record} load={load} />
                                <Popconfirm disabled={record.role === "管理员"} title="是否重置密码？" onConfirm={async function () {
                                    if (submit) return
                                    submit = true
                                    try {
                                        const res = await axios.post("/argus-user/api/user/resetPwd", { username })
                                        load()
                                        Modal.confirm({
                                            title: "密码重置成功",
                                            content: `登录账号：${res.data.data.username} 密码：${res.data.data.password}`
                                        })
                                    } catch (error: any) {
                                        message.error(error.message)
                                    }
                                    submit = false
                                }}>
                                    <Button type="link" disabled={record.role === "管理员"} size="small">密码重置</Button>
                                </Popconfirm>

                            </>
                        }
                    },
                ]}
            />
        </Card>
    </div>
}

function AddUser(props: { load: () => void }) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [form] = Form.useForm();
    return <>
        <Button className="Button" type="primary" icon={<PlusOutlined />} onClick={function () { setIsModalVisible(true) }}>添加账号</Button>
        <Modal className="AddUser" title="添加账号" width={400} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form form={form} requiredMark={false} onFinish={async function (values) {
                try {
                    const res = await axios.post("/argus-user/api/user", values)
                    form.resetFields()
                    setIsModalVisible(false)
                    props.load()
                    Modal.confirm({
                        title: "用户创建成功",
                        content: `登录账号：${res.data.data.username} 密码：${res.data.data.password}`,
                    })
                } catch (error: any) {
                    message.error(error.message)
                }
            }}>
                <Form.Item name="username" label="登录账号" rules={[{
                    required: true,
                    pattern: /^\w{6,15}$/,
                    message: "不得少于6个字且不得超过15个字，只能输入字母、数字、下划线"
                }]}>
                    <Input />
                </Form.Item>
                <Form.Item name="nickname" label="用户名称" rules={[{ required: true, max: 15 }]}>
                    <Input />
                </Form.Item>
                <Form.Item name="role" label="用户角色" rules={[{ required: true }]}>
                    <Select options={[{ value: "操作员" }, { value: "审核员" }, { value: "管理员" }]} />
                </Form.Item>
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

function UpdateUser(props: { data: Data, load: () => {} }) {
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [form] = Form.useForm();
    return <>
        <Button disabled={props.data.username === "admin"} type="link" size="small" onClick={function () { setIsModalVisible(true) }}>修改</Button>
        <Modal className="UpdateUser" title="修改" width={400} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form form={form} initialValues={props.data} requiredMark={false} onFinish={async function (values) {
                try {
                    await axios.put("/argus-user/api/user", values)
                    message.success("用户修改成功")
                    setIsModalVisible(false)
                    props.load()
                } catch (error: any) {
                    message.error(error.message)
                }
            }}>
                <Form.Item name="username" label="登录账号">
                    <Input disabled />
                </Form.Item>
                <Form.Item name="nickname" label="用户名称" rules={[{ required: true, max: 15 }]}>
                    <Input />
                </Form.Item>
                <Form.Item name="role" label="用户角色" rules={[{ required: true }]}>
                    <Select options={[{ value: "操作员" }, { value: "审核员" }, { value: "管理员" }]} />
                </Form.Item>
                <Form.Item className="Buttons">
                    <Button type="primary" htmlType="submit">修改</Button>
                    <Button onClick={function () {
                        setIsModalVisible(false)
                    }}>取消</Button>
                </Form.Item>
            </Form>
        </Modal>
    </>
}