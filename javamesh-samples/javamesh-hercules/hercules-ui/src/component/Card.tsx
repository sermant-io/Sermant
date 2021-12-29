import React, { ReactNode } from "react"
import "./Card.scss"

export default function App(props: {children: ReactNode, height?: string, className?: string}){
    return <div className={"Card " + props.className} style={{height: props.height}}>{props.children}</div>
}