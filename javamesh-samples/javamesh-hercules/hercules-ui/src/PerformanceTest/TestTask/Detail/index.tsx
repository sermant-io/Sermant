import { Button, Descriptions, Input, message, Tag } from "antd"
import React, { useEffect, useRef, useState } from "react"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import "./index.scss"
import { PresetColorTypes } from "antd/lib/_util/colors"
import { Link, useLocation } from "react-router-dom"
import axios from "axios"
import { Line } from "@antv/g2plot"

export default function App() {
    let submit = false
    const [data, setData] = useState<any>()
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const test_id = urlSearchParams.get("test_id")
    const inputRef = useRef(null)
    const chartRef = useRef(null)
    useEffect(function () {
        const chart = new Line(chartRef.current!!, {
            animation: false,
            data: [],
            xField: 'time',
            yField: 'tps',
            xAxis: { tickInterval: 5, range: [0, 1] },
            smooth: true,
            color: "#15c4ff",
            padding: 40,
            height: 230
        })
        let timeInterval: any
        let chartData: any[] = [];
        timeInterval = setInterval(async function () {
            try {
                const res = await axios.get('/argus/api/task/get', { params: { test_id } })
                const resData = res.data.data
                setData(resData)
                chartData.shift()
                chartData.push(resData.chart[0])
                chart.changeData(chartData)
                if (resData.status !== "running") {
                    clearInterval(timeInterval)
                }
            } catch (e: any) {
                clearInterval(timeInterval)
            }
        }, 1000);
        (async function () {
            try {
                const res = await axios.get('/argus/api/task/get', { params: { test_id, start: -90, interval: 1 } })
                const resData = res.data.data
                setData(resData)
                chartData = resData.chart
                chart.changeData(chartData)
                chart.render()
            } catch (e: any) {
                clearInterval(timeInterval)
            }
        })()
        return function () {
            clearInterval(timeInterval)
            chart.destroy()
        }
    }, [test_id])
    return <div className="TaskDetail">
        <Breadcrumb label="压测任务" sub={{ label: "实时TPS数据", parentUrl: "/PerformanceTest/TestTask" }} />
        <Card>
            <div className="Label">基本信息</div>
            <div className="SubCard Info">
                <Descriptions>
                    <Descriptions.Item label={
                        <div className="Title">测试名称</div>
                    }>{data?.test_name}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">压测状态</div>
                    }>{data?.status_label}</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">标签</div>
                    }>{data?.label?.map(function (item: string, index: number) {
                        return <Tag key={index} color={PresetColorTypes[index + 5 % 13]}>{item}</Tag>
                    })}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">描述</div>
                    }>{data?.desc}</Descriptions.Item>
                </Descriptions>
                <Button type="primary">
                    <Link to={"/PerformanceTest/TestReport/Detail?test_id=" + test_id}>详细报告</Link>
                </Button>
            </div>
            <div className="SubCard Basic">
                <div className="Item">
                    <div className="Value">{data?.duration}</div>
                    <div className="Title">运行时间</div>
                </div>
                <div className="Item">
                    <div className="Value">{data?.vuser}</div>
                    <div className="Title">虚拟用户数</div>
                </div>
                <div className="Item">
                    <div className="Value">{data?.tps}</div>
                    <div className="Title">TPS</div>
                </div>
                <div className="Item">
                    <div className="Value">{data?.tps_peak}</div>
                    <div className="Title">TPS峰值</div>
                </div>
                <div className="Item">
                    <div className="Value">{data?.avg_time}</div>
                    <div className="Title">平均时间（ms）</div>
                </div>
                <div className="Item">
                    <div className="Value">{data?.test_count}</div>
                    <div className="Title">执行测试数量</div>
                </div>
                <div className="Item">
                    <div className="Value">{data?.success_count}</div>
                    <div className="Title">测试成功数量</div>
                </div>
                <div className="Item">
                    <div className="Value">{data?.fail_count}</div>
                    <div className="Title">错误</div>
                </div>
            </div>
            <div className="Label">TPS图表</div>
            <div className="SubCard">
                <div className="TaskChart" ref={chartRef}></div>
            </div>
            <div className="Label">测试注释</div>
            {data && <div className="Comment">
                <Input.TextArea ref={inputRef} className="Input" defaultValue={data.test_comment} showCount maxLength={256} autoSize={{ minRows: 2, maxRows: 2 }} placeholder="请输入描述" />
                <div className="Button" onClick={async function () {
                    if (submit) return
                    submit = true
                    try {
                        const input: any = inputRef.current
                        const test_comment = input.resizableTextArea.props.value
                        await axios.put('/argus/api/task/update', { test_id, test_comment })
                        message.success("更新成功")
                    } catch (e: any) {
                        message.error(e.message)
                    }
                    submit = false
                }}>添加注释</div>
            </div>}
            <div className="Label">日志文件</div>
            {data?.log_name?.map(function (item: string, index: number) {
                return <div key={index} >
                    <a href={process.env.PUBLIC_URL + `/api/task/download?test_id=${test_id}&log_name=${item}`} target="_blank" rel="noreferrer">{item}</a>
                </div>
            })}
            <div className="Label">执行日志</div>
            {data?.progress_message?.map(function (item: string, index: number) {
                return <div key={index}>{item}</div>
            })}
        </Card>
    </div>
}