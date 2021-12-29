
import React from "react"
import Breadcrumb from "../../component/Breadcrumb"
import Card from "../../component/Card"
import "./index.scss"
import Monitor from "./Monitor"


export default function App() {
    
    return <div className="MonitorAnalyzer">
        <Breadcrumb label="监控分析" />
        <Card>
            <Monitor />
        </Card>
    </div>
}