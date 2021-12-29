import { Button, Form, Input, message, Modal } from "antd"
import React, { ReactNode, useState } from "react"
import { InfoCircleOutlined } from '@ant-design/icons'
import "./HostForm.scss"

export default function App(props: { children: ReactNode, onFinish: (values: any) => Promise<void> }) {
    let submit = false
    const [isModalVisible, setIsModalVisible] = useState(false);
    return <>
        <span onClick={function () { setIsModalVisible(!isModalVisible) }}>{props.children}</span>
        <Modal width={600} className="HostForm" visible={isModalVisible} title="添加主机" footer={null} onCancel={function () {
            setIsModalVisible(false)
        }}>
            <Form className="Form" onFinish={async function(values) {
                if(!values.ip && !values.domain) {
                    message.error("请至少设置1个选项。")
                    return
                }
                if (submit) return
                submit = true
                try {
                    await props.onFinish(values)
                    setIsModalVisible(false)
                } catch (e: any) {
                    message.error(e.message)
                }
                submit = false
            }}>
                <div className="Info">
                    <InfoCircleOutlined />
                    <span>请至少设置1个选项。</span>
                </div>
                <Form.Item label="域" name="domain" rules={[{
                    pattern: /^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\.?$/,
                    message: "请输入域名"
                }]}>
                    <Input />
                </Form.Item>
                <Form.Item label="IP" name="ip" rules={[{
                    pattern: /^((25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))\.){3}(25[0-5]|2[0-4]\d|((1\d{2})|([1-9]?\d)))$/,
                    message: "请输入IP地址"
                }]}>
                    <Input />
                </Form.Item>
                <Form.Item className="Buttons">
                    <Button type="primary" htmlType="submit">添加</Button>
                    <Button onClick={function () {
                        setIsModalVisible(false)
                    }}>取消</Button>
                </Form.Item>
            </Form>
        </Modal>
    </>
}