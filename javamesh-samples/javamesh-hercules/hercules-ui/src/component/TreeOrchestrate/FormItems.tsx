import { Col, Divider, Form, Input, InputNumber, Radio, Row, Select } from "antd"
import Checkbox from "antd/lib/checkbox/Checkbox"
import { FormItemLabelProps } from "antd/lib/form/FormItemLabel"
import { PlusCircleOutlined, MinusCircleOutlined } from '@ant-design/icons'
import React, { useState } from "react"
import "./FormItems.scss"
import MonacoEditor from "react-monaco-editor"

const defaults = new Map([
    ["Root", {
        sampling_interval: 2,
        sampling_ignore: 0,
    }]
])
export { defaults }

export default function App(props: { type: String }) {
    switch (props.type) {
        case "Root":
            return <>
                <Divider orientation="left">压测配置</Divider>
                <Form.Item name="agent" label="代理数" labelCol={{ span: 3 }} labelAlign="left" rules={[{ type: "integer" }]}>
                    <InputNumber min={1} />
                </Form.Item>
                <Form.Item name="vuser" label="虚拟用户数" labelCol={{ span: 3 }} labelAlign="left" rules={[{ type: "integer" }]}>
                    <InputNumber min={1} />
                </Form.Item>
                <RootBasicScenario labelCol={{ span: 3 }} labelAlign="left" label="基础场景" />
                <Form.Item labelCol={{ span: 3 }} labelAlign="left" label="采样间隔" name="sampling_interval" rules={[{ type: "integer" }]}>
                    <InputNumber className="InputNumber" min={0} />
                </Form.Item>
                <Form.Item labelCol={{ span: 3 }} labelAlign="left" label="忽略采样数量" name="sampling_ignore" rules={[{ type: "integer" }]}>
                    <InputNumber className="InputNumber" min={0} />
                </Form.Item>
                <Form.Item labelCol={{ span: 3 }} labelAlign="left" name="test_param" label="测试参数" rules={[{
                    pattern: /^[\w,.|]+$/,
                    message: "格式错误"
                }]}>
                    <Input.TextArea showCount maxLength={50} autoSize={{ minRows: 2, maxRows: 2 }}
                        placeholder="测试参数可以在脚本中通过System.getProperty('param')取得，参数只能为数字、字母、下划线、逗号、圆点（.）或竖线(|)组成，禁止输入空格，长度在0-50之间。" />
                </Form.Item>
                <RootPresure />
            </>
        case "TransactionController":
            return <>
                <Form.Item name="presure" label="压力分配（%）">
                    <InputNumber min={0} max={100} />
                </Form.Item>
            </>
        case "HTTPRequest":
            return <><Divider orientation="left">Web服务器</Divider>
                <Row gutter={24}>
                    <Col span="6">
                        <Form.Item label="协议" name="protocol">
                            <Input />
                        </Form.Item>
                    </Col>
                    <Col span="12">
                        <Form.Item label="服务器名称或IP" name="domain">
                            <Input />
                        </Form.Item>
                    </Col>
                    <Col span="6">
                        <Form.Item label="端口" name="port">
                            <Input />
                        </Form.Item>
                    </Col>
                </Row>
                <Divider orientation="left">HTTP请求</Divider>
                <Row gutter={24}>
                    <Col span="6">
                        <Form.Item name="method">
                            <Select options={[{ value: "GET" }, { value: "POST" }]} />
                        </Form.Item>
                    </Col>
                    <Col span="12">
                        <Form.Item label="路径" name="path">
                            <Input />
                        </Form.Item>
                    </Col>
                    <Col span="6">
                        <Form.Item label="内容编码" name="content_encoding">
                            <Input />
                        </Form.Item>
                    </Col>
                </Row>
                <Divider orientation="left">HTTP请求</Divider>
                <HTTPRequestHeaders />
                <Divider orientation="left">消息体数据</Divider>
                <Form.Item label="消息体" name="body">
                    <Input.TextArea maxLength={1000} showCount />
                </Form.Item>
            </>
        case "JARImport":
            return <Form.Item name="content">
                <Input.TextArea maxLength={1000} showCount />
            </Form.Item>
        case "WhileController":
            return <>
                <Divider orientation="left">循环继续条件</Divider>
                <Form.Item name="condition">
                    <Input.TextArea maxLength={1000} showCount />
                </Form.Item>
            </>
        case "LoopController":
            return <Form.Item label="循环次数" name="loop_count" rules={[{ type: "integer" }]}>
                <InputNumber className="InputNumber" min={0} />
            </Form.Item>
        case "ConstantTimer":
            return <Form.Item label="线程延迟（毫秒）" name="delay" rules={[{ type: "integer" }]}>
                <InputNumber className="InputNumber" min={0} />
            </Form.Item>
        case "JSR223PreProcessor":
        case "JSR223PostProcessor":
            return <>
                <Divider orientation="left">Groovy脚本</Divider>
                <Form.Item name="script">
                    <MonacoEditor height={600} language="python" />
                </Form.Item>
            </>
        case "ResponseAssertion":
            return <>
                <Divider orientation="left">测试字段</Divider>
                <Form.Item name="test_field">
                    <Radio.Group options={["响应文本","响应代码","响应消息","响应头","请求头","URL样本","文档(文本)","请求数据"]}/>
                </Form.Item>
                <Divider orientation="left">匹配模式</Divider>
                <Form.Item name="test_type">
                    <Radio.Group options={["包括","匹配","相等"]}/>
                </Form.Item>
                <Form.Item name="test_strings">
                    <Input.TextArea maxLength={1000} showCount/>
                </Form.Item>
            </>
        case "TestFunc":
            return <>
                <Form.Item name="method_name" label="方法名">
                    <Input />
                </Form.Item>
                <Divider orientation="left">方法内容</Divider>
                <Form.Item name="script">
                    <MonacoEditor height={600} language="python" />
                </Form.Item>
            </>
    }
    return null
}

