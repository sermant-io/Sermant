export const resultCodeMap = new Map<string, string>([
    ['00', '成功'],
    ['01', '无法连接配置中心'],
    ['02', '配置查询失败'],
    ['03', '配置已存在'],
    ['04', '新增配置失败'],
    ['05', '发布配置失败'],
    ['06', '删除配置失败'],
    ['07', '配置不存在'],
    ['08', '缺少请求参数'],
    ['09', '请求失败'],
]);

export const options = [
    {label: '路由插件配置', value: 'router',},
    {label: 'springboot注册插件配置', value: 'springboot-registry',},
    {label: '注册迁移插件配置', value: 'service-registry',},
    {label: '流控插件配置', value: 'flowcontrol',},
    {label: '离群实例摘除插件配置', value: 'removal',},
    {label: '负载均衡插件配置', value: 'loadbalancer',},
    {label: '标签透传插件配置', value: 'tag-transmission',},
    {label: '消息队列禁止消费', value: 'mq-consume-prohibition',},
    {label: '数据库禁写插件配置', value: 'database-write-prohibition',},
    {label: '其他配置', value: 'other',},
]