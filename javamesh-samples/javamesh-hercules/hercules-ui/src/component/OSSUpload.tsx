import React from "react"
import { UploadOutlined } from '@ant-design/icons'
import { Button, Upload } from "antd"

export default function App(props: { max: number, value?: string, onChange?: (value: string) => void }) {
    return <Upload action={'/argus-emergency/api/file/resource'+window.location.search}
        defaultFileList={props.value?.split(" ").map(function (item) {
            const index = item.indexOf("/")
            const uid = item.slice(0, index)
            const name = item.slice(index + 1)
            return { uid, name, url: '/argus-emergency/api/file/resource/' + item }
        })}
        maxCount={props.max}
        onChange={function (info) {
            console.log(info)
            props.onChange?.(info.fileList.filter(function (item) { return item.status === "done" }).map(function (item) {
                return item.response.data.uid + "/" + item.name
            }).join(" "))
        }}>
        <Button icon={<UploadOutlined />}>上传文件</Button>
    </Upload>
}