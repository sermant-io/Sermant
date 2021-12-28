import React from "react"
import Breadcrumb from "../../../component/Breadcrumb"
import StepForm from "../StepForm"

export default function App() {
    return <div className="ScenarioCreate">
        <StepForm pageInfo={<>选择引流压测，请先完成流量采集，点击采集任务创建流量采集<a href="/fetch"> “采集任务” </a>。</>}
            breadcrumb={<Breadcrumb label="压测场景" sub={{ label: "新增场景", parentUrl: "/PerformanceTest/TestScenario" }} />}
        />
    </div>
}