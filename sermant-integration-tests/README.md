# Automatic Test Manual

[简体中文](README-zh.md) | [English](README.md)

This document describes automated tests and how to write an automated test.。

## Automatic Test Components

```yaml
├─.github   	
│  ├─actions                            #stores all test scenarios.
│  │  ├─common                          #common directory for storing test scenarios.
│  │  │  ├─dubbo                        #dubbo common procedure in test scenarios
│  │  │  ├─entry                        #common procedure for action entry
│  │  │  ├─exit                         #common procedure for action exit
│  │  │  ├─plugin-change-check          #action for check plugin dir change
│  │  │  └─spring                       #spring common procedure in test scenarios
│  │  └─scenarios                       #define specific test scenarios.
│  │      ├─dubbo                       #dubbo test scenarios
│  │      │  ├─dubbo-common             #dubbo common test scenarios
│  │      │  └─router                   #dubbo router test scenarios
│  │      └─spring                      #spring test scenarios
│  │          ├─dynamic-config-nacos    #spring dynacos config for nacos test scenarios
│  │          ├─dynamic-config-zk       #spring dynacos config for zk test scenarios
│  │          ├─graceful                #spring graceful online/offline test scenario
│  │          ├─router                  #spring router test scenarios
│  │          │  ├─spring-router        #spring router test scenario
│  │          │  └─spring-tag-router    #spring router test scenario for springboot
│  │          └─spring-common           #spring common test scenarios
│  ├─ISSUE_TEMPLATE
│  └─workflows                          #auto test workflow
└─sermant-integration-tests             #storing automated tests demo
    ├─dubbo-test                        #dubbo automated tests demo
    │  ├─dubbo-2-6-integration-consumer
    │  ├─dubbo-2-6-integration-controller
    │  ├─dubbo-2-6-integration-provider
    │  ├─dubbo-2-7-integration-consumer
    │  ├─dubbo-2-7-integration-controller
    │  ├─dubbo-2-7-integration-provider
    │  ├─dubbo-integration-api
    │  └─dubbo-integration-test
    ├─scripts                           #auto test script
    └─spring-test                       #spring automated tests demo
        ├─spring-common					
        ├─spring-common-demos           #spring common demos
        │  ├─spring-common-feign        #common feign demo， 2.x
        │  ├─spring-common-feign-1.5.x  #common feign demo, 1.5.x
        │  ├─spring-common-gateway      #common gateway for demo-gateway
        │  ├─spring-common-resttemplate #common Resttemplate test demo
        │  └─spring-common-zuul         #common gateway for demo-zuul
        ├─spring-intergration-test      #auto test ut(Junit)
        ├─spring-nacos-config           #nacos dynamic config demo
        └─spring-zookeeper-config       #zk dynamic config demo
```

The preceding figure shows the directory structure of the current automatic test, which consists of the following three parts:

- workflow, this is an automatic test entry. You need to add actions to be executed in the workflow (test scenario).
- actions，the specific end-to-end test procedure is defined here.
- sermant-integration-tests， this part stores the test demo required by the action and compiles the junit test cases that require the test logic.

## Automatic Test Entry

You need to define the automatic test entry in the workflow. Currently, the workflow has test entries based on the Dubbo and Spring frameworks. The entry file is as follows:

- `dubbo_integration_test.yml`, Dubbo test scenario entry
- `spring_integration_test_1`, `spring_integration_test_2`, Spring test scenario entry. The latter is mainly used to test the label routing capability, while the former is used to test the capabilities of other plug-ins, including flow control, registration, graceful login and logout, and load balancing. The reason why the GitHub workflow is split is to make full use of the concurrent test of the GitHub workflow to improve the test efficiency.

## Automatic Test Design

### Automatic Test Entry Design

