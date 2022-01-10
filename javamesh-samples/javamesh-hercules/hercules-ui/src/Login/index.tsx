import { Button, Form, Input, message, Select } from "antd"
import React, { useState } from "react"
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import Background from './Background.svg'
import "./index.scss"
import axios from "axios"
import { useHistory, useLocation } from "react-router-dom"

export default function App() {
    let [isLogin, setIsLogin] = useState(true)
    return <div className="Login">
        <div className="Image">
            <div className="Title">Argus平台</div>
            <img className="Background" src={Background} alt="" />
        </div>
        <div className="Form">{isLogin ?
            <Login setIsLogin={setIsLogin} /> :
            <Registe setIsLogin={setIsLogin} />
        }</div>
    </div>
}

function Login(props: { setIsLogin: (isLogin: boolean) => void }) {
    const history = useHistory();
    const location = useLocation();
    return <Form onFinish={async function (value) {
        try {
            await axios.post("/argus-user/api/user/login", value)
            const state = location.state as string || ""
            history.replace(state)
        } catch (error: any) {
            message.error(error.message)
        }
    }}>
        <Form.Item name="username">
            <Input prefix={<UserOutlined />} placeholder="登录账号" required maxLength={15} />
        </Form.Item>
        <Form.Item name="password">
            <Input.Password prefix={<LockOutlined />} placeholder="登录密码" required maxLength={15} />
        </Form.Item>
        <Form.Item>
            <Button type="primary" htmlType="submit" >登录</Button>
            <span>或</span>
            <Button type="link" onClick={function () { props.setIsLogin(false) }}>注册</Button>
        </Form.Item>
    </Form>
}

function Registe(props: { setIsLogin: (isLogin: boolean) => void }) {
    const [form] = Form.useForm();
    return <Form form={form} initialValues={{ role: "操作员" }} onFinish={async function (values) {
        try {
            await axios.post("/argus-user/api/user/registe", values)
            message.success("注册成功请登录")
            props.setIsLogin(true)
        } catch (error: any) {
            message.error(error.message)
        }
    }}>
        <Form.Item name="username" rules={[{
            pattern: /^\w{6,15}$/,
            message: "不得少于6个字且不得超过15个字, 只能输入字母、数字、下划线"
        }]}>
            <Input prefix={<UserOutlined />} placeholder="登录账号" required maxLength={15} />
        </Form.Item>
        <Form.Item name="nickname">
            <Input prefix={<UserOutlined />} placeholder="用户名称" required maxLength={15} />
        </Form.Item>
        <Form.Item name="password" rules={[{
            pattern: /^\w{6,15}$/,
            message: "不得少于6个字且不得超过15个字, 只能输入字母、数字、下划线"
        }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="登录密码" required maxLength={15} />
        </Form.Item>
        <Form.Item name="confirm" rules={[{
            async validator(_, value) {
                if (value !== form.getFieldValue("password")) {
                    throw new Error("必须与新密码一致")
                }
            },
        }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
        </Form.Item>
        <Form.Item name="role">
            <Select options={[{ value: "操作员" }, { value: "审核员" }]} placeholder="角色" />
        </Form.Item>
        <Form.Item>
            <Button type="primary" htmlType="submit" >注册</Button>
            <span>或</span>
            <Button type="link" onClick={function () { props.setIsLogin(true) }}>去登录</Button>
        </Form.Item>
    </Form>
}