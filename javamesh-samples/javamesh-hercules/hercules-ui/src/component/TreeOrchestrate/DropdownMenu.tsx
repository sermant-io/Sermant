import { Dropdown, Menu } from "antd"
import MenuItem from "antd/lib/menu/MenuItem"
import React from "react"
// 菜单配置
type MenuConfig = { type: string, title: string, children?: MenuConfig[] }
const Sampler: MenuConfig = {
    type: "Sampler", title: "取样器", children: [
        { type: "HTTPRequest", title: "HTTP请求" },
        { type: "JARImport", title: "JAR导入" },
    ]
}
const LogicController: MenuConfig = {
    type: "LogicController", title: "逻辑控制器", children: [
        { type: "TransactionController", title: "事务控制器" },
        { type: "LoopController", title: "循环控制器" },
        { type: "WhileController", title: "While控制器" }
    ]
}
const Timer: MenuConfig = {
    type: "Timer", title: "定时器", children: [
        { type: "ConstantTimer", title: "固定定时器" }
    ]
}
const PreProcessor: MenuConfig = {
    type: "PreProcessor", title: "前置处理器", children: [
        { type: "JSR223PreProcessor", title: "JSR223 预处理程序" }
    ]
}
const PostProcessor: MenuConfig = {
    type: "PostProcessor", title: "后置处理器", children: [
        { type: "JSR223PostProcessor", title: "JSR223 后置处理程序" },
    ]
}
const Assertions: MenuConfig = {
    type: "Assertions", title: "断言器", children: [
        { type: "ResponseAssertion", title: "响应断言" }
    ]
}
const TestGroup: MenuConfig = {
    type: "TestGroup", title: "代码块", children: [
        { type: "TestFunc", title: "方法代码块" }
    ]
}
const common = [Sampler, LogicController, Timer, PreProcessor, PostProcessor, Assertions]
const menuGroup = new Map<String, MenuConfig[]>([
    ["Root", [...common, TestGroup]],
    ["BeforeProcess", common],
    ["BeforeThread", common],
    ["AfterProcess", common],
    ["AfterThread", common],
    ["Before", common],
    ["After", common],
    ["TransactionController", common]
])
// 计算规则，菜单
const rules = new Map<String, Set<String>>()
const menus = new Map([
    ["Root", "脚本"],
    ["BeforeProcess", "前置处理器"],
    ["BeforeThread", "线程前置处理器"],
    ["AfterProcess", "后置处理器"],
    ["AfterThread", "线程后置处理器"],
    ["Before", "方法前置处理器"],
    ["After", "方法后置处理器"],
])

function loop(menuItem: MenuConfig): MenuConfig[] {
    // 取出末端菜单项
    if (menuItem.children) {
        return menuItem.children.flatMap(loop)
    }
    return [menuItem]
}
menuGroup.forEach(function (value, key) {
    const set = new Set<String>()
    value.flatMap(loop).forEach(function (item) {
        set.add(item.type)
        menus.set(item.type, item.title)
    })
    rules.set(key, set)
})
export { rules, menus }

export default function App(props: {
    children: React.ReactNode, type: string,
    onDelete?: () => void, onAdd: (type: string) => void,
    menuItems?: MenuItem[]
}) {
    const menu = menuGroup.get(props.type)
    return <Dropdown overlay={<Menu>
        {menu && <Menu.SubMenu key="add" title="添加">{menu.map(function (item) {
            if (item.children) {
                return <Menu.SubMenu key={item.type} title={item.title}>{item.children.map(function (item) {
                    return <Menu.Item onClick={function () {
                        props.onAdd?.(item.type)
                    }} key={item.type}>{item.title}</Menu.Item>
                })}</Menu.SubMenu>
            } else {
                return <Menu.Item onClick={function () {
                    props.onAdd?.(item.type)
                }} key={item.type}>{item.title}</Menu.Item>
            }
        })}</Menu.SubMenu>}
        {<Menu.Item disabled={!props.onDelete} onClick={props.onDelete} key="delete">删除</Menu.Item>}
    </Menu>} trigger={['contextMenu']}>{props.children}</Dropdown>
}