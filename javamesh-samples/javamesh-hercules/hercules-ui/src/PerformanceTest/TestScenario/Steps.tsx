import React from "react"
import "./Steps.scss"

export default function App(props: { keys: string[], activeIndex: number }) {
    return <div className="Steps">{props.keys.map(function (item, index) {
        return <div className="Step" key={index}>
            {index !== 0 && <Sep className={`Tail${props.activeIndex === index ? " active": ""}`} />}
            <div className={`Item${props.activeIndex === index ? " active": ""}`}>{item}</div>
            <Sep className={`Head${props.activeIndex === index ? " active": ""}`} />
        </div>
    })}</div>
}

const svg = <svg style={{
    position: "absolute",
    transform: "rotate(90deg) translate(9px, 9px)",
    height: "23px",
    width: "41px",
    fill: "currentColor"
}} >
    <path d="M 20.5 0 L 41 23 L 0 23 Z"></path>
</svg>
function Sep(props: { className: string }) {
    return <span className={props.className} style={{
        lineHeight: "0",
        position: "relative",
        display: "inline-block",
        height: "41px",
        width: "23px"
    }}>{svg}</span>
}