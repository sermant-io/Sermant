import { Line, Liquid, LiquidOptions } from "@antv/g2plot";
import { Button, Descriptions, Form, Input, Select, Table, Tabs, Tag } from "antd";
import { useForm } from "antd/lib/form/Form";
import { PresetColorTypes } from "antd/lib/_util/colors";
import axios from "axios";
import React, { useEffect, useRef, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import Breadcrumb from "../../../component/Breadcrumb";
import Card from "../../../component/Card";
import "./index.scss"

export default function App() {
    // const [data, setData] = useState<any>()
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const test_id = urlSearchParams.get("test_id")
    useEffect(function(){
        axios.get("/argus/api/task/view")
    },[])
    return <div className="TaskView">
        <Breadcrumb label="压测任务" sub={{ label: "实时TPS数据", parentUrl: "/PerformanceTest/TestTask" }} />
        <Card>
            <div className="Label">基本信息</div>
            <div className="SubCard Info">
                <Descriptions>
                    <Descriptions.Item label={
                        <div className="Title">测试名称</div>
                    }>测试名称</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">压测状态</div>
                    }>压测状态</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">标签</div>
                    }>{[].map(function (item: string, index: number) {
                        return <Tag key={index} color={PresetColorTypes[index + 5 % 13]}>{item}</Tag>
                    })}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">描述</div>
                    }>描述</Descriptions.Item>
                </Descriptions>
                <Button type="primary">
                    <Link to={"/PerformanceTest/TestReport/Detail?test_id=" + test_id}>详细报告</Link>
                </Button>
            </div>
            <div className="SubCard Basic">
                <div className="Item">
                    <div className="Value">运行时间</div>
                    <div className="Title">运行时间</div>
                </div>
                <div className="Item">
                    <div className="Value">虚拟用户数</div>
                    <div className="Title">虚拟用户数</div>
                </div>
                <div className="Item">
                    <div className="Value">TPS</div>
                    <div className="Title">TPS</div>
                </div>
                <div className="Item">
                    <div className="Value">TPS峰值</div>
                    <div className="Title">TPS峰值</div>
                </div>
                <div className="Item">
                    <div className="Value">平均时间（ms）</div>
                    <div className="Title">平均时间（ms）</div>
                </div>
                <div className="Item">
                    <div className="Value">执行测试数量</div>
                    <div className="Title">执行测试数量</div>
                </div>
                <div className="Item">
                    <div className="Value">测试成功数量</div>
                    <div className="Title">测试成功数量</div>
                </div>
                <div className="Item">
                    <div className="Value">错误</div>
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
                <Input.TextArea className="Input" showCount maxLength={256} autoSize={{ minRows: 2, maxRows: 2 }} placeholder="请输入描述" />
                <div className="Button">添加注释</div>
            </div>
            <div className="Label">日志文件</div>
            {[].map(function (item: string, index: number) {
                return <div key={index} >
                    <a href={process.env.PUBLIC_URL + `/api/task/download?test_id=${123}&log_name=${item}`} target="_blank" rel="noreferrer">{item}</a>
                </div>
            })}
            <div className="Label">执行日志</div>
            {[].map(function (item: string, index: number) {
                return <div key={index}>{item}</div>
            })}
        </Card>
    </div>
}

function BusinessCharts() {
    return <Table columns={[
        { title: "事务名称" },
        { title: "TPS" },
        { title: "响应时间(ms)" },
        { title: "成功数" },
        { title: "失败数" },
        { title: "失败率%" }
    ]} />
}

function ResourceCharts() {
    const [ips, setIps] = useState<[{ value: string }]>()
    const cpuUsageRef = useRef(null)
    const memoryUsageRef = useRef(null)
    const ioBusyRef = useRef(null)
    const cpuRef = useRef(null)
    const memoryRef = useRef(null)
    const diskRef = useRef(null)
    const networkRef = useRef(null)
    const [form] = useForm()
    useEffect(function () {
        form.setFieldsValue({ ip: "192.168.0.1" })
        setIps([{ value: "192.168.0.1" }])
        const option: LiquidOptions = {
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
            }
        }
        const cpuUsageChart = new Liquid(cpuUsageRef.current!!, option)
        const memoryUsageChart = new Liquid(memoryUsageRef.current!!, option)
        const ioBusyChart = new Liquid(ioBusyRef.current!!, option)
        cpuUsageChart.render()
        memoryUsageChart.render()
        ioBusyChart.render()
        const data = [
            { name: "user", time: "00:00", value: 70 }, { name: "user", time: "00:01", value: 80 }, { name: "user", time: "00:02", value: 60 },
            { name: "system", time: "00:00", value: 60 }, { name: "system", time: "00:01", value: 50 }, { name: "system", time: "00:02", value: 90 },
        ]
        const cpuChart = new Line(cpuRef.current!!, {
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
            yAxis: {
                label: {
                    formatter(text: any) {
                        return text + "%"
                    }
                }
            }
        })
        cpuChart.render()
        return function () {
            cpuUsageChart.destroy()
            memoryUsageChart.destroy()
            ioBusyChart.destroy()
            cpuChart.destroy()
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
                <div ref={cpuRef} className="Line"></div>
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