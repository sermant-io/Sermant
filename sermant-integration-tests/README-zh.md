# 自动化测试手册

[简体中文](README-zh.md) | [English](README.md)

本文档主要介绍自动化测试，以及如何编写自动化测试。

## 组成部分

```yaml
├─.github   	
│  ├─actions                            #存放所有测试场景
│  │  ├─common                          #存放测试场景公共目录
│  │  │  ├─dubbo                        #dubbo测试场景公共步骤
│  │  │  ├─entry                        #action公共入口步骤
│  │  │  ├─exit                         #action公共的出口步骤
│  │  │  ├─plugin-change-check          #action公共目录变更检查步骤
│  │  │  └─spring                       #spring测试场景公共步骤
│  │  └─scenarios                       #定义具体的测试场景
│  │      ├─dubbo                       #dubbo测试场景
│  │      │  ├─dubbo-common             #dubbo公共测试场景
│  │      │  └─router                   #dubbo路由测试场景
│  │      └─spring                      #spring测试场景
│  │          ├─dynamic-config-nacos    #spring动态配置-nacos测试场景
│  │          ├─dynamic-config-zk       #spring动态配置-zk测试场景
│  │          ├─graceful                #spring优雅上下线测试场景
│  │          ├─router                  #spring标签路由
│  │          │  ├─spring-router        #spring标签路由测试
│  │          │  └─spring-tag-router    #spring标签路由测试-springboot场景
│  │          └─spring-common           #spring公共测试场景
│  ├─ISSUE_TEMPLATE
│  └─workflows                          #自动化测试工作流入口
└─sermant-integration-tests             #存放自动化测试demo
    ├─dubbo-test                        #dubbo自动化测试demo
    │  ├─dubbo-2-6-integration-consumer
    │  ├─dubbo-2-6-integration-controller
    │  ├─dubbo-2-6-integration-provider
    │  ├─dubbo-2-7-integration-consumer
    │  ├─dubbo-2-7-integration-controller
    │  ├─dubbo-2-7-integration-provider
    │  ├─dubbo-integration-api
    │  └─dubbo-integration-test
    ├─scripts                           #自动化测试脚本存放处
    └─spring-test                       #spring自动化测试demo
        ├─spring-common					
        ├─spring-common-demos           #公共demo
        │  ├─spring-common-feign        #公共feign demo， 2.x
        │  ├─spring-common-feign-1.5.x  #公共feign demo, 1.5.x
        │  ├─spring-common-gateway      #公共网关demo-gateway
        │  ├─spring-common-resttemplate #公共Resttemplate测试demo
        │  └─spring-common-zuul         #公共网关demo-zuul
        ├─spring-intergration-test      #自动化测试, 即Junit编写
        ├─spring-nacos-config           #nacos动态配置demo
        └─spring-zookeeper-config       #zk动态配置demo
```

如上为当前自动化测试的目录结构，主要由以下三部分组成:

- workflow（工作流入口）, 此为自动化测试入口，相关测试需要在工作流添加要执行的action（测试场景）
- actions，此处定义具体的端到端的测试步骤
- sermant-integration-tests， 该部分主要存放action需要的测试demo，以及编写需要测试逻辑的junit用例

## 自动化测试入口

我们需要在workflow中进行定义自动化测试入口，当前workflow存在以Dubbo与Spring框架为主体的测试入口，入口文件如下：

- `dubbo_integration_test.yml`, Dubbo的测试场景入口
- `spring_integration_test_1`, `spring_integration_test_2`, Spring的测试场景入口，其中后者主要用于测试标签路由能力，而前者测试其他插件的能力, 包含流控，注册，优雅上下线，负载均衡等。此处之所以拆分开，主要考虑到需要充分利用到github工作流的并发测试，提升测试效率。

## 自动化测试设计

### 自动化测试入口设计

