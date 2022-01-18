import { Line, LineOptions, Liquid, LiquidOptions } from "@antv/g2plot";
import { Button, Descriptions, Form, Input, message, Select, Table, Tabs, Tag } from "antd";
import { useForm } from "antd/lib/form/Form";
import { PresetColorTypes } from "antd/lib/_util/colors";
import axios from "axios";
import React, { useEffect, useRef, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import Breadcrumb from "../../../component/Breadcrumb";
import Card from "../../../component/Card";
import "./index.scss"

export default function App() {
    let submit = false
    const [data, setData] = useState<any>({})
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const test_id = urlSearchParams.get("test_id")
    const inputRef = useRef(null)

    useEffect(function () {
        (async function () {
            const res = await axios.get("/argus/api/task/view", { params: { test_id } })
            setData(res.data.data)
            const input: any = inputRef.current
            input.resizableTextArea.textArea.value = res.data.data.test_comment
        })()
        async function load() {
            const res = await axios.get("/argus/api/task/view", { params: { test_id } })
            setData(res.data.data)
        }
        const interval = setInterval(load, 5000)
        return function(){
            clearInterval(interval)
        }
    }, [test_id])
    return <div className="TaskView">
        <Breadcrumb label="压测任务" sub={{ label: "实时TPS数据", parentUrl: "/PerformanceTest/TestTask" }} />
        <Card>
            <div className="Label">基本信息</div>
            <div className="SubCard Info">
                <Descriptions>
                    <Descriptions.Item label={
                        <div className="Title">测试名称</div>
                    }>{data.test_name}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">压测状态</div>
                    }>{data.status_label}</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">标签</div>
                    }>{data.label?.map(function (item: string, index: number) {
                        return <Tag key={index} color={PresetColorTypes[index + 5 % 13]}>{item}</Tag>
                    })}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">描述</div>
                    }>{data.desc}</Descriptions.Item>
                </Descriptions>
                <Link to={"/PerformanceTest/TestReport/Detail?test_id=" + test_id}>
                    <Button type="primary">详细报告</Button>
                </Link>
            </div>
            <div className="SubCard Basic">
                <div className="Item">
                    <div className="Value">{data.duration}</div>
                    <div className="Title">运行时间</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.vuser}</div>
                    <div className="Title">虚拟用户数</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.tps}</div>
                    <div className="Title">TPS</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.tps_peak}</div>
                    <div className="Title">TPS峰值</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.avg_time}</div>
                    <div className="Title">平均时间（ms）</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.test_count}</div>
                    <div className="Title">执行测试数量</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.success_count}</div>
                    <div className="Title">测试成功数量</div>
                </div>
                <div className="Item">
                    <div className="Value">{data.fail_count}</div>
                    <div className="Title">错误</div>
                </div>
            </div>
            <div className="Label">TPS图表</div>
            <Tabs type="card" size="small">
                <Tabs.TabPane tab="业务性能指标" key="BusinessCharts">
                    <BusinessCharts />
                </Tabs.TabPane>
                <Tabs.TabPane tab="硬件资源指标" key="ResourceCharts">
                    <ResourceCharts />
                </Tabs.TabPane>
                <Tabs.TabPane tab="JVM性能指标" key="JvmCharts">
                    <JvmCharts />
                </Tabs.TabPane>
            </Tabs>
            <div className="Label">测试注释</div>
            <div className="Comment">
                <Input.TextArea ref={inputRef} className="Input" showCount maxLength={256} autoSize={{ minRows: 2, maxRows: 2 }} placeholder="请输入描述" />
                <div className="Button" onClick={async function () {
                    if (submit) return
                    submit = true
                    try {
                        const input: any = inputRef.current
                        const test_comment = input.resizableTextArea.textArea.value
                        await axios.put('/argus/api/task/update', { test_id, test_comment })
                        message.success("更新成功")
                    } catch (e: any) {
                        message.error(e.message)
                    }
                    submit = false
                }}>添加注释</div>
            </div>
            <div className="Label">日志文件</div>
            {data.log_name?.map(function (item: string, index: number) {
                return <div key={index} >
                    <a href={process.env.PUBLIC_URL + `/api/task/download?test_id=${test_id}&log_name=${item}`} target="_blank" rel="noreferrer">{item}</a>
                </div>
            })}
            <div className="Label">执行日志</div>
            {data.progress_message?.map(function (item: string, index: number) {
                return <div key={index}>{item}</div>
            })}
        </Card>
    </div>
}

function BusinessCharts() {
    const [data, setData] = useState()
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const test_id = urlSearchParams.get("test_id")
    useEffect(function() {
        async function load() {
            const res = await axios.get('/argus/api/task/service', {params: {test_id}})
            setData(res.data.data)
        }
        load()
        setInterval(load, 5000)
    }, [test_id])
    return <Table dataSource={data} size="small" rowKey="transaction" pagination={false} columns={[
        { title: "事务名称", dataIndex: "transaction" },
        { title: "TPS", dataIndex: "tps" },
        { title: "响应时间(ms)", dataIndex: "response_ms" },
        { title: "成功数", dataIndex: "success_count" },
        { title: "失败数", dataIndex: "fail_count" },
        { title: "失败率%", dataIndex: "fail_rate" }
    ]} />
}

