# 第三方版权说明手册

本文档针对开发过程中涉及的**第三方**源码或二进制包的版权作相关说明。

## 源码引用

如果在代码中，存在以下情况，视为对**第三方**源码的 **引用**：

- 整体拷贝：直接拷贝**第三方**源码中的文件，在其基础上进行修改。
- 局部拷贝：拷贝**第三方**源码中部分方法或内部类，将其用于自研代码中。
- 设计参考：开发者在进行架构设计时，若参考**第三方**的架构，且两种架构中存在定位相同的内容，也视为 **引用**。

以上三种情况中，需要开发者对涉及的文件做如下操作：

- 在`LICENSE`文件中添加对**第三方**源码的拷贝说明，形如：
  ```txt
  The following files contain a portion of ${THIRD PARTY PROJECT NAME} project.

  ${RELATED FILE A} in this product is copied from ${THIRD PARTY FILE A} of ${THIRD PARTY PROJECT NAME} project.

  ${RELATED FILE B} in this product is copied from ${THIRD PARTY FILE B} of ${THIRD PARTY PROJECT NAME} project.

  ...

  ${THIRD PARTY PROJECT NAME} project is published at ${THIRD PARTY PROJECT CODEBASES URL} and its license is ${THIRD PARTY PROJECT LICENSE NAME}.
  ```
  其中：
  - `THIRD PARTY PROJECT NAME`表示**第三方**工程的名称。
  - `RELATED FILE`为本项目的**涉事文件**：为类时，键入全限定名路径；否则，键入项目相对路径。
  - `THIRD PARTY FILE`表示**第三方**的**被拷贝文件**：为类时，键入全限定名路径；否则，键入项目相对路径；如果**第三方**项目是单模块项目，也可键入source目录相对路径。
  - `THIRD PARTY PROJECT CODEBASES URL`表示**第三方**工程源码仓的地址；如果找不到源码仓地址，可以改为官方网站地址或源码下载地址，总之原则就是要做到可溯源。
  - `THIRD PARTY PROJECT LICENSE NAME`表示**第三方**工程的`LICENSE`名称，通常可以参考其`pom`文件中的`licenses`标签，如果存在多个`LICENSE`，则改为复数形式：
    ```txt
    ...
    and its licenses are ${LICENSE A}, ${LICENSE B}, ..., and ${LICENSE C}.
    ```
  - 如果已经存在目标**第三方**工程的条目，则掐头去尾将中间的拷贝信息填入即可。
- 在**涉事文件**中键入**被拷贝文件**的头信息(如果有)，并添加拷贝源信息，形如：
  ```txt
  Based on ${THIRD PARTY FILE} from the ${THIRD PARTY PROJECT NAME} project.
  ```
- 如果**第三方**工程中含有`NOTICE`文件，则将其追加到本工程的`NOTICE`文件结尾。如果已经包含，无需重复追加多次。

## 带依赖jar包

如果开发者：

- 没有修改`resources`标签的内容
- 开发的模块中`sermant.basedir`参数正确指向顶层目录
- 不打带依赖`jar`包，或使用`shade`插件打带依赖`jar`包，且没有修改`transformers`标签

那么无需对输出的`jar`包作任何调整，否则请详细阅读下面的说明，并按实际情况处理。

在默认打包过程中，需要将本工程默认的`LICENSE`文件和`NOTICE`文件打入。这两个文件存放于`sermant-package`模块的`resources/META-INF`目录下，由`resources`标签特别地指向。

一般情况下，只要保证打包的模块(`packaging`标签的值不为`pom`)中`sermant.basedir`参数指向本工程的顶层目录，就会默认添加这两个文件，无需特别关心。

在打包流程中，使用`shade`插件、`assembly`插件或`spring`打包插件打带依赖`jar`包时，如果打入的**第三方**`jar`包中含有`NOTICE`文件，最好将他们与本工程默认的`NOTICE`文件合并。`shade`插件的`ApacheNoticeResourceTransformer`正好可以做到这一点。这一点在顶层工程的`pom`文件中有配置，除非有修改`Transformer`的需要，否则不建议覆盖顶层工程的`shade`插件设置。

注意：本节所提到的默认的`LICENSE`文件和`NOTICE`文件，指的是仅包含本工程相关信息的文件。在项目顶层目录中存放的`LICENSE`文件和`NOTICE`文件，系整理过源码拷贝信息后的文件，包含本工程相关信息和被拷贝的**第三方**工程信息。

## RELEASE产品包

`RELEASE`产品包中需要将工程源码的`LICENSE`文件和`NOTICE`文件带上，前者还需要添加`RELEASE`产品包中所有涉及的**第三方**`jar`包的`LICENSE`信息。`RELEASE`产品包还需要将与本工程`LICENSE`不同的**第三方**`LICENSE`存放于`licenses`目录中带上，该目录存放于`sermant-package`模块的`resources`目录中。

综上，`RELEASE`产品包内部结构形如：
- `agent`目录: 核心增强逻辑
- `server`目录: 增强逻辑配套的服务端
- `licenses`目录: 与本工程`LICENSE`不同的**第三方开源依赖项目**`LICENSE`存放目录
- `LICENSE`文件: 本工程`LICENSE`文件，附加拷贝**第三方开源**源码的LICENSE声明，以及`RELEASE`产品包中涉及的所有**第三方开源依赖项目**`jar`包的`LICENSE`说明
- `NOTICE`文件: 本工程`NOTICE`文件，追加拷贝**第三方开源**源码的`NOTICE`文件。

本工程通过以下方式生成`RELEASE`产品包的`LICENSE`文件、`NOTICE`文件和`licenses`目录：
- 通过`license-maven-plugin`插件生成项目中所有涉及的第三方`jar`包的`LICENSE`信息：
  ```shell
  mvn license:aggregate-add-third-party
  ```
  生成的文件`LICENSE-binary-suffix.txt`存放于`sermant-package`模块的`resources`目录。该过程初次执行时间较久，请耐心等待。
- 工程各组件打包，输出到临时目录下。
- 在`sermant-package`模块打包时：
  - 将工程源码的`LICENSE`文件、`NOTICE`文件和`licenses`目录拷贝到临时目录中。
  - 调用脚本将`LICENSE-binary-suffix.txt`文件追加到临时目录的`LICENSE`文件。
  - 将临时目录压缩为`RELEASE`产品包。

综上，开发者可以通过以下命令编译并发布`RELEASE`产品包：
```shell
mvn license:aggregate-add-third-party clean package -Dmaven.test.skip
```

[返回**Sermant**说明文档](../README.md)
