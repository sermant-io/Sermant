import React from "react"
import Breadcrumb from "../../../component/Breadcrumb"
import StepForm from "../StepForm"

export default function App() {
    return <div className="ScenarioCreate">
        <StepForm  breadcrumb={<Breadcrumb label="压测场景" sub={{ label: "新增场景", parentUrl: "/PerformanceTest/TestScenario" }} />} />
    </div>
}