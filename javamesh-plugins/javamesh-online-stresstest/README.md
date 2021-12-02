# javamesh-online-stresstest

## 概述

`javamesh-online-stresstest`是全链路压测实现模块

## 使用

业务需要准备stress.properties文件用于制定影子库同正式库的映射关系，并打包到resources中。

影子数据库配置示例，该值不能缺省：

key:db 标识配置影子数据库信息，为固定字符串。

value为json串，内容是正式库和影子库的映射关系。内容的key为host:port/dbname的格式，value是影子库的连接信息，用户名和密码同正式库保持一致。

db={"100.100.153.226:3306/stress_test":{"url":"jdbc:mysql://100.100.153.226:3306/_shadow_stress_test?allowMultiQueries=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT"}}

影子redis配置示例, 该值缺省时表示影子数据在同一个redis中，使用shadow_前缀区分：

key:redis.repository 标识配置影子redis信息，为固定字符串。

value为json串，内容是影子redis的地址。内容为json串，默认地址在address中，使用队列格式，如果时master slave模式，需要加上"slave":[]。

redis.repository={"address":["redis://100.100.153.226:6380"]}

影子mogodb配置示例，该值缺省时表示影子数据在同一个mongo database中，使用shadow_前缀的collection区分：

key:mongo.repository 标识配置影子mongo信息，为固定字符串。

value为json串，内容是影子mongodb同原始库的映射信息。内容为json串，key时原始的mongodb数据库名，值是影子mongodb库的信息。

mongo.repository={'stress_test':{'host':'localhost:27017','database':'shadow_stress_test','user_suffix':''}}