function ResourceCharts() {
    const [ips, setIps] = useState<[{ value: string }]>()
    const cpuUsageRef = useRef(null)
    const memoryUsageRef = useRef(null)
    const ioBusyRef = useRef(null)
    const cpuRef = useRef<HTMLDivElement>(null)
    const memoryRef = useRef(null)
    const diskRef = useRef(null)
    const networkRef = useRef(null)
    const [form] = useForm()
    useEffect(function () {
        form.setFieldsValue({ ip: "192.168.0.1" })
        setIps([{ value: "192.168.0.1" }])
        const liquidOption: LiquidOptions = {
            percent: 0.7,
            outline: {
                border: 2,
                distance: 4,
            },
            wave: {
                length: 64,
            },
            statistic: {
                content: {
                    style(data) {
                        const percent = (data as { percent: number }).percent
                        return {
                            fontSize: "20px",
                            fill: percent > 0.65 ? 'white' : 'rgba(44,53,66,0.85)',
                        }
                    },
                    formatter(data) {
                        const percent = (data as { percent: number }).percent
                        return `${(percent * 100).toFixed(0)}%`
                    }
                }
            },
            liquidStyle({ percent }) {
                let color = '#5B8FF9'
                if (percent > 0.6) {
                    color = '#FAAD14'
                }
                if (percent > 0.8) {
                    color = '#ff4d4f'
                }
                return {
                    fill: color,
                    stroke: color,
                };
            },
            autoFit: false
        }
        const cpuUsageChart = new Liquid(cpuUsageRef.current!!, liquidOption)
        const memoryUsageChart = new Liquid(memoryUsageRef.current!!, liquidOption)
        const ioBusyChart = new Liquid(ioBusyRef.current!!, liquidOption)
        cpuUsageChart.render()
        memoryUsageChart.render()
        ioBusyChart.render()
        const data = [
            { name: "user", time: "00:00", value: 70 }, { name: "user", time: "00:01", value: 80 }, { name: "user", time: "00:02", value: 60 },
            { name: "system", time: "00:00", value: 60 }, { name: "system", time: "00:01", value: 50 }, { name: "system", time: "00:02", value: 90 },
        ]
        const lineOption: LineOptions = {
            data,
            xField: "time",
            yField: "value",
            seriesField: "name",
            xAxis: { tickInterval: 1, range: [0, 1] },
            smooth: true,
            area: {
                style: {
                    fillOpacity: 0.15,
                },
            },
            animation: false,
        }
        const cpuChart = new Line(cpuRef.current!!, {...lineOption, yAxis: {
            label: {
                formatter(text: any) {
                    return text + "%"
                }
            }
        }})
        const memoryChart = new Line(memoryRef.current!!, lineOption)
        cpuChart.render()
        memoryChart.render()
        return function () {
            cpuUsageChart.destroy()
            memoryUsageChart.destroy()
            ioBusyChart.destroy()
            cpuChart.destroy()
            memoryChart.destroy()
        }
    }, [form])
    return <div className="ResourceCharts">
        <Form form={form} layout="inline" className="Form">
            <Form.Item name="ip">
                <Select className="Input" showSearch options={ips} />
            </Form.Item>
        </Form>
        <div className="Grid">
            <div className="Item Middle">
                <div className="Value">4</div>
                <div className="Title">CPU核心数</div>
            </div>
            <div className="Item Middle">
                <div className="Value">8GiB</div>
                <div className="Title">内存大小</div>
            </div>
            <div className="Item Middle">
                <div className="Value">1小时</div>
                <div className="Title">启动时间</div>
            </div>
        </div>
        <div className="Grid">
            <div className="Item">
                <div ref={cpuUsageRef} className="Liquid"></div>
                <div className="Title">CPU利用率</div>
            </div>
            <div className="Item">
                <div ref={memoryUsageRef} className="Liquid"></div>
                <div className="Title">内存利用率</div>
            </div>
            <div className="Item">
                <div ref={ioBusyRef} className="Liquid"></div>
                <div className="Title">IO繁忙率</div>
            </div>
        </div>
        <div className="Grid">
            <div className="Item">
                <div ref={cpuRef} className="Line" style={{width: "100%"}}></div>
                <div className="Title">CPU</div>
            </div>
            <div className="Item">
                <div ref={memoryRef} className="Line"></div>
                <div className="Title">内存</div>
            </div>
        </div>
        <div className="Grid">
            <div className="Item">
                <div ref={diskRef} className="Line"></div>
                <div className="Title">磁盘IO</div>
            </div>
            <div className="Item">
                <div ref={networkRef} className="Line"></div>
                <div className="Title">网络IO</div>
            </div>
        </div>
    </div>
}

function JvmCharts() {
    return <div>表格</div>
}