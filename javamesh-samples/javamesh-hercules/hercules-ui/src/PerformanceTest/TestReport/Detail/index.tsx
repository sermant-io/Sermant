import { Line } from '@ant-design/charts'
import { Descriptions, Tag, Tooltip } from "antd"
import { PresetColorTypes } from "antd/lib/_util/colors"
import axios from "axios"
import React, { useEffect, useState } from "react"
import { useLocation } from "react-router-dom"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import "./index.scss"

export default function App() {
    const [data, setData] = useState<any>({})
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const test_id = urlSearchParams.get("test_id")
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get("/argus/api/report/get", { params: { test_id } })
                setData(res.data.data)
            } catch (error: any) {
                
            }
        })()
    }, [test_id])
    return <div className="ReportDetail">
        <Breadcrumb label="压测报告" sub={{ label: "详细报告", parentUrl: "/PerformanceTest/TestReport" }} />
        <Card>
            <div className="Label">基本信息</div>
            <div className="SubCard Info">
                <Descriptions>
                    <Descriptions.Item span={3} label={
                        <div className="Title">测试名称</div>
                    }>{data.test_name}</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">标签</div>
                    }>{data.label?.map(function (item: string, index: number) {
                        return <Tag key={index} color={PresetColorTypes[index + 5 % 13]}>{item}</Tag>
                    })}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">描述</div>
                    }>{data.desc}</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">压测引擎</div>
                    }>{data.agent}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">忽略采样数量</div>
                    }>{data.sampling_ignore}</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">插件</div>
                    }>{data.plugin}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">目标服务器</div>
                    }>{data.target_host}</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">开始时间</div>
                    }>{data.start_time}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">测试时间</div>
                    }>{data.test_time}</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">结束时间</div>
                    }>{data.end_time}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">运行时间</div>
                    }>{data.run_time}</Descriptions.Item>
                </Descriptions>
                {/* <Button className="Download" type="primary" icon={<DownloadOutlined />}>下载报告</Button> */}
            </div>
            <div className="SubCard Basic">
                <div className="Item">
                    <div className="Value">{data.process}/{data.thread}</div>
                    <div className="Title">进程数/线程数</div>
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
                    <div className="Value">{data.tps_max}</div>
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
            <ReportCharts />
            {/* <Tabs defaultActiveKey="1" type="card">
                <Tabs.TabPane tab="压测信息" key="1">
                    <ReportCharts />
                </Tabs.TabPane>
                <Tabs.TabPane tab="监控信息" key="2">
                    <Monitor />
                </Tabs.TabPane>
            </Tabs> */}
        </Card>
    </div>
}

function ReportCharts() {
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const test_id = urlSearchParams.get("test_id")
    const [data, setData] = useState([])
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get("/argus/api/report/chart", { params: { test_id } })
                setData(res.data.data)
            } catch (error: any) {
                
            }
        })()
    }, [test_id])
    return <div className="ReportCharts">
        <div className="Label">TPS图表<Tooltip title="TPS图表">
            <span className="Question icon fa fa-question-circle"></span>
        </Tooltip></div>
        <div className="SubCard">
            <Line data={data} xField="time" yField="tps" color="#15c4ff"
                height={230} xAxis={{ tickCount: 20, range: [0, 1] }} smooth={true}
            />
        </div>
        <div className="Label">平均时间（ms）</div>
        <div className="SubCard">
            <Line data={data} xField="time" yField="avg_time" color="#ff699f"
                height={230} xAxis={{ tickCount: 20, range: [0, 1] }} smooth={true}
            />
        </div>
        <div className="Label">首次接收数据的平均时间（ms）</div>
        <div className="SubCard">
            <Line data={data} xField="time" yField="receive_avg" color="#0eaa76"
                height={230} xAxis={{ tickCount: 20, range: [0, 1] }} smooth={true}
            />
        </div>
        <div className="Label">Vuser</div>
        <div className="SubCard">
            <Line data={data} xField="time" yField="vuser" color="#0eaa76"
                height={230} xAxis={{ tickCount: 20, range: [0, 1] }} smooth={true}
            />
        </div>
        <div className="Label">错误</div>
        <div className="SubCard">
            <Line data={data} xField="time" yField="fail_count" color="#15c4ff"
                height={230} xAxis={{ tickCount: 20, range: [0, 1] }} smooth={true}
            />
        </div>
    </div>
}