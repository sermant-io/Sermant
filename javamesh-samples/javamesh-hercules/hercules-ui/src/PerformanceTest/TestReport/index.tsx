import { Button, Input, Table, DatePicker, Select, Form, message, Modal } from "antd"
import React, { useEffect, useRef, useState } from "react"
import Breadcrumb from "../../component/Breadcrumb"
import Card from "../../component/Card"
import PageInfo from "../../component/PageInfo"
import { CloseOutlined, SearchOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import "./index.scss"
import axios from "axios"
import { Link, Route, useRouteMatch } from "react-router-dom"
import CacheRoute, { CacheSwitch } from "react-router-cache-route"
import Detail from "./Detail"

export default function App() {
    const { path } = useRouteMatch();
    return <CacheSwitch>
        <CacheRoute exact path={path} component={Home} />
        <Route exact path={`${path}/Detail`}><Detail /></Route>
    </CacheSwitch>
}

type Data = { report_id: string, test_name: string, type: string, test_id: string }
function Home() {
    let submit = false
    const { path } = useRouteMatch();
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
            const res = await axios.get("/argus/api/report", { params })
            setData(res.data)
        } catch (e: any) {

        }
        setLoading(false)
    }
    async function batchDelete(selectedRowKeys: React.Key[]) {
        if (submit) return
        submit = true
        Modal.confirm({
            title: '是否删除？',
            icon: <ExclamationCircleOutlined />,
            content: '删除后无法恢复，请谨慎操作',
            okType: 'danger',
            async onOk() {
                try {
                    await axios.delete("/argus/api/report", { params: { test_id: selectedRowKeys } })
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
    useEffect(function () {
        load()
    }, [])
    return <div className="TestReport">
        <Breadcrumb label="压测报告" />
        <PageInfo>如需下载代理，请在右上角菜单栏点击选择<Button type="link"> “下载代理” </Button>。</PageInfo>
        <Card>
            <div className="ToolBar">
                <Button icon={<CloseOutlined />} onClick={function () {
                    if (selectedRowKeys.length === 0) {
                        return
                    }
                    batchDelete(selectedRowKeys)
                }}>批量删除</Button>
                <div className="Space"></div>
                <Form initialValues={{ test_type: "" }} layout="inline" onFinish={function (values) {
                    stateRef.current.search = {}
                    stateRef.current.search.test_type = values.test_type
                    stateRef.current.search.keywords = values.keywords;
                    if (values.test_time && values.test_time.length === 2) {
                        stateRef.current.search.start_time = values.test_time[0].format("YYYY-MM-DD HH:mm:ss")
                        stateRef.current.search.end_time = values.test_time[1].format("YYYY-MM-DD HH:mm:ss")
                    }
                    load()
                }}>
                    <Form.Item label="压测类型" name="test_type">
                        <Select className="Select">
                            <Select.Option value="">全部</Select.Option>
                            <Select.Option value="动态编排">动态编排</Select.Option>
                            <Select.Option value="引流压测">引流压测</Select.Option>
                            <Select.Option value="自定义脚本">自定义脚本</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="压测时间" name="test_time">
                        <DatePicker.RangePicker showTime />
                    </Form.Item>
                    <Form.Item name="keywords">
                        <Input className="Input" placeholder="Keywords" />
                    </Form.Item>
                    <Button htmlType="submit" icon={<SearchOutlined />}>查找</Button>
                </Form>
            </div>
            <Table size="middle" dataSource={data.data} loading={loading} rowKey="report_id"
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
                        title: "报告ID",
                        dataIndex: "report_id",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "测试名称",
                        dataIndex: "test_name",
                        sorter: true,
                        width: 200,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.test_name)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "压测类型",
                        dataIndex: "test_type",
                        ellipsis: true
                    },
                    {
                        title: "所有者",
                        dataIndex: "owner",
                        ellipsis: true
                    },
                    {
                        title: "开始时间",
                        dataIndex: "start_time",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "结束时间",
                        dataIndex: "end_time",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "测试时间",
                        dataIndex: "duration",
                        ellipsis: true
                    },
                    {
                        title: "操作",
                        render(_, record) {
                            return <>
                                <Link to={`${path}/Detail?test_id=${record.test_id}`}>查看</Link>
                                <Button type="link" size="small" onClick={function () {
                                    batchDelete([record.report_id])
                                }}>删除</Button>
                            </>
                        }
                    },
                ]} />
        </Card>
    </div>
}