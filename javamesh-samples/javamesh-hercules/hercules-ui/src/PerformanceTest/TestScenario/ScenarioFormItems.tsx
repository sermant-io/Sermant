import { Form, Input, Select } from "antd"
import React from "react"

export default function App() {
    return <>
        <Form.Item rules={[
            { max: 64, required: true, whitespace: true },
        ]} label="应用名" name="app_name">
            <Input minLength={1} placeholder="请输入" />
        </Form.Item>
        <Form.Item rules={[
            { max: 64, required: true, whitespace: true },
        ]} label="场景名称" name="scenario_name">
            <Input placeholder="请输入" />
        </Form.Item>
        <Form.Item rules={[
            {
                async validator(_, value?: string[]) {
                    if (!value) return
                    if (value.length > 4) {
                        throw new Error("标签不能多于4个")
                    }
                    if (value.find(function (item) {
                        return item.length > 16
                    })) {
                        throw new Error("单个标签不能长于16")
                    }
                }
            }
        ]} label="标签" name="label">
            <Select mode="tags" placeholder="请输入标签" />
        </Form.Item>
        <Form.Item label="描述" name="desc">
            <Input.TextArea showCount maxLength={256} rows={4} autoSize={{ minRows: 2, maxRows: 2 }} placeholder="请输入描述" />
        </Form.Item>
    </>
}