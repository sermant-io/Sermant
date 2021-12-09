# online-stresstest

本文档主要介绍[影子库压测插件](../../../javamesh-plugins/javamesh-online-stresstest)以及该插件的使用方法

## 功能
影子库插件通过拦截流量，识别和传递压测标记，实现将压测流量转发到影子库的目的。

- **压测流量识别**：服务在接收到消息时，需要一个特定的字段来区别于正常的用户流量。比如当HttpServlet接收到一个http请求时，会检查http header，如果header中包含`x-test`字段，且值为`true`时，表示该请求为压测请求，需要将处理该请求的线程设置为压测线程。同理，kafka消息或者其他第三方消息也需要包含类似的压测标记来识别压测流量。
- **压测标记传递**：当服务再调用第三方时，需要具备跟进压测标记寻址到不同的目标对象或者将压测标记传递下去的能力。比如当服务需要通过HttpClient调用第三方服务时，会检查自己是不是压测线程，然后决定再下发请求时是否需要带上压测标记；在操作数据库时，需要检查自己是不是压测线程，然后决定是否寻址到影子库。

## 使用说明
业务首先需要明确自己的整体架构，使用了哪些中间件，然后根据实际情况选择影子库策略，并创建对应的影子环境。

在业务的resources目录下创建`stress.properties`文件，并打包到业务可执行包中，该文件主要包含正式库同影子库的映射关系。如下为文件中需要包含的各种值。
- **数据库**
```
db={"100.100.153.226:3306/stress_test":{"url":"jdbc:mysql://100.100.153.226:3306/_shadow_stress_test?allowMultiQueries=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT"}}
```
其中db为固定的关键字，表示当前正在配置影子库的映射关系。比如`100.100.153.226:3306/stress_test`为当前系统的正式库地址，对应的值是该正式库对应的影子库地址，格式为json。
`url`为影子库的地址信息，影子库和正式库需要使用相同用户名和密码。
- **MongoDB**
```
mongo.repository={'stress_test':{'host':'localhost:27017','database':'shadow_stress_test'}}
```
其中mongo.repository为固定的关键字，表示当前正在配置影子库的映射关系。比如`stress_test`为当前系统的正式mongodb库地址，对应的值为该正式库对应的影子库地址，格式为json。
`host`为影子库的地址和接口信息，`database`为影子库的database信息，影子库和正式库需要使用相同的用户名和密码。

**注意**：如果没有配置`mongo.repository`，则mongodb的影子环境会直接使用当前的数据库，直接创建影子collection。
- **redis**
```
redis.repository={"address":["redis://100.100.153.226:6380"]}
```
其中redis.repository为固定的关键字，表示影子redis的地址，格式为json。影子redis地址保存在address中，使用[]格式，如果时master slave模式，需要加上"slave":[]。
**注意**：如果没有配置`redis.repository`，则redis的影子环境会直接使用当前的redis，在key前加前缀，使用shadow_key。

[返回**Java-mesh**说明文档](../../README.md)
