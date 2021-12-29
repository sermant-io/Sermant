import { Button } from "antd"
import React, { useContext } from "react"
import { Link } from "react-router-dom"
import Context from "../ContextProvider"
import Banner from "./Banner.png"
import "./index.scss"
import Limiting from "./Limiting.png"
import Oam from "./Oam.png"

export default function App() {
    const { auth } = useContext(Context)
    const list = [
        { title: "全链路压测", desc: "全链路压测，性能测试！", img: Limiting, link: "/PerformanceTest" },
        { title: "容灾切换", desc: "预案、场景、任务、脚本。", img: Limiting, link: "/DisasterRecovery" },
        { title: "系统管理", desc: "用户管理、权限管理、密码重置。", img: Oam, link: "/SystemConfig", auth: "admin" },
    ]
    return <div className="AppHome">
        <img className="Banner" src={Banner} alt="" />
        <div className="Grid">{list.map(function (item, index) {
            if (auth.length && item.auth && !auth.includes(item.auth)) return null
            return <div key={index} className="Item">
                <h2 className="Title">{item.title}</h2>
                <div className="Desc">{item.desc}</div>
                <img className="Img" src={item.img} alt="" />
                <Link to={item.link}><Button type="primary">查看详情</Button></Link>
            </div>
        })}
            <div className="Item">
                <h2 className="Title">敬请期待</h2>
            </div>
        </div>
    </div>
}