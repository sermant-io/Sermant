import { Descriptions } from "antd"
import axios from "axios"
import React from "react"
import { useEffect, useState } from "react"

export default function PlanInfo({ plan_id }: { plan_id: string }) {
    const [data, setData] = useState({ plan_no: "", plan_name: "" })
    useEffect(function () {
      (async function () {
        try {
          const res = await axios.get("/argus-emergency/api/plan/get", { params: { plan_id } })
          setData(res.data.data)
        } catch (error: any) {
  
        }
      })()
    }, [plan_id])
    return <Descriptions className="Desc">
      <Descriptions.Item label="预案编号">{data.plan_no}</Descriptions.Item>
      <Descriptions.Item label="预案名称">{data.plan_name}</Descriptions.Item>
    </Descriptions>
  }