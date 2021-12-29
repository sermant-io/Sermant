import { Button, Table } from "antd"
import axios from "axios"
import React, { useEffect, useRef, useState } from "react"
import Card from "../../component/Card"
import PageInfo from "../../component/PageInfo"
import { DynamicCard, ScriptCard, TCPCopyCard } from "../TestScenario/Scenario"
import Left from "./Left.png"
import Right from "./Right.png"
import Calendar from "./Calendar.jpg"
import './index.scss'

export default function App() {
    return <div className="Home">
        <div className="HomeLeft">
            <PageInfo>
                <span>Argus性能测试可提供快速压测、Jmeter压测、引流录制，极易上手！</span>
                <Button type="link" size="small">点击查看快速压测文档。</Button>
            </PageInfo>
            <Card className="Guide" height="130px">
                <DynamicCard />
                <TCPCopyCard />
                <ScriptCard />
            </Card>
            <Card className="Chart" height="310px">
                <div className="Left">
                    <h3>ARGUS</h3>
                    <img src={Left} alt="" />
                </div>
                <div className="Right">
                    <h3>ARGUS</h3>
                    <img src={Right} alt="" />
                </div>
            </Card>
            <Card className="ScenarioList" height="440px">
                <h3>场景列表</h3>
                <ScenarioList />
            </Card>
        </div>
        <div className="HomeRight">
            <Card height="380px" className="WithTitle">
                <div className="Title">压测日历</div>
                <div className="Content">
                    <img src={Calendar} alt="" />
                </div>
            </Card>
            <Card height="200px" className="Kanban">
                <h2>276</h2>
                <p>共压测次数</p>
                <span>查看详情</span>
            </Card>
            <Card height="370px" className="WithTitle">
                <div className="Title">技术支持</div>
                <div className="Content">
                    <Support />
                </div>
            </Card>
        </div>
    </div>
}

function Support() {
    const [data, setData] = useState<{ title: string, url: string }[]>([])
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get("/argus/api/support")
                setData(res.data.data)
            } catch (error: any) {
                
            }
        })()
    }, [])
    return <div className="Support">{data.map(function (item, index) {
        return <div key={index} className="Item">
            <a href={item.url} target="_blank" rel="noreferrer">{item.title}</a>
            {index < 3 && <span className="icon md">fiber_new</span>}
        </div>
    })}</div>
}

type Data = {
    create_by: string,
    app_name: string,
    scenario_type: string
}
const pageSize = 5
function ScenarioList() {
    const [data, setData] = useState<{ data: Data[], total: number }>({ data: [], total: 0 })
    const [loading, setLoading] = useState(false)
    const stateRef = useRef<any>({})
    async function load() {
        setLoading(true)
        try {
            const params = {
                pageSize: stateRef.current.pagination?.pageSize || pageSize,
                current: stateRef.current.pagination?.current,
                sorter: stateRef.current.sorter?.field,
                order: stateRef.current.sorter?.order,
                ...stateRef.current.filters
            }
            const res = await axios.get("/argus/api/scenario", { params })
            setData(res.data)
        } catch (error: any) {
            
        }
        setLoading(false)
    }
    useEffect(function () {
        load()
    }, [])
    return <Table size="middle" loading={loading} dataSource={data.data} rowKey="scenario_id"
        onChange={function (pagination, filters, sorter) {
            stateRef.current = { pagination, filters, sorter }
            load()
        }}
        pagination={{ total: data.total, pageSize, showSizeChanger: false, size: "small", hideOnSinglePage: true, showTotal() { return `共 ${data.total} 条` } }}
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
        ]} />
}