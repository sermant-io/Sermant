import { Button } from "antd"
import axios from "axios"
import React, { useEffect, useRef, useState } from "react"
import { useLocation } from "react-router-dom"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import PageInfo from "../../../component/PageInfo"
import { Line, LineOptions } from "@antv/g2plot"
import "./index.scss"
import moment from "moment"

export default function App() {
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const agent_id = urlSearchParams.get("agent_id") || ""
    const [data, setDate] = useState<any>({})
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get("/argus/api/agent/get", { params: { agent_id } })
                setDate(res.data.data)
            } catch (error: any) {
                
            }
        })()
    }, [agent_id])
    return <div className="AgentDetail">
        <Breadcrumb label="压测引擎" sub={{ label: "引擎信息", parentUrl: "/PerformanceTest/TestAgent" }} />
        <PageInfo>如需下载代理，请在右上角菜单栏点击选择<Button type="link" size="small"> “下载代理” </Button>。</PageInfo>
        <Card>
            <div className="Label">基本信息</div>
            <div className="SubCard Basic">
                <div className="Item">
                    <div className="Value">{data.domain}</div>
                    <div className="Title">IP</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.port}</div>
                    <div className="Title">端口</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.agent_name}</div>
                    <div className="Title">名称</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.region}</div>
                    <div className="Title">区域</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.version}</div>
                    <div className="Title">版本</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.status_label}</div>
                    <div className="Title">状态</div>
                </div>
            </div>
            {data.status === "running" && <AgentCharts agent_id={agent_id}/>}
        </Card>
    </div>
}

function AgentCharts({agent_id}: {agent_id: string}) {
    const usageRef = useRef(null)
    const memoryRef = useRef(null)
    useEffect(function () {
        const config: LineOptions = {
            animation: false,
            data: [],
            xField: 'time',
            xAxis: { tickInterval: 5, range: [0, 1] },
            smooth: true,
        }
        const usageData = Array.from({ length: 91 }, function (_, index) {
            return {
                time: moment(new Date(index * 1000)).format("mm:ss"),
                usage: null
            }
        })
        const usageChart = new Line(usageRef.current!!, {
            ...config, yField: 'usage',
            data: usageData, color: '#ff699f', yAxis: {
                label: {
                    formatter(text: any) {
                        return text + "%"
                    }
                }
            },
        })
        usageChart.render()
        const memoryData = Array.from({ length: 91 }, function (_, index) {
            return {
                time: moment(new Date(index * 1000)).format("mm:ss"),
                memory: null
            }
        })
        const memoryChart = new Line(memoryRef.current!!, {
            ...config, yField: 'memory',
            data: memoryData, color: "#15c4ff", yAxis: {
                label: {
                    formatter(text: any) {
                        return text + "M"
                    }
                }
            },
        })
        memoryChart.render()
        let second = 0
        const timeInterval = setInterval(async function () {
            second++
            try {
                const res = await axios.get('/argus/api/agent/chart', {params: {agent_id}})
                const data = {
                    time: moment(new Date(second * 1000)).format("mm:ss"),
                    ...res.data.data
                }
                if (second > 90) {
                    usageData.shift()
                    usageData.push(data)
                    memoryData.shift()
                    memoryData.push(data)
                } else {
                    usageData[second] = data
                    memoryData[second] = data
                }
                usageChart.changeData(usageData)
                memoryChart.changeData(memoryData)
            } catch (error: any) {
                clearInterval(timeInterval)
            }
        }, 1000)
        return function () {
            clearInterval(timeInterval)
            usageChart.destroy()
            memoryChart.destroy()
        }
    }, [agent_id])
    return <div className="AgentCharts">
        <div className="Label">CPU使用率</div>
        <div ref={usageRef} className="SubCard"></div>
        <div className="Label">内存使用率</div>
        <div ref={memoryRef} className="SubCard"></div>
    </div>
}
