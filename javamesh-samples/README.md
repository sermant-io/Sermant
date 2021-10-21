# javamesh-samples

## 概述

`javamesh-samples`是`JavaMesh`的样品模块，内含各种功能的插件及其附加件

## 内容列表

- [概述](#概述)
- [背景](#背景)
- [组成部分](#组成部分)
- [开发流程](#开发流程)
- [打包流程](#打包流程)

## 背景

`javamesh-samples`模块的前身是`plugins`模块，原只用于存放自定义插件，考虑到部分插件可能需要将数据传递到服务器进行处理，因此对`plugins`模块进行整理，现在的`javamesh-samples`模块现在可以存放自定义插件和相应的附加件。

## 组成部分

`javamesh-samples`按照`功能(function)`划分子模块，每个功能子模块中可能包含以下子模块：

- `插件(plugin)`，通常包含对宿主应用的增强逻辑
- `后端(server)`，用于接收插件数据的服务端
- `前端(webapp)`，用于对服务端数据作前端展示
- `其他(other)`特殊附加件

目前`javamesh-samples`包含以下功能：

- [javamesh-example(function)](javamesh-example): 示例功能
  - [demo-plugin(plugin)](javamesh-example/demo-plugin): 示例插件
  - [demo-application(other)](javamesh-example/demo-application): 示例宿主应用，不参与打包
- [javamesh-flowcontrol(function)](javamesh-flowcontrol): 流控功能
  - [flowcontrol-plugin(plugin)](javamesh-flowcontrol/flowcontrol-plugin): 流控插件
  - [flowcontrol-server(server)](javamesh-flowcontrol/flowcontrol-server): 流控插件的后端
- [javamesh-flowrecord(function)](javamesh-flowrecord): 流量录制回放功能
  - [flowrecord-plugin(plugin)](javamesh-flowrecord/flowrecord-plugin): 流量录制回放插件
- [javamesh-server-monitor(function)](javamesh-server-monitor): 服务监控功能
  - [server-monitor-plugin(plugin)](javamesh-server-monitor/server-monitor-plugin): 服务监控插件

## 开发流程

- 添加`功能(function)`模块，依据该`功能(function)`中包含的内容，在[javamesh-samples的pom文件](pom.xml)中添加相应模块：
  - 在`id`为`all`的`profile`中添加该模块。
  - 如果该`功能(function)`包含`插件(plugin)`，那么需要在`id`为`plugin`的`profile`中添加该模块。
  - 如果该`功能(function)`包含其他内容，则需要在`id`为`ext`的`profile`中添加该模块。
  - 在`功能(function)`的`pom.xml`中添加以下标签：
  ```xml
  <packaging>pom</packaging>
  ```
  ```xml
  <properties>
    <javamesh.basedir>${pom.basedir}/../../..</javamesh.basedir>
    <package.sample.name>${功能名称}</package.sample.name>
  </properties>
  ```
- 在`功能(function)`模块下添加`插件(plugin)`子模块(如果需要)：
  - 为`功能(function)`模块的`pom.xml`添加以下`profile`：
  ```xml
  <profiles>
    <profile>
      <id>plugin</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${插件模块名}</module>
      </modules>
    </profile>
    <profile>
      <id>all</id>
      <modules>
        <module>${插件模块名}</module>
      </modules>
    </profile>
  </profiles>
  ```
  - 为`插件(plugin)`子模块添加以下参数：
  ```xml
  <properties>
    <package.sample.dir>${package.agent.dir}/plugins</package.sample.dir>
  </properties>
  ```
- 在`功能(function)`模块下添加`后端(server)`、`前端(webapp)`和`其他(other)`子模块(如果需要)：
  - 为`功能(function)`模块的`pom.xml`添加以下`profile`：
  ```xml
  <profiles>
    <profile>
      <id>ext</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <modules>
        <module>${后端模块名(如果有)}</module>
        <module>${前端模块名(如果有)}</module>
        <module>${其他模块名(如果有)}</module>
      </modules>
    </profile>
    <profile>
      <id>all</id>
      <modules>
        <module>${后端模块名(如果有)}</module>
        <module>${前端模块名(如果有)}</module>
        <module>${其他模块名(如果有)}</module>
      </modules>
    </profile>
  </profiles>
  ```
  - 为`后端(server)`子模块添加以下参数(如果需要)：
  ```xml
  <properties>
    <package.sample.dir>${package.server.dir}</package.sample.dir>
  </properties>
  ```
  - 为`前端(webapp)`子模块添加以下参数(如果需要)：
  ```xml
  <properties>
    <package.sample.dir>${package.webapp.dir}</package.sample.dir>
  </properties>
  ```

## 打包流程

目前[JavaMesh](../pom.xml)的打包过程中，包含`prepare`、`agent`、`plugin`、`ext`、`example`、`package`和`all`
7个步骤，其中与[javamesh-samples](pom.xml)相关的步骤如下：

- `plugin`: 对所有`插件(plugin)`进行打包，他们将输出到产品`agent/plugins/${功能名称}`目录。
- `ext`: 对所有附加件进行打包，包括`后端(server)`、`前端(webapp)`和`其他(other)`，其中`后端(server)`将输出到产品的`server/${功能名称}`目录，`前端(webapp)`将输出到产品的`webapp/${功能名称}`目录，`其他(other)`没有特殊的打包要求。
- `example`: 对示例功能[javamesh-example](javamesh-example)进行打包。
- `all`: 对上述的所有内容进行打包。

需要注意的是：

- `插件(plugin)`的产品输出到整个产品的`agent/plugins/${功能名称}`目录
  - `插件(plugin)`涉及到`byte-buddy`包的使用，通常需要使用`maven-shade-plugin`插件作包名修正，添加如下标签即可：
  ```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
  ```
  - 如果需要在`maven-shade-plugin`插件中增加其他修正规则，至少需要将`byte-buddy`包的规则保留。
- `后端(server)`的产品输出到整个产品的`server/${功能名称}`目录，打包方式、输出内容由该模块自定义。
- `前端(webapp)`的产品输出到整个产品的`webapp/${功能名称}`目录，打包方式、输出内容由该模块自定义。
