import React from "react";
import { useLocation } from "react-router-dom";
import Breadcrumb from "../../../component/Breadcrumb";
import Card from "../../../component/Card";
import "./index.scss"
import PlanInfo from "./PlanInfo";
import TreeEditor from "./TreeEditor";

export default function App() {
  const urlSearchParams = new URLSearchParams(useLocation().search)
  const plan_id = urlSearchParams.get("plan_id") || ""
  return <div className="PlanEditor">
    <Breadcrumb label="预案管理" sub={{ label: "编辑预案", parentUrl: "/DisasterRecovery/PlanManage" }} />
    <Card>
      <PlanInfo plan_id={plan_id} />
      <TreeEditor plan_id={plan_id} />
    </Card>
  </div>
}