Currently, the automatic test is driven based on the version matrix of the test framework and depends on the [GitHub action matrix]((https://docs.github.com/cn/actions/using-jobs/using-a-matrix-for-your-jobs)). The version matrix is set at the workflow entry. Based on the specified matrix, all actions (specific test scenarios) configured at the entry are tested to achieve the test purpose.

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
      - uses: actions/checkout@v2
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

As shown in the preceding figure, two matrix versions are defined. Take `springBootVersion` as an example. There are `1.5.0.RELEASE` and `2.0.2.RELEASE`. During the actual test, the actions(configured `graceful` and `zk dynamic config`)  of the two versions are tested at the same time (if the concurrency is not limited).The execution does not affect each other.

The action path is specified by the `uses` configuration file.

### Automatic Test Scenario

The automatic test is specific to a specified scenario. Take `graceful` in the preceding section as an example. The` action.yml` file is defined in the `./.github/actions/scenarios/spring/graceful` directory. This file simulates the end-to-end test process and simulates the manual graceful login and logout test process. Reproduce it during automated testing.

### Common Capabilities of Automated Testing

Common capabilities are provided to simplify test scenario compilation and decouple service test scenarios from common capabilities. The common capabilities are as follows:

- Middleware caching/downloading/starting, agent packaging caching/downloading
- Action public entry processing (only logs currently) and action public exit processing (only processes are terminated and environment variables are cleared currently)
- Code directory change check and judgment

#### Middleware caching/downloading/starting and Agent packaging caching/downloading

The related component cache is defined at each test entry. The following uses Spring `graceful` as an example. Two public jobs are defined at the entry:

```yaml
download-midwares-and-cache:
    name: download midwares and cache
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
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
      - uses: actions/checkout@v2
      - name: Set up JDK 8
        uses: actions/setup-java@v2
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

Use the `needs` action of GitHub action to ensure that specific action tests are processed after caching.

```shell
test-for-spring:
    name: Test for spring
    runs-on: ubuntu-latest
    needs: [build-agent-and-cache, download-midwares-and-cache] #Depending on the front job
```

Download and start related components in the unified action. For details, see the file `./.github/actions/common/spring/action.yml`

#### Action Common Entry Processing And Exit Processing

The entry and exit of each action are unified. The steps must be performed before and after each action is processed.

- Entry, it's path is `./.github/actions/common/entry/action.yml`

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

  The log output path is unified to prevent each action from occupying the same file after the demo application is started. The input parameter `log-dir` is required. The user needs to specify the path. In addition, **when the demo needs to output logs, add the log path prefix environment variable (`${{env.logDir}}`)**.

- Exit,   it's path is `./.github/actions/common/exit/action.yml`

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

  The exit action is used to clear environment variables and terminate processes based on the jps command. Process keywords need to be transferred. Multiple keywords are separated by vertical bars (`|`). The keywords are obtained from the name of the JAR package of the demo.

#### Code Directory Change Check

To improve the efficiency and accuracy of the automatic test, determine whether to execute the corresponding scenario based on the code submission path.

For example, the `graceful` code path is `sermant-plugins/sermant-service-registry`, . If the submitted code modifies the file in the path, the action test is triggered. If the submitted code is another plug-in, the scenario will not be triggered.

The implementation of this function depends on the open-source action [has-changed-path](https://github.com/marketplace/actions/has-changed-path). This component can determine the change of a specified path force based on the submitted code. An independent action is extracted to detect the code change. The path is `./.github/actions/common/plugin-change-check/action.yml`.

The following figure shows the `graceful` path detection process.

```yaml
name: "Plugin change check"
description: "check file change for all plugins"
runs:
  using: "composite"
  steps:
  	# 1. Check whether change files exist in the sermant-plugins/sermant-service-registry     directory.
    - uses: marceloprado/has-changed-path@v1
      id: changed-sermant-service-registry
      with:
        paths: sermant-plugins/sermant-service-registry
    - name: env sermant-sermant-service-registry
      shell: bash
      run: |
        echo "sermantServiceRegistryChanged=${{ steps.changed-sermant-service-registry.outputs.changed }}" >> $GITHUB_ENV
     # 2. Check whether the pipeline file is changed.
    - uses: marceloprado/has-changed-path@v1
      id: changed-workflow-or-test
      with:
        paths: ./.github/actions ./.github/workflows sermant-integration-tests
    - name: statistic scenarios change env
      shell: bash
      run: |
        # ==========graceful is needed to test?==========
        if [ ${{ env.sermantAgentCoreChanged }} == 'true' -o ${{ env.sermantServiceRegistryChanged }} == 'true' ];then
          # 3. If the file of the agentcore or graceful plugin changes, add the environment variable enableGraceful to the environment variable.
          echo "enableGraceful=true" >> $GITHUB_ENV
        fi
        # all workflow will trigger while workflow changed
        if [ ${{ steps.changed-workflow-or-test.outputs.changed }} == 'true' ];then
          # 4. If the workflow changes, add it to the environment variable.
          echo "enableGraceful=true" >> $GITHUB_ENV
        fi
```

In the preceding command, `paths` indicates the specified change path. After the preceding action is specified, the environment variable `env.enableGraceful` is stored. The environment variable is used in the workflow, as shown in the following figure:

```yaml
- name: (graceful) test for springboot=${{ matrix.springBootVersion }} springCloudVersion=${{ matrix.springCloudVersion }}
  if: env.enableGraceful == 'true'
  uses: ./.github/actions/scenarios/spring/graceful
```

The `graceful` test case is executed only when `env.enableGraceful` is true.

##  Create Specific Test Scenarios.

How to compile an automated test? The steps are as follows:

1. Check whether the current middleware meets the requirements. (Currently, only ZooKeeper, Nacos, and LocalCse are supported.)
2. (Optional) Add common middleware support.
3. Coding test demos and corresponding Junit test cases for specific scenarios.
4. Create the test action, simulate the manual test process, and reproduce the problem in the action.
5. (Optional) Adding code path detection
6. Add actions to the automatic test entry, that is, workflow, based on the framework type and version matrix.
7. (Optional) Modify the paths of the working ingress.
8. Submit the workflow for testing

### Added Middleware Support

(1) To add the cache to the middleware, you need to add the cache steps in the corresponding automatic test entry. The steps are as follows:

```yaml
download-midwares-and-cache:
    name: download midwares and cache
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
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

An example. Use [actions/cache@v3](https://github.com/marketplace/actions/cache) to cache data, and then perform the download step. When `cache-hit` is triggered, the cached ZooKeeper is directly used next time.

(2) Add Startup Steps for Mideware

Check whether the middleware is universal. If the middleware is universal, it is recommended that the middleware be included in the common action. Otherwise, the life cycle of the middleware is managed only in the current action.

a) Add the startup step to the common action.

If the application is Dubbo, add the startup steps to`./.github/actions/common/dubbo/action.yml`

If the application is a Spring application, add the startup step to`./.github/actions/common/spring/action.yml`

b) Add a startup step to the current action.

In this case, you need to add the middleware startup step at the beginning of the action, and add the middleware termination step at the end of the action.

### Coding Test Demos and Corresponding Junit Test Cases for Specific Scenarios.

Find the `sermant-integration-tests` directory and check whether the current test scenario is applicable to the existing demo application. If yes, add related test interfaces. Otherwise, you need to compile an independent test demo.

Codeing the Junit test cases in the `sermant-integration-tests/spring-test/spring-integration-test` or `sermant-integration-tests/dubbo-test/dubbo-integration-test` directory.

### Create Action

Create a directory in the current scenario in `./.github/actions/scenarios`. For example, the content of `./.github/actions/scenarios/test/action.yml`, is as follows:

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

As shown above, there are the following steps:

1. entry
2. package demo
3. start
4. test
5. exit
6. if failure then upload error log （Upload error logs when errors occur）

Perform the preceding steps. Steps 2, 3, and 4 are used to simulate the end-to-end test.

### Add Check for Code Path File Modify

Referring to [Code Directory Change Check](#Code-Directory-Change-Check). If the plugin type is a new plug-in type, add the corresponding plugin path and trigger the action test using environment variables at the entrance of the automatic test.

### Add The Action to the Automated Test Entry.

For details about the entry, see [section entry](#Automatic-Test-Entry). Add the current action as follows (for details, see Graceful Online and Offline):

```yaml
steps:
   - name: (graceful) test for springboot=${{ matrix.springBootVersion }} springCloudVersion=${{ matrix.springCloudVersion }}
     if: env.enableGraceful == 'true'
     uses: ./.github/actions/scenarios/spring/graceful
```

If the matrix of the current entry is seriously inconsistent with the test scenario, you can add an entry or add a job based on the workflow of the entry to compile your own version matrix. (For details, see the` test-for-spring` job task.) Certainly, steps may be reused herein, for example, middleware caching and common processing steps.

### Add Path to the Ingress Detection Path.

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

The preceding figure shows the triggering event of the work ingress. The workflow is triggered only when `branches` and `paths` meet the requirements at the same time. In this example, `paths` are used for path detection. The workflow is triggered only when the path is matched with the code in the paths list. **Therefore, if the new code is in another directory, you must add the path to `paths`.**

## Others

### Unified Sermant Version

Currently, the automatic test version is unified and defined in the workflow entry, as shown in the following figure:

```yaml
name: Spring Integration Test1
env:
  sermantVersion: 1.0.0
```

The following demo startup commands must use the environment variable `${{env.sermantVersion}}` as the agent version path. For example:

`-javaagent:sermant-agent-${{ env.sermantVersion }}/agent/sermant-agent.jar=appName=default`

### Concurrency Group

Currently, a concurrent group is defined for each working inbound interface. The configuration is as follows:

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.head_ref }}-${{ github.ref }}
  cancel-in-progress: true
```

`${{ github.workflow }}`indicates  the name of the current workflow

`${{ github.head_ref }}`indicates  source branch of the pull request

`${{ github.ref }}` indicates the target branch of the pull request.

If two tasks have the same group, the former will cancel the workflow.

