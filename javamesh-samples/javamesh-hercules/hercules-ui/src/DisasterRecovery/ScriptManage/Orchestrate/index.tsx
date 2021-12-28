import { Modal } from "antd"
import axios from "axios"
import React, { useEffect, useState } from "react"
import { useLocation } from "react-router-dom"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import "./index.scss"
import TreeOrchestrate, { Values } from "../../../component/TreeOrchestrate"

export default function App() {
    const script_id = new URLSearchParams(useLocation().search).get("script_id") || ""
    const [data, setData] = useState<Values>()
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get("/argus-emergency/api/script/orchestrate/get", { params: { script_id } })
                const tree = res.data.data.tree
                const mapData = res.data.data.map
                const map = new Map()
                for (const key in mapData) {
                    map.set(key, mapData[key])
                }
                setData({ tree, map })
            } catch (error: any) {

            }
        })()
    }, [script_id])
    return <div className="ScriptOrchestrate">
        <Breadcrumb label="脚本管理" sub={{ label: "编排", parentUrl: "/DisasterRecovery/ScriptManage" }} />
        <Card>
            {data && <TreeOrchestrate initialValues={data} onSave={async function (values) {
                console.log(values)
                try {
                    const map: any = {}
                    values.map.forEach(function (value, key) {
                        map[key] = value
                    })
                    await axios.put("/argus-emergency/api/script/orchestrate", { tree: values.tree, map, script_id })
                } catch (error: any) {
                    Modal.error({
                        title: "保存失败",
                        content: error.message,
                        onOk: function () {
                            window.history.back()
                        }
                    })
                }
            }} />}
        </Card>
    </div>
}