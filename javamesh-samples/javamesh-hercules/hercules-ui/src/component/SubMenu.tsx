import React, { useState } from "react"
import { NavLink } from "react-router-dom"
import "./SubMenu.scss"

export default function App(props: { menuList: { path: string; label: string; comp: JSX.Element; exact: boolean; }[], children: React.ReactNode }) {
    const [active, setActive] = useState(true)
    return <div className={`SubMenu${active ? ' active' : ''}`} >
      <div className="Wapper">
        <div className="Menu">
          <div className="Title">{props.children}</div>
          {props.menuList.map(function (item) {
            return <NavLink key={item.path} exact={item.exact} to={item.path} className="Item">{item.label}</NavLink>
          })}
        </div>
      </div>
      <div className="Icon" onClick={function () { setActive(!active) }}>
        <div className="rect"></div>
        <span className={`icon fa${active ? " fa-long-arrow-left" : " fa-long-arrow-right"}`}></span>
        <div className="rect"></div>
      </div>
    </div>
  }