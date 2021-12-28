import { Form, Input, Radio } from "antd"
import React, { useState } from "react"

export default function App() {
    const [commentVisiable, setCommonVisiable] = useState(false)
    return <>
        <Form.Item className="Middle" name="approve" label="审核" rules={[{ required: true }]}>
            <Radio.Group options={["通过", "驳回"]} onChange={function(e){
                setCommonVisiable(e.target.value === "驳回")
            }}/>
        </Form.Item>
        {commentVisiable && <Form.Item className="Middle" name="comment" label="备注" rules={[{ max: 32 }]}>
            <Input />
        </Form.Item>}
    </>
}