import React from "react"
import Dynamic from './Dynamic.png'
import TCPCopy from './TCPCopy.png'
import Script from './Script.png'
import "./Scenario.scss"

export function DynamicCard(props: {scenario?: string, onClick?: (vlaue: string) => void}){
    return <div className={"Scenario"+(props.scenario === "Dynamic" ? " active" : "")} onClick={function(){
        props.onClick?.("Dynamic")
    }}>
        <img src={Dynamic} alt="" />
        <div className="Content">
            <h2 className="Title">动态编排</h2>
            <p className="Text">简单快捷，极易上手</p>
        </div>
    </div>
}

export function TCPCopyCard(props: {scenario?: string, onClick?: (vlaue: string) => void}){
    return <div className={"Scenario"+(props.scenario === "TCPCopy" ? " active" : "")} onClick={function(){
        props.onClick?.("TCPCopy")
    }}>
        <img src={TCPCopy} alt="" />
        <div className="Content">
            <h2 className="Title">引流压测</h2>
            <p className="Text">ARGUS特色</p>
        </div>
    </div>
}

export function ScriptCard(props: {scenario?: string, onClick?: (vlaue: string) => void}){
    return <div className={"Scenario"+(props.scenario === "Script" ? " active" : "")} onClick={function(){
        props.onClick?.("Script")
    }}>
        <img src={Script} alt="" />
        <div className="Content">
            <h2 className="Title">自定义脚本</h2>
            <p className="Text">自定义脚本</p>
        </div>
    </div>
}