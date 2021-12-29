import { Button, Form, Input, message, Radio, Select } from "antd"
import React, { useEffect } from "react"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import MonacoEditor from 'react-monaco-editor'
import { useHistory, useLocation } from "react-router-dom"
import axios from "axios"
import "./index.scss"
import DebugScript from "../DebugScript"

export default function App() {
    let submit = false
    const history = useHistory()
    const urlSearchParams = new URLSearchParams(useLocation().search)
    const script_id = urlSearchParams.get("script_id")
    const [form] = Form.useForm()
    useEffect(function () {
        (async function () {
            try {
                const res = await axios.get('/argus-emergency/api/script/get', { params: { script_id } })
                form.setFieldsValue(res.data.data)
            } catch (error: any) {

            }
        })()
    }, [form, script_id])
    return <div className="ScriptUpdate">
        <Breadcrumb label="脚本管理" sub={{ label: "详情", parentUrl: "/DisasterRecovery/ScriptManage" }} />
        <Card>
            <Form form={form} requiredMark={false} labelCol={{ span: 3 }}
                initialValues={{ language: "Shell", pwd_from: "本地", script_from: "手工录入", public: "私有", has_pwd: "无" }}
                onFinish={async function (values) {
                    if (submit) return
                    submit = true
                    try {
                        await axios.put('/argus-emergency/api/script', values)
                        message.success("更新成功")
                        history.goBack()
                    } catch (e: any) {
                        message.error(e.message)
                    }
                    submit = false
                }}>
                <div className="Line">
                    <Form.Item className="Middle" name="script_name" label="脚本名">
                        <Input disabled />
                    </Form.Item>
                    <Form.Item className="Middle" name="language" label="脚本分类">
                        <Select disabled options={[{ value: "Shell" }, { value: "Jython", disabled: true }, { value: "Groovy", disabled: true }]} />
                    </Form.Item>
                    <Form.Item className="Middle" name="public" label="是否公有">
                        <Radio.Group disabled options={["私有", "公有"]} />
                    </Form.Item>
                </div>
                <Form.Item labelCol={{ span: 1 }} label="脚本用途" name="submit_info" rules={[{ required: true }]}>
                    <Input.TextArea maxLength={50} showCount autoSize={{ minRows: 2, maxRows: 2 }} />
                </Form.Item>
                <Form.Item label="脚本内容" className="Editor WithoutLabel" name="content" rules={[{ required: true, max: 5000 }]}>
                    <MonacoEditor height="200" language="shell" />
                </Form.Item>
                <DebugScript form={form}/>
                <Form.Item className="ScriptParam" labelCol={{ span: 1 }} name="param" label="脚本参数" rules={[{
                    pattern: /^[\w,.|]+$/,
                    message: "格式错误"
                }]}>
                    <Input.TextArea className="Param" showCount maxLength={50} autoSize={{ minRows: 2, maxRows: 2 }}
                        placeholder="测试参数可以在脚本中通过System.getProperty('param')取得，参数只能为数字、字母、下划线、逗号、圆点（.）或竖线(|)组成，禁止输入空格，长度在0-50之间。" />
                </Form.Item>
                <Form.Item className="Buttons">
                    <Button className="Save" htmlType="submit" type="primary">提交</Button>
                    <Button onClick={function(){
                        history.goBack()
                    }}>取消</Button>
                </Form.Item>
            </Form>
        </Card>
    </div>
}