function RootBasicScenario(props: FormItemLabelProps) {
    const [basic, setBasic] = useState(false)
    return <Form.Item {...props} className="RootBasicScenario" initialValue="by_time" name="basic">
        <Radio.Group onChange={function (e) {
            setBasic(e.target.value === "by_count")
        }}>
            <Radio value="by_time">测试时长</Radio>
            <div>
                <Form.Item label="小时" className="WithoutLabel" name="by_time_h" rules={[{ type: "integer" }]}>
                    <InputNumber disabled={basic} className="Time" min={0} />
                </Form.Item>
                <span className="Sep">:</span>
                <Form.Item label="分钟" className="WithoutLabel" name="by_time_m" rules={[{ type: "integer" }]}>
                    <InputNumber disabled={basic} className="Time" min={0} max={60} />
                </Form.Item>
                <span className="Sep">:</span>
                <Form.Item label="秒" className="WithoutLabel" name="by_time_s" rules={[{ type: "integer" }]}>
                    <InputNumber disabled={basic} className="Time" min={0} max={60} />
                </Form.Item>
                <span className="Format">HH:MM:SS</span>
            </div>
            <Radio value="by_count">测试次数</Radio>
            <div>
                <Form.Item label="次数" className="WithoutLabel" name="by_count" rules={[{ type: "integer" }]}>
                    <InputNumber disabled={!basic} className="Count" min={0} max={10000} addonAfter="最大值：10000" />
                </Form.Item>
            </div>
        </Radio.Group>
    </Form.Item>
}

function RootPresure() {
    const [disabled, setDisabled] = useState(true)
    return <>
        <Divider orientation="left">压力配置</Divider>
        <Form.Item name="is_increased" valuePropName="checked">
            <Checkbox onChange={function (e) { setDisabled(!e.target.checked) }}>压力递增</Checkbox>
        </Form.Item>
        <Form.Item labelCol={{ span: 3 }} labelAlign="left" label="初始数" name="init_value" rules={[{ type: "integer" }]}>
            <InputNumber disabled={disabled} className="InputNumber" min={0} />
        </Form.Item>
        <Form.Item labelCol={{ span: 3 }} labelAlign="left" label="增量" name="increment" rules={[{ type: "integer" }]} >
            <InputNumber disabled={disabled} className="InputNumber" min={0} />
        </Form.Item>
        <Form.Item labelCol={{ span: 3 }} labelAlign="left" label="初始等待时间" name="init_wait" rules={[{ type: "integer" }]}>
            <InputNumber disabled={disabled} className="InputNumber" min={0} addonAfter="MS" />
        </Form.Item>
        <Form.Item labelCol={{ span: 3 }} labelAlign="left" label="进程增长间隔" name="growth_interval" rules={[{ type: "integer" }]}>
            <InputNumber disabled={disabled} addonAfter="MS" min={0} className="InputNumber" />
        </Form.Item>
    </>
}

function HTTPRequestHeaders() {
    return <div className="HTTPRequestHeaders">
        <Form.List initialValue={[{}]} name="headers">{function (fields, { add, remove }) {
            return fields.map(function (item) {
                return <div key={item.name} className="FormList">
                    <Form.Item name={[item.name, "name"]} rules={[{ max: 32 }]}><Input /></Form.Item>
                    <span className="Equal">=</span>
                    <Form.Item name={[item.name, "value"]} rules={[{ max: 32 }]}><Input /></Form.Item>
                    <PlusCircleOutlined onClick={add} />
                    {item.key !== 0 && <MinusCircleOutlined onClick={function () { remove(item.name) }} />}
                </div>
            })
        }}</Form.List>
    </div>
}