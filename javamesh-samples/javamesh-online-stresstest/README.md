# javamesh-online-stresstest

## 概述

`javamesh-online-stresstest`是全链路压测实现模块

## 使用

业务需要准备stress.properties文件用于制定影子库同正式库的映射关系，并打包到resources中。

示例：
key:db 标识配置影子数据库信息，为固定字符串。

value为json串，内容是正式库和影子库的映射关系。内容的key为host:port/dbname的格式，value是影子库的连接信息，用户名和密码同正式库保持一致。

db={"100.100.153.226:3306/stress_test":{"url":"jdbc:mysql://100.100.153.226:3306/_shadow_stress_test?allowMultiQueries=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT"}}
