import React from "react"
import Breadcrumb from "../../component/Breadcrumb"
import StepForm from "../TestScenario/StepForm"

export default function App() {
    return <StepForm pageInfo={"快速压测"}
        breadcrumb={<Breadcrumb label="快速开始" />}
        quickStart={true}
    />
}