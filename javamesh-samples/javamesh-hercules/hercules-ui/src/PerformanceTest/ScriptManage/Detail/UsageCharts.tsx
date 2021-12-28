import { Line, LineOptions } from "@antv/g2plot"
import axios from "axios"
import React, { useEffect, useRef } from "react"
import "./UsageCharts.scss"

export default function App(props: { url: string, params: any }) {
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
        const usageChart = new Line(usageRef.current!!, {
            ...config, yField: 'usage', color: '#ff699f', yAxis: {
                label: {
                    formatter(text: any) {
                        return text + "%"
                    }
                }
            },
        })
        usageChart.render()
        const memoryChart = new Line(memoryRef.current!!, {
            ...config, yField: 'memory', color: "#15c4ff", yAxis: {
                label: {
                    formatter(text: any) {
                        return text + "M"
                    }
                }
            },
        })
        memoryChart.render()
        let data: any[] = []
        let timeInterval: any;
        timeInterval = setInterval(async function () {
            try {
                const res = await axios.get(props.url, { params: props.params })
                data.shift()
                data.push(res.data.data[0])
                usageChart.changeData(data)
                memoryChart.changeData(data)
            } catch (error: any) {
                clearInterval(timeInterval)
            }

        }, 1000);
        (async function () {
            try {
                const chartRes = await axios.get(props.url, { params: { ...props.params, start: -90, interval: 1 } })
                data = chartRes.data.data
                usageChart.changeData(data)
                memoryChart.changeData(data)
            } catch (error: any) {
                clearInterval(timeInterval)
            }
        })()
        return function () {
            clearInterval(timeInterval)
            usageChart.destroy()
            memoryChart.destroy()
        }
    }, [props.params, props.url])
    return <div className="UsageCharts">
        <div className="Label">CPU使用率</div>
        <div ref={usageRef} className="SubCard"></div>
        <div className="Label">内存使用率</div>
        <div ref={memoryRef} className="SubCard"></div>
    </div>
}