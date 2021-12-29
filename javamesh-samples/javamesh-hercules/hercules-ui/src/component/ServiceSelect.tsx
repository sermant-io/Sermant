import { Select, Spin } from "antd"
import React, { useRef } from "react"
import axios from 'axios';
import { useState } from 'react';
import { debounce } from 'lodash';

export default function App(props: { url: string, value?: string, onChange?: (value: string) => void, placeholder?: string, allowClear?: boolean }) {
    const [options, setOptions] = useState()
    const [loading, setLoading] = useState(false)
    async function loadBelongTo(value?: string) {
        setLoading(true)
        try {
            const res = await axios.get(props.url, { params: { value } })
            setOptions(res.data.data.map(function (item: string) {
                return { value: item }
            }))
        } catch (error: any) {
            
        }
        setLoading(false)
    }
    const debounceRef = useRef(debounce(loadBelongTo, 1000))
    return <Select placeholder={props.placeholder} defaultValue={props.value} onChange={props.onChange} options={options}
        allowClear={props.allowClear} showSearch
        onSearch={debounceRef.current}
        onFocus={function () {
            options || loadBelongTo()
        }}
        notFoundContent={loading && <Spin size="small" />}
    />
}