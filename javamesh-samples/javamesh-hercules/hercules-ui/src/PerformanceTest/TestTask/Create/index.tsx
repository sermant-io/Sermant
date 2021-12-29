import React from "react"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import PageInfo from "../../../component/PageInfo"
import './index.scss'
import TaskForm from "./TaskForm"

export default function App(){
    return <div className="TaskCreate">
        <Breadcrumb label="压测任务" sub={{ label: "创建测试", parentUrl: "/PerformanceTest/TestTask" }} />
        <PageInfo>选择引流压测，请先完成流量采集，点击采集任务创建流量采集<a href="/fetch"> “采集任务” </a>。</PageInfo>
        <Card>
            <TaskForm/>
        </Card>
    </div>
}