import React from "react"
import { Link } from "react-router-dom"
import './Breadcrumb.scss'

export default function App(props: { label: string, sub?: {label: string, parentUrl: string}}) {
    return <div className="Breadcrumb">
        {props.sub ? <><Link to={props.sub.parentUrl}>{props.label}</Link>&nbsp;/&nbsp;{props.sub.label}</> : props.label}
    </div>
}