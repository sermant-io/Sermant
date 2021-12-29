import { Button, Upload } from "antd"
import { RcFile } from "antd/lib/upload"
import React, { useState } from "react"
import {UploadOutlined} from '@ant-design/icons'

export default function App(props: {max: number,onChange?: (value: RcFile[]) => void}) {
    const [fileList, setFileList] = useState<RcFile[]>([])
    return <Upload data={fileList} maxCount={props.max} onChange={function(info){
        props.onChange?.(fileList)
    }}
    beforeUpload={function (file) {
        if (props.max === 1) {
            setFileList([file])
        } else if (fileList.length < props.max) {
            setFileList([...fileList, file])
        }
        return false
    }}
    onRemove={function (file) {
        const index = fileList.indexOf(file.originFileObj!!)
        fileList.splice(index, 1);
        setFileList(fileList)
    }}>
        <Button icon={<UploadOutlined />}>上传文件</Button>
    </Upload>
}