import { Button, Form, Input, message, Modal, Table } from "antd"
import React, { useEffect, useRef, useState } from "react"
import Breadcrumb from "../../component/Breadcrumb"
import Card from "../../component/Card"
import PageInfo from "../../component/PageInfo"
import "./index.scss"
import { PlusOutlined, CloseOutlined, SearchOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import { Link, Route, useRouteMatch } from "react-router-dom"
import Create from "./Create"
import axios from "axios"
import ScenarioFormItems from "./ScenarioFormItems"
import CacheRoute, { CacheSwitch, useDidRecover } from "react-router-cache-route"

export default function App() {
    let { path } = useRouteMatch();
    return <CacheSwitch>
        <CacheRoute exact path={path} component={Home} />
        <Route exact path={`${path}/Create`}><Create /></Route>
    </CacheSwitch>
}

type Data = {
    create_by: string,
    app_name: string,
    scenario_type: string,
    scenario_id: string
}
function Home() {
    let submit = false
    let { path } = useRouteMatch();
    const [data, setData] = useState<{ data: Data[], total: number }>({ data: [], total: 0 })
    const [loading, setLoading] = useState(false)
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
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
            const res = await axios.get("/argus/api/scenario", { params })
            setData(res.data)
        } catch (e: any) {
            
        }
        setLoading(false)
    }
    async function batchDelete(selectedRowKeys: React.Key[]) {
        if (submit) return
        submit = true
        const params = { scenario_id: selectedRowKeys }
        try {
            const res = await axios.get("/argus/api/scenario/deleteCheck", { params })
            const confirm = res.data.data
            Modal.confirm({
                title: '是否删除？',
                icon: <ExclamationCircleOutlined />,
                content: confirm && confirm.length > 0 && "这些压测场景有压测任务，仍然删除？场景名称：" + confirm.join(" "),
                okType: 'danger',
                async onOk() {
                    try {
                        await axios.delete("/argus/api/scenario", { params })
                        message.success("删除成功")
                        load()
                    } catch (error: any) {
                        message.error(error.message)
                        throw error
                    }
                }
            })
        } catch (e: any) {
            message.error(e.message)
        }
        submit = false
    }
    useEffect(function () {
        load()
    }, [])
    useDidRecover(load)
    return <div className="TestScenario">
        <Breadcrumb label="压测场景" />
        <PageInfo>压测场景</PageInfo>
        <Card>
            <div className="ToolBar">
                <Link to={`${path}/Create`}><Button className="Add" type="primary" icon={<PlusOutlined />}>新增场景</Button></Link>
                <Button icon={<CloseOutlined />} onClick={function () {
                    if (selectedRowKeys.length === 0) {
                        return
                    }
                    batchDelete(selectedRowKeys)
                }}>批量删除</Button>
                <div className="Space"></div>
                <Form layout="inline" onFinish={function (values) {
                    stateRef.current.search = values
                    load()
                }}>
                    <Form.Item name="keywords">
                        <Input className="Input" placeholder="Keywords" />
                    </Form.Item>
                    <Button htmlType="submit" icon={<SearchOutlined />}>查找</Button>
                </Form>
            </div>
            <Table size="middle" loading={loading} dataSource={data.data} rowKey="scenario_id"
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
                        title: "应用名",
                        dataIndex: "app_name",
                        sorter: true,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.app_name)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "场景名称",
                        dataIndex: "scenario_name",
                        ellipsis: true
                    },
                    {
                        title: "场景类型",
                        dataIndex: "scenario_type",
                        sorter: true,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.scenario_type)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "创建人",
                        dataIndex: "create_by",
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.create_by)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "创建时间",
                        dataIndex: "create_time",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "修改时间",
                        dataIndex: "update_time",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "标签",
                        dataIndex: "label",
                        render(value) {
                            return value?.join(",")
                        },
                        ellipsis: true
                    },
                    {
                        title: "描述",
                        dataIndex: "desc",
                        ellipsis: true
                    },
                    {
                        title: "操作",
                        width: 120,
                        render(_, record) {
                            return <>
                                <ScenarioUpdate data={record} load={load} />
                                <Button type="link" size="small" onClick={function () {
                                    batchDelete([record.scenario_id])
                                }}>删除</Button>
                            </>
                        }
                    }
                ]} />
        </Card>
    </div>
}

function ScenarioUpdate(props: { data: Data, load: () => {} }) {
    const [isModalVisible, setIsModalVisible] = useState(false)
    const [form] = Form.useForm()
    useEffect(function () {
        if (!isModalVisible) return
        form.setFieldsValue(props.data)
    }, [form, isModalVisible, props.data])
    return <>
        <Button type="link" size="small" onClick={function () {
            setIsModalVisible(true)
        }}>编辑</Button>
        <Modal className="ScenarioUpdate" title="编辑" width={700} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form form={form} labelCol={{ span: 4 }}
                onFinish={async function (values) {
                    try {
                        const data = { ...values, scenario_id: props.data.scenario_id }
                        await axios.put('/argus/api/scenario', data)
                        setIsModalVisible(false)
                        message.success("保存成功")
                        props.load()
                    } catch (e: any) {
                        message.error(e.message)
                    }
                }}>
                <ScenarioFormItems />
                <Form.Item className="Buttons">
                    <Button type="primary" htmlType="submit">更新</Button>
                    <Button onClick={function () {
                        setIsModalVisible(false)
                    }}>取消</Button>
                </Form.Item>
            </Form>
        </Modal>
    </>
}