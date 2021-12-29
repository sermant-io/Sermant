import { Button, Form, Input, message, Modal, Select, Spin, Table } from "antd"
import axios from "axios"
import { debounce } from "lodash"
import React, { useEffect, useRef, useState } from "react"
import { FileSearchOutlined, SearchOutlined } from '@ant-design/icons'

export default function App(props: { value?: string, onChange?: (value: string) => void, placeholder?: string, allowClear?: boolean }) {
  const [options, setOptions] = useState()
  const [loading, setLoading] = useState(false)
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [value, setValue] = useState(props.value)
  function updateValue(value: string) {
    props.onChange?.(value)
    setValue(value)
  }

  async function loadBelongTo(value?: string) {
    setLoading(true)
    try {
      const res = await axios.get("/argus-emergency/api/script/search", { params: { value, status: "approved" } })
      setOptions(res.data.data.map(function (item: string) {
        return { value: item }
      }))
    } catch (error: any) {

    }
    setLoading(false)
  }
  const debounceRef = useRef(debounce(loadBelongTo, 1000))
  return <div className="SearchSelect">
    <Select placeholder={props.placeholder} value={value} onChange={updateValue} options={options}
      allowClear={props.allowClear} showSearch
      onSearch={debounceRef.current}
      onFocus={function () {
        options || loadBelongTo()
      }}
      notFoundContent={loading && <Spin size="small" />}
    />
    <Button icon={<FileSearchOutlined />} onClick={function () { setIsModalVisible(true) }}>查找</Button>
    {isModalVisible && <SearchSelectModal setIsModalVisible={setIsModalVisible} onChange={updateValue} />}
  </div>
}

function SearchSelectModal(props: { setIsModalVisible: (visible: boolean) => void, onChange: (value: string) => void }) {
  const [data, setData] = useState({ data: [], total: 0 })
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
  const [loading, setLoading] = useState(false)
  const stateRef = useRef<any>({})
  async function load() {
    setLoading(true)
    try {
      const params = {
        pageSize: stateRef.current.pagination?.pageSize || 5,
        current: stateRef.current.pagination?.current,
        sorter: stateRef.current.sorter?.field,
        order: stateRef.current.sorter?.order,
        ...stateRef.current.search,
        ...stateRef.current.filters,
        status: "approved"
      }
      const res = await axios.get("/argus-emergency/api/script", { params })
      setData(res.data)
    } catch (error: any) {

    }
    setLoading(false)
  }
  useEffect(function () {
    load()
  }, [])
  return <Modal className="SearchSelectModal" title="选择脚本" width={750} visible={true} onCancel={function () { props.setIsModalVisible(false) }}
    onOk={function () {
      if (!selectedRowKeys.length) {
        message.warning("请选择一条记录")
        return
      }
      props.onChange(selectedRowKeys[0].toString())
      props.setIsModalVisible(false)
    }}
  >
    <Form className="Search" layout="inline" onFinish={function (values) {
      stateRef.current.search = values
      load()
    }}>
      <Form.Item name="script_name">
        <Input placeholder="脚本名称" />
      </Form.Item>
      <Button htmlType="submit" icon={<SearchOutlined />}>查找</Button>
    </Form>
    <Table dataSource={data.data} size="middle" loading={loading} pagination={{ total: data.total, pageSize: 5 }} rowKey="script_name"
      rowSelection={{
        type: "radio", selectedRowKeys, onChange(selectedRowKeys) {
          setSelectedRowKeys(selectedRowKeys)
        }
      }}
      onChange={function (pagination, filters, sorter) {
        stateRef.current = { ...stateRef.current, pagination, filters, sorter }
        load()
      }}
      columns={[
        { title: "脚本名称", dataIndex: "script_name", ellipsis: true },
        { title: "脚本用途", dataIndex: "submit_info", ellipsis: true },
      ]}
    />
  </Modal>
}