import { Button, Drawer, Form, Input, message, Select, Switch } from "antd";
import axios from "axios";
import React, { useState } from "react";
import MonacoEditor from "react-monaco-editor";
import "./AddPlanTask.scss"
import SearchSelect from "./SearchSelect";
import TabelTransfer from "./TabelTransfer";

export default function App(props: { children: React.ReactNode, onFinish: (values: any) => void }) {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [script, setScript] = useState("")
  const [form] = Form.useForm();
  return <>
    <Button type="link" size="small" onClick={function () { setIsModalVisible(true) }}>{props.children}</Button>
    <Drawer className="AddPlanTask" title={props.children} width={950} visible={isModalVisible} maskClosable={false} footer={null} onClose={function () {
      setIsModalVisible(false)
    }}>
      <Form form={form} requiredMark={false} labelCol={{ span: 4 }} initialValues={{ channel_type: "SSH" }} onFinish={async (values) => {
        try {
          values.sync === false ? values.sync = "异步" : values.sync = "同步"
          // 获取key
          const res = await axios.post("/argus-emergency/api/plan/task", values)
          props.onFinish({ ...values, ...res.data.data, title: values.task_name })
          form.resetFields()
          setScript("")
          setIsModalVisible(false)
        } catch (error: any) {
          message.error(error.message)
        }
      }}>
        <Form.Item labelCol={{ span: 2 }} label="名称" name="task_name" rules={[{ required: true, max: 64 }]}><Input /></Form.Item>
        <div className="Line">
          <Form.Item className="Middle" label="通道类型" name="channel_type">
            <Select options={[{ value: "SSH" }, { value: "API" }]} />
          </Form.Item>
          <Form.Item className="Middle" label="执行方式" name="sync" valuePropName="checked">
            <Switch checkedChildren="同步" unCheckedChildren="异步" defaultChecked />
          </Form.Item>
        </div>
        <Form.Item labelCol={{ span: 2 }} label="执行主机" name="service_id">
          <TabelTransfer />
        </Form.Item>
        <Form.Item className="ScriptName" labelCol={{ span: 2 }} label="脚本名称" name="script_name">
          <SearchSelect onChange={async function (name) {
            setScript("")
            form.setFieldsValue({ submit_info: "" })
            try {
              const res = await axios.get("/argus-emergency/api/script/getByName", { params: { name, status: "approved" } })
              setScript(res.data.data.content)
              form.setFieldsValue({ submit_info: res.data.data.submit_info })
            } catch (error) {

            }
          }} />
        </Form.Item>
        <Form.Item name="submit_info" labelCol={{ span: 2 }} label="脚本用途"><Input.TextArea disabled /></Form.Item>
        <div className="Editor">
          <MonacoEditor height="150" language="shell" options={{ readOnly: true }} value={script} />
        </div>
        <Form.Item className="Buttons">
          <Button type="primary" htmlType="submit">创建</Button>
          <Button onClick={function () {
            setIsModalVisible(false)
          }}>取消</Button>
        </Form.Item>
      </Form>
    </Drawer>
  </>
}