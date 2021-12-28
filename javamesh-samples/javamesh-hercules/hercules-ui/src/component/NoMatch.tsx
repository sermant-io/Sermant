import React from "react"
import NoLogin from "./NoMatch.png"
import "./NoMatch.scss"

export default function App() {
    return <div className="NoMatch">
        <img src={NoLogin} alt="" />
        <div className="Title">404 找不到内容</div>
    </div>
}