import { Button, Form, Input, message, Table } from "antd";
import React, { useEffect, useRef, useState } from "react"
import CacheRoute, { CacheSwitch, useDidRecover } from "react-router-cache-route";
import { Link, Route, useRouteMatch } from "react-router-dom";
import Breadcrumb from "../../component/Breadcrumb";
import Card from "../../component/Card";
import Detail from "./Detail"
import { SearchOutlined } from '@ant-design/icons'
import "./index.scss"
import axios from "axios";

export default function App() {
    const { path } = useRouteMatch();
    return <CacheSwitch>
        <CacheRoute exact path={path} component={Home} />
        <Route exact path={path + '/Detail'}><Detail /></Route>
    </CacheSwitch>
}

type Data = {
    history_id: string,
    plan_name: string,
    creator: string
}

function Home() {
    let { path } = useRouteMatch();
    const [data, setData] = useState<{ data: Data[], total: number }>({ data: [], total: 0 })
    const [loading, setLoading] = useState(false)
    const stateRef = useRef<any>({})
    async function load() {
        setLoading(true)
        const params = {
            pageSize: stateRef.current.pagination?.pageSize || 10,
            current: stateRef.current.pagination?.current,
            sorter: stateRef.current.sorter?.field,
            order: stateRef.current.sorter?.order,
            ...stateRef.current.search,
            ...stateRef.current.filters
        }
        try {
            const res = await axios.get('/argus-emergency/api/history', { params })
            setData(res.data)
        } catch (error: any) {
            message.error(error.message)
        }
        setLoading(false)
    }
    useEffect(function () {
        load()
    }, [])
    useDidRecover(load)
    return <div className="RunningLog">
        <Breadcrumb label="执行记录" />
        <Card>
            <div className="ToolBar">
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
            <Table size="middle" loading={loading} dataSource={data.data} rowKey="history_id"
                onChange={function (pagination, filters, sorter) {
                    stateRef.current = { ...stateRef.current, pagination, filters, sorter }
                    load()
                }}
                pagination={{ total: data.total, size: "small", showTotal() { return `共 ${data.total} 条` }, showSizeChanger: true }}
                columns={[
                    {
                        title: "活动名称",
                        dataIndex: "plan_name",
                        sorter: true,
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.plan_name)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        render(value, record) {
                            return <Link to={path + "/Detail?history_id=" + record.history_id}>{value}</Link>
                        },
                        ellipsis: true
                    },
                    {
                        title: "执行用户",
                        dataIndex: "creator",
                        filters: function () {
                            const set = new Set<string>()
                            data.data.forEach(function (item) {
                                set.add(item.creator)
                            })
                            return Array.from(set).map(function (item) {
                                return { text: item, value: item }
                            })
                        }(),
                        ellipsis: true
                    },
                    {
                        title: "执行状态",
                        width: 200,
                        dataIndex: "status", 
                        ellipsis: true
                    },
                    {
                        title: "执行时间",
                        width: 200,
                        dataIndex: "execute_time",
                        ellipsis: true
                    }
                ]}
            />
        </Card>
    </div>
}