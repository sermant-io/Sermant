import { Button, message, Modal, Table } from "antd"
import React, { useEffect, useRef, useState } from "react"
import Breadcrumb from "../../component/Breadcrumb"
import Card from "../../component/Card"
import PageInfo from "../../component/PageInfo"
import { CloseOutlined, SyncOutlined, PauseOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
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

type Data = { domain: string, agent_id: string, status_label: string, status: string }

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
            const res = await axios.get("/argus/api/agent", { params })
            setData(res.data)
        } catch (e: any) {

        } finally {
            setLoading(false)
        }
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
                    await axios.delete("/argus/api/agent", { params: { agent_id: selectedRowKeys.join(",") } })
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
            await axios.post("/argus/api/agent/stop", { agent_id: selectedRowKeys })
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
    return <div className="ScriptManage">
        <Breadcrumb label="压测引擎" />
        <PageInfo>如需下载代理，请在右上角菜单栏点击选择<Button type="link" size="small"> “下载代理” </Button>。</PageInfo>
        <Card>
            <div className="ToolBar">
                <Button className="Add" type="primary" icon={<SyncOutlined />} onClick={load}>更新代理</Button>
                <Button icon={<CloseOutlined />} onClick={function () {
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
                <span className="Download">下载代理</span>
                <AgentDownload />
            </div>
            <Table size="middle" dataSource={data.data} loading={loading} rowKey="agent_id"
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
                        width: 80,
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "IP / 域",
                        dataIndex: "domain",
                        render(_, record) {
                            return <Link to={`${path}/Detail?agent_id=${record.agent_id}`}>{record.domain}</Link>
                        },
                        sorter: true,
                        width: 200,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.domain)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "端口",
                        dataIndex: "port",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "名称",
                        dataIndex: "agent_name",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "版本",
                        dataIndex: "version",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "区域",
                        dataIndex: "region",
                        sorter: true,
                        ellipsis: true
                    },
                    {
                        title: "已许可",
                        dataIndex: "licensed",
                        width: 200,
                        render(data, record) {
                            return <span className={`Licensed${data ? " active" : " deactive"}`} onClick={async function () {
                                try {
                                    await axios.post('/argus/api/agent/license', { agent_id: record.agent_id, licensed: !data })
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
                ]} />
        </Card>
    </div>
}

function AgentDownload() {
    const [link, setLink] = useState()
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get('/argus/api/agent/link')
                setLink(res.data.data.link)
            } catch (error: any) {

            }
        })()
    }, [])
    return <a className="AgentDownload" href={'/argus/api/agent/download/' + link} target="_blank" rel="noreferrer">{link}</a>
}