当前自动化测试基于测试框架的版本矩阵进行驱动，依赖于github action的[矩阵](https://docs.github.com/cn/actions/using-jobs/using-a-matrix-for-your-jobs),  在工作流的入口我们会设定版本矩阵，根据指定的矩阵，遍历测试所有配置在入口的action（具体的测试场景），达到测试的目的。

```yaml
test-for-spring:
    name: Test for spring
    runs-on: ubuntu-latest
    needs: [build-agent-and-cache, download-midwares-and-cache]
    strategy:
      matrix:
        include:
          - springBootVersion: "1.5.0.RELEASE"
            springCloudVersion: "Edgware.SR2"
            nacosVersion: "1.5.0.RELEASE"
          - springBootVersion: "2.0.2.RELEASE"
            springCloudVersion: "Finchley.RELEASE"
            nacosVersion: "2.0.0.RELEASE"
      fail-fast: false
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 100
      - name: common operations
        uses: ./.github/actions/common/spring
      - name: (graceful) test for springboot=${{ matrix.springBootVersion }} springCloudVersion=${{ matrix.springCloudVersion }}
        if: env.enableGraceful == 'true'
        uses: ./.github/actions/scenarios/spring/graceful
      - name: (zk dynamic config) test for springboot=${{ matrix.springBootVersion }} springCloudVersion=${{ matrix.springCloudVersion }}
        if: env.enableDynamicConfig == 'true'
        uses: ./.github/actions/scenarios/spring/dynamic-config-zk
```

如上，我们分别定义了两个矩阵版本，以`springBootVersion`为例，存在版本`1.5.0.RELEASE`与`2.0.2.RELEASE`, 实际测试时将同时(如果并发未限制)测试两版本的actions(即配置的`graceful`与`zk dynamic config`)，各自执行互不影响。

其中使用的action路径将由配置`uses`指定目录。

### 自动化测试场景

自动化测试，则是具体到一个指定的场景， 以上面的章节的`graceful（优雅上下线）`为例，我们在目录`./.github/actions/scenarios/spring/graceful`定义了`action.yml`, 该文件所做的是模拟端到端的测试流程，模拟我们手动测试优雅上下线测试过程，将之在自动化测试过程中重现。

### 自动化测试的公共能力

提供公共能力，目的在于简化测试场景编写，同时解耦业务测试场景与公共能力，其主要涉及如下几块：

- 中间件缓存/下载/启动、agent打包缓存/下载
- action公共入口处理（当前仅日志）， action公共出口处理 (当前仅终止进程与清理环境变量)
- 代码目录变更检查与判断

#### 中间件缓存/下载/启动、agent打包缓存/下载

相关组件缓存主要定义于各测试入口，以Spring的`graceful`为例，在入口处，定义两处公共job：

```yaml
download-midwares-and-cache:
    name: download midwares and cache
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: cache local cse
        uses: actions/cache@v3
        with:
          path: Local-CSE-2.1.3-linux-amd64.zip
          key: ${{ runner.os }}-local-cse
          restore-keys: |
            ${{ runner.os }}-local-cse
      - name: download cse
        run: |
          export ROOT_PATH=$(pwd)
          bash ./sermant-integration-tests/scripts/tryDownloadMidware.sh cse
      - name: cache zookeeper
        uses: actions/cache@v3
        with:
          path: apache-zookeeper-3.6.3-bin.tar.gz
          key: ${{ runner.os }}-apache-zookeeper-3.6.3
          restore-keys: |
            ${{ runner.os }}-apache-zookeeper-3.6.3
      - name: download zookeeper
        run: |
          export ROOT_PATH=$(pwd)
          bash ./sermant-integration-tests/scripts/tryDownloadMidware.sh zk
      - name: cache nacos server
        uses: actions/cache@v3
        with:
          path: nacos-server-1.4.2.tar.gz
          key: ${{ runner.os }}-nacos-server-1.4.2
          restore-keys: |
            ${{ runner.os }}-nacos-server-1.4.2
      - name: download nacos
        run: |
          export ROOT_PATH=$(pwd)
          bash ./sermant-integration-tests/scripts/tryDownloadMidware.sh nacos
  build-agent-and-cache:
    name: build agent and cache
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 8
        uses: actions/setup-java@v3
        with:
          java-version: '8'
          distribution: 'adopt'
          cache: maven
      - name: cache agent
        uses: actions/cache@v3
        with:
          path: sermant-agent-*/
          key: ${{ runner.os }}-agent-${{ github.run_id }}
      - name: package agent
        run: |
          sed -i '/sermant-backend/d' pom.xml
          sed -i '/sermant-backend-lite/d' pom.xml
          sed -i '/sermant-integration-tests/d' pom.xml
          sed -i '/sermant-injector/d' pom.xml
          mvn package -DskipTests -Ptest --file pom.xml
```

通过使用github action的needs动作确保在缓存之后再处理具体action测试

```shell
test-for-spring:
    name: Test for spring
    runs-on: ubuntu-latest
    needs: [build-agent-and-cache, download-midwares-and-cache] #依赖前置job
```

随后在统一的action进行相关组件的下载与启动, 具体步骤可参考文件`./.github/actions/common/spring/action.yml`

#### action公共入口处理与出口处理

这里统一了每一个action的入口（entry）与出口（exit），是每个action处理前后必须执行的步骤

- 入口，路径`./.github/actions/common/entry/action.yml`

  ```shell
  name: "Entrance operations"
  description: "do something entrance operations for all test"
  inputs:
    log-dir:
      description: 'Log path for cur workflow'
      required: true
      default: './logs/default'
  runs:
    using: "composite"
    steps:
      - name: create log dir
        shell: bash
        run: |
          log_dir=${{ inputs.log-dir }}
          mkdir -p ${log_dir}
          echo "logDir=${log_dir}" >> $GITHUB_ENV
          echo "=======check log dir======"
          ls -l ${log_dir}
  ```

  统一日志输出路径，方便demo应用启动后，防止各个action占用同一个文件。这里需要一个入参即`log-dir`, 需使用方指定路径。同时，**在需要demo输出日志时，务必添加日志路径前缀环境变量（`${{ env.logDir }}`）进行输出**

- 出口,  路径`./.github/actions/common/exit/action.yml`

  ```yaml
  name: "Exit operations"
  description: "do something exit operations for all test"
  inputs:
    processor-keyword:
      description: 'The keyword associate to processor that you want to kill, if you have one more key word, you can
      separate with |,such as rest|feign'
      required: false
      default: ''
  runs:
    using: "composite"
    steps:
      - name: reset
        shell: bash
        run: |
          echo "tailVersion=" >> $GITHUB_ENV
          echo "healthApi=" >> $GITHUB_ENV
          echo "logDir=" >> $GITHUB_ENV
          keyword='${{ inputs.processor-keyword }}'
          if [ $keyword != '' ];then
            jps -l | grep -E "${keyword}" | awk '{print $1}' | xargs -n 1 kill -9
            echo "========check process========"
            jps -l
          fi
  ```

  出口主要用于环境变量清空与终止进程， 基于jps命令进行查找。使用时需要传入进程关键字，多个关键字使用`|`分隔，关键字来源于具体demo的jar包的名称。

#### 代码目录变更检查

为提升自动化测试的效率与精准性，此处依据提交代码的路径判断对应场景是否需要执行。

这里举个例子，例如`graceful`的代码路径为`sermant-plugins/sermant-service-registry`, 若提交的代码修改了该路径的文件，则会触发该action的测试；若提交的代码为其他的插件，则该场景将不再触发。

该功能的实现依赖于开源action [has-changed-path](https://github.com/marketplace/actions/has-changed-path), 该组件可根据提交代码判断指定路劲的变更情况，我们抽出了独立的action检测我们的代码变更，其路径为`./.github/actions/common/plugin-change-check/action.yml`

如下，为`graceful`的路径检测流程

```yaml
name: "Plugin change check"
description: "check file change for all plugins"
runs:
  using: "composite"
  steps:
  	# 1. 检查sermant-plugins/sermant-service-registry路径是否存在变更文件
    - uses: marceloprado/has-changed-path@v1.0.1
      id: changed-sermant-service-registry
      with:
        paths: sermant-plugins/sermant-service-registry
    - name: env sermant-sermant-service-registry
      shell: bash
      run: |
        echo "sermantServiceRegistryChanged=${{ steps.changed-sermant-service-registry.outputs.changed }}" >> $GITHUB_ENV
    # 2. 判断流水线文件是否更变
    - uses: marceloprado/has-changed-path@v1.0.1
      id: changed-workflow-or-test
      with:
        paths: ./.github/actions ./.github/workflows sermant-integration-tests
    # 3. 判断是否为push事件
    - name: check push event
      shell: bash
      run: |
        eventName=${{ github.event_name }}
        if [ $eventName == 'push' ];then
          echo "triggerPushEvent=true" >> $GITHUB_ENV
        else
          echo "triggerPushEvent=false" >> $GITHUB_ENV
        fi
    - name: statistic scenarios change env
      shell: bash
      run: |
        # ==========graceful is needed to test?==========
        if [ ${{ env.sermantAgentCoreChanged }} == 'true' -o ${{ env.sermantServiceRegistryChanged }} == 'true' ];then
          # 4. 如果agentcore或者graceful插件的文件有变更, 则将环境变量enableGraceful塞入环境变量中
          echo "enableGraceful=true" >> $GITHUB_ENV
        fi
        # all workflow will trigger while workflow changed
        if [ ${{ steps.changed-workflow-or-test.outputs.changed }} == 'true' -o ${{ env.triggerPushEvent }} == 'true' ];then
          # 5. 若工作流有变更，同样将之塞入环境变量中
          echo "enableGraceful=true" >> $GITHUB_ENV
        fi
```

其中`paths`为指定的变更路径检测。上面的action指定后，将存放环境变量`env.enableGraceful`, 该环境变量将在workflow使用，如下：

```yaml
- name: (graceful) test for springboot=${{ matrix.springBootVersion }} springCloudVersion=${{ matrix.springCloudVersion }}
  if: env.enableGraceful == 'true'
  uses: ./.github/actions/scenarios/spring/graceful
```

仅`env.enableGraceful`为true时，`graceful`测试用例才予以执行。

**注意：**

除以上路径判断，还存在整个工作流的路径判断，针对`pull_request`生效，存在如下配置：

```shell
on:
  push:
  pull_request:
    branches:
      - main
      - develop
    paths:
      - 'sermant-agentcore/**'
      - 'sermant-integration-tests/**'
      - 'sermant-plugins/sermant-dynamic-config/**'
      - 'sermant-plugins/sermant-flowcontrol/**'
      - 'sermant-plugins/sermant-loadbalancer/**'
      - 'sermant-plugins/sermant-service-registry/**'
      - 'sermant-plugins/sermant-springboot-registry/**'
      - '.github/workflows/spring_integration*.yaml'
      - '.github/actions/**'
```

可以看到`paths`配置对应的集合，若使该工作流生效运行，则提交的代码路径需包含在内。

**因此若新增插件或者目录，务必在`paths`增加对应的目录！**

## 编写具体的测试场景

那自动化测试该如何编写呢，主要包含以下步骤：

1. 判断当前的中间件是否满足要求（当前仅支持zk，nacos，LocalCse）
2. 增加公共的中间件支持（可选）
3. 编写具体场景的测试demo与对应Junit测试用例
4. 编写测试action，模拟手动测试流程，在action复现
5. 增加代码路径检测（可选）
6. 根据框架类型与版本矩阵将action添加到自动化测试入口，即workflow
7. 修改工作流入口的paths路径（可选）
8. 提交工作流进行测试

### 增加中间件支持

(1) 中间件添加缓存需要在对应自动化测试入口添加缓存步骤，如下部分：

```yaml
download-midwares-and-cache:
    name: download midwares and cache
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: cache zookeeper
        uses: actions/cache@v3
        with:
          path: apache-zookeeper-3.6.3-bin.tar.gz
          key: ${{ runner.os }}-apache-zookeeper-3.6.3
          restore-keys: |
            ${{ runner.os }}-apache-zookeeper-3.6.3
      - name: download zookeeper
        run: |
          export ROOT_PATH=$(pwd)
          bash ./sermant-integration-tests/scripts/tryDownloadMidware.sh zk
```

以zookeeper为例， 首先使用[`actions/cache@v3`](https://github.com/marketplace/actions/cache)进行缓存， 随后执行下载步骤，当触发`cache-hit`则下次进来时，直接取用缓存的zookeeper。

(2) 添加启动步骤

首先判断该中间件是否有通用性，如果通用性较高，建议抽到公共的action中，否则仅在当前的action管理该中间件的生命周期。

a) 在公共的action中添加启动步骤

如果为Dubbo应用，则添加启动步骤到`./.github/actions/common/dubbo/action.yml`

如果为Spring应用, 则添加启动步骤到`./.github/actions/common/spring/action.yml`

b) 在当前action添加启动步骤

此时，则需在action的开始添加中间件的启动步骤，并在结束时添加中间件的终止步骤

### 编写具体场景的测试demo与对应Junit测试用例

找到目录`sermant-integration-tests`, 首先判断当前的测试场景是否适用于现存的demo应用，如果适用，则添加相关测试接口即可，否则需要自己编写独立的测试demo。

随后在目录`sermant-integration-tests/spring-test/spring-integration-test`或者`sermant-integration-tests/dubbo-test/dubbo-integration-test`编写对用的Junit测试用例。

### 编写action

首先需要在`./.github/actions/scenarios`目录下创建当前场景的目录， 例如`./.github/actions/scenarios/test/action.yml`, 内容如下：

```yaml
name: "{Scenarios} Test"
description: "Auto test for {your scenarios}"
runs:
  using: "composite"
  steps:
    - name: entry
      uses: ./.github/actions/common/entry
      with:
        log-dir: ./logs/test
    - name: package common demos
      shell: bash
      run: |
        xxx
    - name: start provider that has closed graceful ability
      shell: bash
      run: |
        xxx
    - name: start applications that has opened graceful ability
      shell: bash
      env:
        grace.rule.enableSpring: true
      run: |
        xxx
    - name: waiting for services start
      shell: bash
      run: |
        ps -ef | grep java
        bash ./sermant-integration-tests/scripts/checkService.sh http://127.0.0.1:8015/graceful/testGraceful 120
    - name: test graceful up
      shell: bash
      run: mvn test -Dsermant.integration.test.type=GRACEFUL -Dgraceful.test.type=up --file sermant-integration-tests/spring-test/pom.xml
    - name: exit
      if: always()
      uses: ./.github/actions/common/exit
      with:
        processor-keyword: feign|rest
    - name: if failure then upload error log
      uses: actions/upload-artifact@v3
      if: ${{ failure() || cancelled() }}
      with:
        name: (${{ github.job }})-graceful-(${{ matrix.springBootVersion }}-${{ matrix.springCloudVersion }})-logs
        path: |
          ./*.log
          ./logs/**
        if-no-files-found: warn
        retention-days: 2

```

如上，主要有以下几个大块：

1. entry
2. package demo
3. start
4. test
5. exit
6. if failure then upload error log （错误时上传错误日志）

按照以上步骤编写, 其中模拟端到端测试主要在步骤2,3,4

### 增加代码路径判断

参考章节[代码目录变更检查](#代码目录变更检查)， 若为新增插件类型，需增加对应插件路径进行判断，并在自动化测试入口处通过环境变量进行触发action测试

### 将action添加到自动化测试入口

通常情况， 入口参考[入口章节](#自动化测试入口)。添加当前action， 如下(参考优雅上下线)：

```yaml
steps:
  - name: (graceful) test for springboot=${{ matrix.springBootVersion }} springCloudVersion=${{ matrix.springCloudVersion }}
    if: env.enableGraceful == 'true'
    uses: ./.github/actions/scenarios/spring/graceful
```

若当前入口的矩阵与测试场景严重不符，可考虑新增入口，或者基于入口的workflow添加新的job， 编写属于自己的版本矩阵（这里可参考job任务`test-for-spring`）；当然此处可复用步骤，例如中间件缓存，公共处理步骤等。

### 添加path到入口检测路径

```yaml
on:
  push:
  pull_request:
    branches:
      - main
      - develop
    paths:
      - 'sermant-agentcore/**'
      - 'sermant-plugins/sermant-dynamic-config/**'
      - 'sermant-plugins/sermant-flowcontrol/**'
      - 'sermant-plugins/sermant-loadbalancer/**'
      - 'sermant-plugins/sermant-service-registry/**'
      - 'sermant-plugins/sermant-springboot-registry/**'
      - '.github/workflows/spring_integration*.yaml'
      - '.github/actions/**'
```

如上为，该工作流入口的触发事件, 其中`branches`与`paths`需同时满足，才会触发工作流。而此处`paths`则为路径检测，仅当路径为paths列表匹配的代码才会触发工作流，**因此，如果此处新增的代码在其他目录，务必将路径添加到`paths`下。**

## 其他

### 统一sermant版本

当前自动化测试统一了版本，定义在工作流的入口中，如下：

```yaml
name: Spring Integration Test1
env:
  sermantVersion: 1.0.0
```

后续的demo启动命令，均需基于环境变量`${{ env.sermantVersion }}`作为agent的版本路径，例如：

`-javaagent:sermant-agent-${{ env.sermantVersion }}/agent/sermant-agent.jar=appName=default`

### 并发组

当前针对每一个工作流入口，均定义了并发组,  如下配置:

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}-${{ github.head_ref }}
  cancel-in-progress: true
```

其中

`${{ github.workflow }}` 为当前工作流的名称

`${{ github.event.pull_request.number }}`为pull request的编号

`${{ github.ref }}`为pull request的目标分支；当为push事件时， 其值为当前分支

`${{ github.head_ref }}`为pull request的源分支；当为push事件时，其值为空

若相同的两个任务的group相同，后者将会使前者取消工作流

### 关于工作流的触发条件

触发具体action（具体测试场景）依赖于workflow（工作流）的触发，工作流的触发则依赖于`push`与`pull_request`事件。因此自动化测试workflow主要依赖于前面的两种事件，同时workflow触发一定优先与action触发，仅触发了workflow，才有action这一层测试触发。下面基于优先级依次说明workflow的触发与action的触发。

**（1）Workflow触发**

- `push`事件，即commit操作，该事件默认会触发所有的工作流，不区分分支，路径

- `pull_request`, 即提PR操作，该事件会具体考量到分支名称与代码路径。举个栗子，如下：

  ```yaml
  on:
    pull_request:
      branches:
        - main
        - develop
      paths:
        - 'sermant-agentcore/**'
        - 'sermant-integration-tests/**'
        - 'sermant-plugins/sermant-dynamic-config/**'
  ```

  如上触发条件则是：

  1. 合并的分支是`main`或者`develop`分支
  2. 提交的代码路径必须在`sermant-agentcore,sermant-integration-tests,sermant-plugins/sermant-dynamic-config`中

  以上两个条件都满足才会生效。

  **因此呢，若新增插件测试，务必得添加新的目录到`paths`下。**

**（2）Action（特指具体测试场景的action，非公共的action）触发**

​	action的触发是在workflow触发之后，仅workflow触发，action才有可能触发。

​	action的触发更考量的是代码路径，但特定的路径或者事件则会开启全量的自动化测试。下面也以具体事件进行说明。

- `push`事件，该事件默认将开启所有action测试，我们可以找到路径判断的action文件，参考章节[代码目录变更检查](#代码目录变更检查)， 若为该事件，则所有的action开关都将开启。
- `pull_request`事件，该事件则是具体判断代码变更路径，参考章节[代码目录变更检查](#代码目录变更检查)；这里将根据不同的路径确定不同的测试场景，例如`sermant-plugins/sermant-router`存在文件变更，那我则认为关联路由的场景均可开启测试，同时在自动化测试入口添加对应开关，控制自动化测试的执行。

