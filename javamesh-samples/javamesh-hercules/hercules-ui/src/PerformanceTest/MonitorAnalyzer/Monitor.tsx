import React, { useEffect, useRef, useState } from "react"
import { DatePicker, Form, Radio, Table } from "antd"
import moment from 'moment'
import "./Monitor.scss"
import { Line, LineOptions } from "@antv/g2plot"
import axios from "axios"

let timeInterval: any
export default function App() {
    const [value, setValue] = useState("20分钟")
    const [changeData, setChangeData] = useState<{ func: (data: any) => void }>()
    const cpuRef = useRef(null)
    const memoryRef = useRef(null)
    const diskRef = useRef(null)
    const networkRef = useRef(null)
    const gcRef = useRef(null)
    const threadRef = useRef(null)
    const classRef = useRef(null)
    const jvmMemoryRef = useRef(null)
    const jvmCpuRef = useRef(null)
    useEffect(function () {
        const config: LineOptions = {
            smooth: true,
            data: [],
            xField: 'time',
            yField: 'value',
            seriesField: 'category',
            xAxis: { range: [0, 1], tickInterval: 6 },
            legend: {
                position: "bottom"
            }
        }
        const cupLine = new Line(cpuRef.current!!, { ...config, color: ['#05AEFC', '#0DC69A', '#9E5BF8'] })
        cupLine.render()
        const memoryLine = new Line(memoryRef.current!!, { ...config, color: ['#9E5BF8'] })
        memoryLine.render()
        const diskLine = new Line(diskRef.current!!, { ...config, color: ['#9E5BF8', '#0DC69A'] })
        diskLine.render()
        const networkLine = new Line(networkRef.current!!, { ...config, color: ['#9E5BF8', '#0DC69A'] })
        networkLine.render()
        const gcLine = new Line(gcRef.current!!, { ...config, color: ['#9E5BF8', '#0DC69A', '#05AEFC', '#FF9F40'] })
        gcLine.render()
        const threadLine = new Line(threadRef.current!!, { ...config, color: ['#9E5BF8', '#0DC69A', '#05AEFC', '#FF9F40', '#EF6A6A'] })
        threadLine.render()
        const jvmMemoryLine = new Line(jvmMemoryRef.current!!, { ...config, color: ['#9E5BF8', '#FF9F40'] })
        jvmMemoryLine.render()
        const classLine = new Line(classRef.current!!, { ...config, color: ['#9E5BF8', '#0DC69A', '#FF9F40'] })
        classLine.render()
        const jvmCpuLine = new Line(jvmCpuRef.current!!, { ...config, color: ['#05AEFC'] })
        jvmCpuLine.render();
        setChangeData({
            func(data?: any) {
                cupLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.cupUseage,
                            "category": "CPU使用率"
                        },
                        {
                            "time": item.time,
                            "value": item.cusSysUseage,
                            "category": "CPU系统使用率"
                        },
                        {
                            "time": item.time,
                            "value": item.cusUserUseage,
                            "category": "CPU用户使用率"
                        },
                    ]
                }).flat())
                memoryLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.memory,
                            "category": "内存"
                        },
                    ]
                }).flat())
                diskLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.diskRead,
                            "category": "每秒都K字节数"
                        },
                        {
                            "time": item.time,
                            "value": item.diskWrite,
                            "category": "每秒写K字节数"
                        },
                    ]
                }).flat())
                networkLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.networkRead,
                            "category": "平均每秒读包数"
                        },
                        {
                            "time": item.time,
                            "value": item.networkWrite,
                            "category": "平均每秒写包数"
                        },
                    ]
                }).flat())
                gcLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.fullGcCount,
                            "category": "fullgc次数"
                        },
                        {
                            "time": item.time,
                            "value": item.fullGcSpend,
                            "category": "fullgc时间"
                        },
                        {
                            "time": item.time,
                            "value": item.ygcCount,
                            "category": "ygc次数"
                        },
                        {
                            "time": item.time,
                            "value": item.ygcSpend,
                            "category": "ygc时间"
                        },
                    ]
                }).flat())
                threadLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.threadCount,
                            "category": "当前线程数"
                        },
                        {
                            "time": item.time,
                            "value": item.threadRunning,
                            "category": "所有启动线程数"
                        },
                        {
                            "time": item.time,
                            "value": item.threadPeak,
                            "category": "峰值线程数"
                        },
                        {
                            "time": item.time,
                            "value": item.threadDead,
                            "category": "死锁线程数"
                        },
                        {
                            "time": item.time,
                            "value": item.threadGuard,
                            "category": "守护线程数"
                        },
                    ]
                }).flat())
                jvmMemoryLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.heapMemory,
                            "category": "堆内存使用"
                        },
                        {
                            "time": item.time,
                            "value": item.nonHeapMemory,
                            "category": "非堆内存使用"
                        },
                    ]
                }).flat())
                classLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.classRunning,
                            "category": "当前类个数"
                        },
                        {
                            "time": item.time,
                            "value": item.classLoading,
                            "category": "总共加载类个数"
                        },
                        {
                            "time": item.time,
                            "value": item.classUnloading,
                            "category": "卸载的类个数"
                        },
                    ]
                }).flat())
                jvmCpuLine.changeData(data.map(function (item: any) {
                    return [
                        {
                            "time": item.time,
                            "value": item.jvmCpuUsage,
                            "category": "使用率"
                        },
                    ]
                }).flat())
            }
        })
        return function () {
            cupLine.destroy()
            memoryLine.destroy()
            diskLine.destroy()
            networkLine.destroy()
            gcLine.destroy()
            threadLine.destroy()
            jvmMemoryLine.destroy()
            classLine.destroy()
            jvmCpuLine.destroy()
        }
    }, [])
    useEffect(function () {
        if (!changeData) return
        clearInterval(timeInterval)
        let data: any[] = []
        timeInterval = setInterval(async function () {
            try {
                const res = await axios.get("/argus/api/monitor")
                data.shift()
                data.push(res.data.data[0])
                changeData?.func(data)
            } catch (error: any) {
                clearInterval(timeInterval)
            }
        }, 50 * 1000);
        (async function () {
            try {
                const res = await axios.get("/argus/api/monitor", { params: { start: -1200, interval: 50 } })
                changeData.func(res.data.data)
                data = res.data.data
            } catch (error: any) {
                clearInterval(timeInterval)
            }
        })()
        return function () {
            clearInterval(timeInterval)
        }
    }, [changeData])
    return <div className="Monitor">
        <div className="TimePicker">
            <Form layout="inline" initialValues={{ radio: "20分钟", picker: [moment(new Date(new Date().getTime() - 20 * 60 * 1000)), moment(new Date())] }}
                onValuesChange={async function (_, values) {
                    const value = values.radio
                    setValue(value)
                    clearInterval(timeInterval)
                    if (value === "自定义") {
                        let [start, end] = values.picker
                        const interval = Math.round(end.diff(start, 'second') / 24)
                        start = start.format("YYYY-MM-DD HH:mm:ss")
                        end = end.format("YYYY-MM-DD HH:mm:ss")
                        try {
                            const res = await axios.get("/argus/api/monitor", { params: { start, end, interval } })
                            changeData?.func(res.data.data)
                        } catch (error: any) {
                            
                        }
                        return
                    }
                    let start = -1200
                    let interval = 50
                    // 类型 时长 坐标间隔 坐标数量 采样精度 数据间隔 
                    // 20分钟 1200 300 4 6 50
                    // 1小时 3600 600 6 6 100
                    // 3小时 10800 200 6 6 300
                    // 6小时 21600 3600 6 6 600
                    // 12小时 43200 7200 6 6 1200
                    // 1天    86400 14400 6 6 2400
                    // 1周   604800 86400 7 6 14400
                    switch (value) {
                        case "1小时":
                            start = -3600
                            interval = 100
                            break
                        case "3小时":
                            start = -10800
                            interval = 300
                            break
                        case "6小时":
                            start = -21600
                            interval = 600
                            break
                        case "12小时":
                            start = -43200
                            interval = 1200
                            break
                        case "1天":
                            start = -86400
                            interval = 2400
                            break
                        case "1周":
                            start = -604800
                            interval = 14400
                            break
                    }
                    try {
                        const res = await axios.get("/argus/api/monitor", { params: { start, interval } })
                        changeData?.func(res.data.data)
                        const data: any[] = res.data.data
                        if (start === -120000) {
                            clearInterval(timeInterval)
                            timeInterval = setInterval(async function () {
                                try {
                                    const res = await axios.get("/argus/api/monitor")
                                    data.shift()
                                    data.push(res.data.data[0])
                                    changeData?.func(data)
                                } catch (error: any) {
                                    clearInterval(timeInterval)
                                    
                                }
                            }, interval * 1000)
                        }
                    } catch (e: any) {
                        
                    }
                }}>
                <Form.Item label="监控时间" name="radio">
                    <Radio.Group
                        options={["20分钟", "1小时", "3小时", "6小时", "12小时", "1天", "1周", "1月", "自定义"]}
                    />
                </Form.Item>
                <Form.Item name="picker">
                    <DatePicker.RangePicker style={function () {
                        if (value !== "自定义") {
                            return {
                                display: "none"
                            }
                        }
                        return {}
                    }()} showTime allowClear={false} />
                </Form.Item>
            </Form>
        </div>
        <div className="Label">机器</div>
        <div className="Compute">
            <div ref={cpuRef} className="Chart"></div>
            <div ref={memoryRef} className="Chart"></div>
            <div ref={diskRef} className="Chart"></div>
            <div ref={networkRef} className="Chart"></div>
        </div>
        <div className="Label">JVM</div>
        <div className="JVM">
            <div ref={gcRef} className="Chart"></div>
            <div ref={threadRef} className="Chart"></div>
            <div ref={jvmMemoryRef} className="Chart"></div>
            <div ref={classRef} className="Chart"></div>
            <div ref={jvmCpuRef} className="Chart"></div>
            <div className="Chart">
                <div className="Title">MEMORYPOOL</div>
                <MemoryPool />
            </div>
        </div>
    </div>
}
function MemoryPool() {
    const [data, setData] = useState()
    useEffect(function () {
        (async function () {
            try{
                const res = await axios.get("/argus/api/monitor/MemoryPool")
                setData(res.data.data)
            } catch (error: any) {
                
            }
        })()
    }, [])
    return <div className="MemoryPool">
        <span className="Time">最近时间 {data && new Date().toLocaleString()}</span>
        <Table size="small" rowKey="name"
            pagination={{ hideOnSinglePage: true, size: "small", pageSize: 5, showSizeChanger: false }}
            dataSource={data}
            columns={[
                {
                    title: "name",
                    dataIndex: "name"
                },
                {
                    title: "max",
                    dataIndex: "max",
                    sorter: true,
                },
                {
                    title: "used",
                    dataIndex: "used",
                    sorter: true,
                },
                {
                    title: "init",
                    dataIndex: "init",
                    sorter: true,
                },
                {
                    title: "committed",
                    dataIndex: "committed",
                    sorter: true,
                },
            ]}
        />
    </div>
}