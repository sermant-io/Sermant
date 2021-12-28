import { Button } from "antd"
import React from "react"
import { useHistory } from "react-router-dom"
import NoLogin from "./NoLogin.png"
import "./NoLogin.scss"

export default function App() {
    const history = useHistory()
    return <div className="NoLogin">
        <img src={NoLogin} alt="" />
        <div className="Title">请<Button type="link" size="large" onClick={function(){
            const { pathname, search, hash } = window.location
            history.replace("/Login", pathname + search + hash)
        }}>登录</Button>后查看内容</div>
    </div>
}