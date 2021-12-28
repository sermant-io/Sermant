import React, { ReactNode } from "react"
import "./PageInfo.scss"

export default function App(props: {children: ReactNode}) {
    return <div className="PageInfo">
        <span className="icon md">info_outline</span>
        {props.children}
    </div>
}