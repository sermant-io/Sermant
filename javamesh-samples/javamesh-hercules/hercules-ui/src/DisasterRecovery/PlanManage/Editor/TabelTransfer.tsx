import { Table, Transfer } from "antd"
import axios from "axios"
import { debounce } from "lodash"
import React, { useEffect, useRef, useState } from "react"
import "./TabelTransfer.scss"

type Data = { server_id: string }
export default function App(props: { onChange?: (value: string[]) => void }) {
    const [leftData, setLeftData] = useState<{ data: Data[], total: number }>({ data: [], total: 0 })
    const [rightData, setRightData] = useState<Data[]>([])
    const [loading, setLoading] = useState(false)
    const stateRef = useRef<any>({})
    async function load() {
        setLoading(true)
        const params = {
            pageSize: stateRef.current.pagination?.pageSize || 5,
            current: stateRef.current.pagination?.current,
            sorter: stateRef.current.sorter?.field,
            order: stateRef.current.sorter?.order,
            ...stateRef.current.search,
            ...stateRef.current.filters,
            excludes: stateRef.current.excludes
        }
        try {
            const res = await axios.get("/argus-emergency/api/host", { params })
            setLeftData(res.data)
        } catch (error: any) {

        }
        setLoading(false)
    }
    useEffect(function () {
        load()
    }, [])
    const debounceRef = useRef(debounce(load, 1000))
    return <Transfer className="TabelTransfer" showSearch showSelectAll={false}
        onSearch={function (_, server_name) {
            stateRef.current.search = { server_name }
            debounceRef.current()
        }}
        onChange={function (_, direction, moveKeys) {
            let rightDataNew = rightData
            if (direction === "right") {
                rightDataNew = rightDataNew.concat(
                    leftData.data.filter(function (item) {
                        return moveKeys.includes(item.server_id)
                    })
                )
            } else {
                rightDataNew = rightDataNew.filter(function (item) {
                    return !moveKeys.includes(item.server_id)
                })
            }
            setRightData(rightDataNew)
            const rightKeys = rightDataNew.map(function (item) { return item.server_id })
            props.onChange?.(rightKeys)
            stateRef.current.excludes = rightKeys
            load()
        }}
    >{function (props) {
        const columns = [
            { title: "主机名称", dataIndex: "server_name" },
            { title: "服务器IP", dataIndex: "server_ip" }
        ]
        if (props.direction === "left") {
            return <Table size="small" rowKey="server_id" dataSource={leftData.data} loading={loading}
                onChange={function (pagination, filters, sorter) {
                    stateRef.current = { ...stateRef.current, pagination, filters, sorter }
                    load()
                }}
                pagination={{ total: leftData.total, size: "small", pageSize: 5, showTotal() { return `共 ${leftData.total} 条` }, showSizeChanger: false }}
                rowSelection={{
                    hideSelectAll: true,
                    onSelect(record, selected) {
                        props.onItemSelect(record.server_id, selected)
                    }
                }}
                columns={columns}
            />
        } else {
            return <Table size="small" rowKey="server_id" dataSource={rightData}
                rowSelection={{
                    hideSelectAll: true,
                    onSelect(record, selected) {
                        props.onItemSelect(record.server_id, selected)
                    }
                }}
                columns={columns}
            />
        }
    }}</Transfer>
}


