import React from "react"
import { Route, Switch, useRouteMatch } from "react-router-dom"
import SubMenu from "../component/SubMenu"
import Home from "./Home"
import MonitorAnalyzer from "./MonitorAnalyzer"
import QuickStart from "./QuickStart"
import ScriptManage from "./ScriptManage"
import TestAgent from "./TestAgent"
import TestReport from "./TestReport"
import TestScenario from "./TestScenario"
import TestTask from "./TestTask"
import "./index.scss"
import NoMatch from "../component/NoMatch"

export default function App() {
  const { path } = useRouteMatch();
    const menuList = [
        { path: path + "/", label: "概览", comp: <Home />, exact: true },
        { path: path + "/QuickStart", label: "快速开始", comp: <QuickStart />, exact: false },
        { path: path + "/TestScenario", label: "压测场景", comp: <TestScenario />, exact: false },
        { path: path + "/TestTask", label: "压测任务", comp: <TestTask />, exact: false },
        { path: path + "/ScriptManage", label: "脚本管理", comp: <ScriptManage />, exact: false },
        { path: path + "/TestAgent", label: "压测引擎", comp: <TestAgent />, exact: false },
        { path: path + "/MonitorAnalyzer", label: "监控分析", comp: <MonitorAnalyzer />, exact: false },
        { path: path + "/TestReport", label: "压测报告", comp: <TestReport />, exact: false },
      ]
    return <div className="AppBody">
    <SubMenu menuList={menuList}>性能测试</SubMenu>
    <div className="AppRoute">
      <Switch>
        {menuList.map(function (item) {
          return <Route key={item.path} exact={item.exact} path={item.path}>{item.comp}</Route>
        })}
        <Route path="*"><NoMatch /></Route>
      </Switch>
    </div>
  </div>
}