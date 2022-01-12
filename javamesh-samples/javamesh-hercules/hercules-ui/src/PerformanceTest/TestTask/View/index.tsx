import { Button, Descriptions, Input, Radio, Table, Tabs, Tag } from "antd";
import { PresetColorTypes } from "antd/lib/_util/colors";
import React from "react";
import { Link } from "react-router-dom";
import Breadcrumb from "../../../component/Breadcrumb";
import Card from "../../../component/Card";
import "./index.scss"

export default function App() {
    return <div className="TaskView">
        <Breadcrumb label="压测任务" sub={{ label: "实时TPS数据", parentUrl: "/PerformanceTest/TestTask" }} />
        <Card>
            <div className="Label">基本信息</div>
            <div className="SubCard Info">
                <Descriptions>
                    <Descriptions.Item label={
                        <div className="Title">测试名称</div>
                    }>测试名称</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">压测状态</div>
                    }>压测状态</Descriptions.Item>
                    <Descriptions.Item label={
                        <div className="Title">标签</div>
                    }>{[].map(function (item: string, index: number) {
                        return <Tag key={index} color={PresetColorTypes[index + 5 % 13]}>{item}</Tag>
                    })}</Descriptions.Item>
                    <Descriptions.Item span={2} label={
                        <div className="Title">描述</div>
                    }>描述</Descriptions.Item>
                </Descriptions>
                <Button type="primary">
                    <Link to={"/PerformanceTest/TestReport/Detail?test_id=" + 123}>详细报告</Link>
                </Button>
            </div>
            <div className="SubCard Basic">
                <div className="Item">
                    <div className="Value">运行时间</div>
                    <div className="Title">运行时间</div>
                </div>
                <div className="Item">
                    <div className="Value">虚拟用户数</div>
                    <div className="Title">虚拟用户数</div>
                </div>
                <div className="Item">
                    <div className="Value">TPS</div>
                    <div className="Title">TPS</div>
                </div>
                <div className="Item">
                    <div className="Value">TPS峰值</div>
                    <div className="Title">TPS峰值</div>
                </div>
                <div className="Item">
                    <div className="Value">平均时间（ms）</div>
                    <div className="Title">平均时间（ms）</div>
                </div>
                <div className="Item">
                    <div className="Value">执行测试数量</div>
                    <div className="Title">执行测试数量</div>
                </div>
                <div className="Item">
                    <div className="Value">测试成功数量</div>
                    <div className="Title">测试成功数量</div>
                </div>
                <div className="Item">
                    <div className="Value">错误</div>
                    <div className="Title">错误</div>
                </div>
            </div>
            <div className="Label">TPS图表</div>
            <Tabs type="card" size="small">
                <Tabs.TabPane tab="业务性能指标" key="1">
                    <Table columns={[
                        { title: "事务名称" },
                        { title: "TPS" },
                        { title: "响应时间(ms)" },
                        { title: "成功数" },
                        { title: "失败数" },
                        { title: "失败率%" }
                    ]} />
                </Tabs.TabPane>
                <Tabs.TabPane tab="硬件资源指标" key="2">
                    <Radio.Group options={["全部", "192.168.0.1"]} />
                </Tabs.TabPane>
                <Tabs.TabPane tab="JVM性能指标" key="3">
                    Content of Tab Pane 2
                </Tabs.TabPane>
            </Tabs>
            <div className="Label">测试注释</div>
            <div className="Comment">
                <Input.TextArea className="Input" showCount maxLength={256} autoSize={{ minRows: 2, maxRows: 2 }} placeholder="请输入描述" />
                <div className="Button">添加注释</div>
            </div>
            <div className="Label">日志文件</div>
            {[].map(function (item: string, index: number) {
                return <div key={index} >
                    <a href={process.env.PUBLIC_URL + `/api/task/download?test_id=${123}&log_name=${item}`} target="_blank" rel="noreferrer">{item}</a>
                </div>
            })}
            <div className="Label">执行日志</div>
            {[].map(function (item: string, index: number) {
                return <div key={index}>{item}</div>
            })}
        </Card>
    </div>
}