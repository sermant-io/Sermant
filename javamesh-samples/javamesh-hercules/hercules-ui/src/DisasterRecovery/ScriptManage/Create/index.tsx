import { Button, Form, Input, message, Radio, Select } from "antd"
import React, { useRef, useState } from "react"
import Breadcrumb from "../../../component/Breadcrumb"
import Card from "../../../component/Card"
import MonacoEditor from 'react-monaco-editor'
import { useHistory } from "react-router-dom"
import axios from "axios"
import { debounce } from 'lodash';
import "./index.scss"
import ServiceSelect from "../../../component/ServiceSelect"
import Upload from "../../../component/Upload"
import DebugScript from "../DebugScript"

export default function App() {
    let submit = false
    const history = useHistory()
    const [form] = Form.useForm()
    return <div className="ScriptCreate">
        <Breadcrumb label="脚本管理" sub={{ label: "创建", parentUrl: "/DisasterRecovery/ScriptManage" }} />
        <Card>
            <Form form={form} requiredMark={false} labelCol={{ span: 3 }}
                initialValues={{ language: "Shell", pwd_from: "本地", script_from: "手工录入", public: "私有", has_pwd: "无" }}
                onFinish={async function (values) {
                    if (submit) return
                    submit = true
                    try {
                        if (values.script_from === "本地导入") {
                            const formData = new FormData()
                            formData.append('file', values.file[0]);
                            delete values.file
                            for (let key in values) {
                                formData.append(key, values[key]);
                            }
                            const res = await fetch('/argus-emergency/api/script/upload', {
                                method: 'POST',
                                body: formData
                            })
                            if (!res.ok) {
                                throw new Error("Request failed with status code " + res.status)
                            }
                            const json = await res.json()
                            if (json.msg) {
                                throw new Error(json.msg)
                            }
                        } else {
                            await axios.post('/argus-emergency/api/script', values)
                        }
                        message.success("创建成功")
                        history.goBack()
                    } catch (e: any) {
                        message.error(e.message)
                    }
                    submit = false
                }}>
                <div className="Line">
                    <Form.Item className="Middle" name="script_name" label="脚本名" rules={[
                        { max: 25, required: true, whitespace: true },
                        { pattern: /^\w+$/, message: "请输入英文、数字、下划线" }
                    ]}>
                        <Input />
                    </Form.Item>
                    <Form.Item className="Middle" name="language" label="脚本分类">
                        <Select options={[{ value: "Shell" }, { value: "Jython", disabled: true }, { value: "Groovy", disabled: true }]} />
                    </Form.Item>
                    <Form.Item className="Middle" name="public" label="是否公有">
                        <Radio.Group options={["私有", "公有"]} />
                    </Form.Item>
                </div>
                <Form.Item labelCol={{ span: 1 }} label="脚本用途" name="submit_info" rules={[{ required: true }]}>
                    <Input.TextArea maxLength={50} showCount autoSize={{ minRows: 2, maxRows: 2 }} />
                </Form.Item>
                <Script />
                <DebugScript form={form}/>
                <Form.Item className="ScriptParam" labelCol={{ span: 1 }} name="param" label="脚本参数" rules={[{
                    pattern: /^[\w,.|]+$/,
                    message: "格式错误"
                }]}>
                    <Input.TextArea showCount maxLength={50} autoSize={{ minRows: 2, maxRows: 2 }}
                        placeholder="测试参数可以在脚本中通过System.getProperty('param')取得，参数只能为数字、字母、下划线、逗号、圆点（.）或竖线(|)组成，禁止输入空格，长度在0-50之间。" />
                </Form.Item>
                <Form.Item className="Buttons">
                    <Button className="Save" htmlType="submit" type="primary">提交</Button>
                    <Button htmlType="reset">重置</Button>
                </Form.Item>
            </Form>
        </Card>
    </div>
}

function Script() {
    const [scriptFrom, setScriptFrom] = useState("input")
    const [script, setScript] = useState("")
    return <>
        <div className="Line">
            <Form.Item className="Middle" name="script_from" label="脚本来源">
                <Radio.Group onChange={function (e) {
                    setScriptFrom(e.target.value)
                }} options={["手工录入", "脚本克隆", "本地导入"]} />
            </Form.Item>
            {scriptFrom === "脚本克隆" && <Form.Item className="Middle" name="content_from" label="克隆来源">
                <ServiceSelect url='/argus-emergency/api/script/search' onChange={async function (name) {
                    try {
                        const res = await axios.get("/argus-emergency/api/script/getByName", { params: { name } })
                        setScript(res.data.data.content)
                    } catch (error: any) {

                    }
                }} />
            </Form.Item>}
            {scriptFrom === "本地导入" && <Form.Item className="Middle" name="file" label="文件" rules={[{ required: true }]}>
                <Upload max={1} />
            </Form.Item>}
        </div>
        {scriptFrom !== "本地导入" && <Form.Item label="脚本内容" className="Editor WithoutLabel" name="content" rules={[{ required: true, max: 5000 }]}>
            <ScriptEditor script={script} setScript={setScript} />
        </Form.Item>}
    </>
}

function ScriptEditor(props: { onChange?: (value: string) => void, script: string, setScript: (script: string) => void }) {
    const debounceRef = useRef(debounce(function (value: string) {
        props.setScript(value)
        props.onChange?.(value)
    }, 1000))
    return <MonacoEditor height="200" language="shell" value={props.script} onChange={debounceRef.current} />
}