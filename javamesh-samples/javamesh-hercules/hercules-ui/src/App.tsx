import React, { useContext, useEffect, useState } from 'react'
import { NavLink, Route, Switch, useHistory, useLocation } from 'react-router-dom'
import './App.scss'
import axios from 'axios'
import PerformanceTest from './PerformanceTest'
import Login from './Login';
import AppHome from './AppHome'
import DisasterRecovery from './DisasterRecovery'
import Logo from './Logo.png'
import { HomeOutlined, SettingOutlined, ThunderboltOutlined, AppstoreOutlined, DownOutlined } from '@ant-design/icons'
import { Button, Dropdown, Form, Input, Menu, message, Modal } from 'antd'
import Context from './ContextProvider'
import NoMatch from './component/NoMatch'
import NoLogin from './component/NoLogin'
import SystemConfig from './SystemConfig'

export default function App() {
  const history = useHistory()
  const { auth, setAuth } = useContext(Context)
  useEffect(function () {
    const interceptor = axios.interceptors.response.use(function (response) {
      if (response.data?.msg) {
        return Promise.reject(new Error(response.data.msg));
      }
      return response;
    }, function (error) {
      if (error.response?.status === 401) {
        setAuth([])
        const { pathname, search, hash } = window.location
        history.replace("/Login", pathname + search + hash)
      }
      return Promise.reject(error);
    });
    return function () {
      axios.interceptors.response.eject(interceptor);
    }
  }, [history, setAuth])
  const menuList = [
    { path: "/SystemConfig", label: "系统配置", comp: <SystemConfig />, icon: <SettingOutlined />, auth: "admin" },
    { path: "/PerformanceTest", label: "性能测试", comp: <PerformanceTest />, icon: <AppstoreOutlined /> },
    { path: "/DisasterRecovery", label: "容灾切换", comp: <DisasterRecovery />, icon: <ThunderboltOutlined /> }
  ]

  return <Switch>
    <Route key="/Login" exact path="/Login">
      <Login />
    </Route>
    <Route key="/" path="/">
      <div className="App">
        <div className="AppMenu">
          <div className="Logo">
            <img src={Logo} alt="" width="25" />
          </div>
          <div className="Menu">
            <NavLink to="/" className="Item" exact>
              <div className="Content"><HomeOutlined />概览</div>
            </NavLink>
            {menuList.map(function (item) {
              if (auth.length && item.auth && !auth.includes(item.auth)) return null
              return <NavLink key={item.path} to={item.path} className="Item">
                <div className="Content">{item.icon}{item.label}</div>
              </NavLink>
            })}
          </div>
        </div>
        <div className="AppContent">
          <div className="AppHeader">
            <Account />
          </div>
          <Switch>
            <Route path="/" exact><AppHome /></Route>
            {menuList.map(function (item) {
              if (auth.length && item.auth && !auth.includes(item.auth)) return null
              return <Route key={item.path} path={item.path}>{auth.length > 0 ? item.comp : <NoLogin />}</Route>
            })}
            <Route path="*"><NoMatch /></Route>
          </Switch>
        </div>
      </div>
    </Route>
  </Switch>
}

function Account() {
  const history = useHistory()
  const location = useLocation()
  const { setAuth } = useContext(Context)
  let [user, setUser] = useState<{ nickname: string, role: string, username: string }>()
  useEffect(function () {
    (async function () {
      try {
        const res = await axios.get("/argus-user/api/user/me")
        setUser(res.data.data)
        setAuth(res.data.data.auth)
      } catch (error: any) {
        
      }
    })()
  }, [setAuth])
  return user ? <Dropdown overlay={
    <Menu>
      <Menu.Item key="1"><ChangePwd username={user.username} /></Menu.Item>
      <Menu.Item key="2"><Logout /></Menu.Item>
    </Menu>}>
    <div className="Account">{user.nickname}-{user.role}<DownOutlined /></div>
  </Dropdown> :
    <div className="Account" onClick={function () {
      const { pathname, search, hash } = location
      history.replace("/Login", pathname + search + hash)
    }}>请登录</div>
}

function ChangePwd({ username }: { username: string }) {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [form] = Form.useForm();
  return <>
    <span onClick={function () {
      setIsModalVisible(true)
    }}>修改密码</span>
    <Modal className="ChangePwd" title="修改密码" width={400} visible={isModalVisible} maskClosable={false} footer={null} onCancel={function () {
      setIsModalVisible(false)
    }}>
      <Form form={form} initialValues={{ username }} labelCol={{ span: 4 }} requiredMark={false} onFinish={async function (values) {
        try {
          await axios.post("/argus-user/api/user/chagnePwd", values)
          form.resetFields()
          setIsModalVisible(false)
          message.success("修改密码成功")
        } catch (error: any) {
          message.error(error.message)
        }
      }}>
        <Form.Item name="username" hidden>
          <Input />
        </Form.Item>
        <Form.Item name="old_password" label="原密码">
          <Input.Password required maxLength={15} />
        </Form.Item>
        <Form.Item name="password" label="新密码" rules={[{
          pattern: /^\w{6,15}$/,
          message: "不得少于6个字且不得超过15个字，只能输入字母、数字、下划线"
        }]}>
          <Input.Password required maxLength={15} />
        </Form.Item>
        <Form.Item name="confirm" label="确认密码" rules={[{
          async validator(_, value) {
            if (value !== form.getFieldValue("password")) {
              throw new Error("必须与新密码一致")
            }
          },
        }]}>
          <Input.Password required maxLength={15} />
        </Form.Item>
        <Form.Item className="Buttons">
          <Button type="primary" htmlType="submit">提交</Button>
          <Button onClick={function () {
            setIsModalVisible(false)
          }}>取消</Button>
        </Form.Item>
      </Form>
    </Modal>
  </>
}

function Logout() {
  const { setAuth } = useContext(Context)
  const history = useHistory()
  return <span onClick={async function () {
    try {
      await axios.post('/argus-user/api/user/logout')
      setAuth([])
      const { pathname, search, hash } = window.location
      history.replace("/Login", pathname + search + hash)
    } catch (error: any) {
      message.error(error.message)
    }
  }}>退出登录</span>
}