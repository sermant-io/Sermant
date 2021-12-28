import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import moment from 'moment';

if (process.env.NODE_ENV === 'development') {
  const mock = new MockAdapter(axios, { delayResponse: 500 });
  // mock.onGet('/argus/api/support').replyOnce(401)
  mock.onGet('/argus/api/support').reply(200, {
    data: Array.from({ length: 8 }, function () {
      return { title: "快速压测技术指南", url: "http://w3.huawei.com/next/indexa.html" }
    })
  });
  let scenario = {
    scenario_id: 1,
    app_name: "ARGUS", scenario_name: "ARGUS快速场景",
    scenario_type: "动态编排", create_by: "xwx638739",
    create_time: "2019-03-19 10:53", update_time: "2019-03-19 10:53",
    label: ["a", "b", "c"], desc: "描述"
  }
  mock.onGet('/argus/api/scenario').reply(function () {
    return [200, {
      data: [
        scenario,
        {
          scenario_id: 2,
          app_name: "QUARK", scenario_name: "VPC乌兰察布",
          scenario_type: "引流压测", create_by: "ywx600465",
          create_time: "2019-03-19 10:53", update_time: "2019-03-19 10:53"
        },
        {
          scenario_id: 3,
          app_name: "SRE应用接入", scenario_name: "SRE性能压测",
          scenario_type: "自定义脚本", create_by: "j00466872",
          create_time: "2019-03-19 10:53", update_time: "2019-03-19 10:53"
        },
        {
          scenario_id: 4,
          app_name: "CloudAgent", scenario_name: "AGENT场景",
          scenario_type: "动态编排", create_by: "j00466872",
          create_time: "2019-03-19 10:53", update_time: "2019-03-19 10:53"
        },
        {
          scenario_id: 5,
          app_name: "SRE乌兰察布", scenario_name: "SRE乌兰察布",
          scenario_type: "引流压测", create_by: "xwx638736",
          create_time: "2019-03-19 10:53", update_time: "2019-03-19 10:53"
        },
      ],
      total: 5
    }]
  });
  mock.onGet('/argus/api/scenario/search').reply(function (config) {
    let data = ['场景A', '场景B', '场景C', '场景D']
    if (config.params?.value) {
      data = data.filter(item => item.indexOf(config.params.value) !== -1)
    } else {
      data = data.slice(0, 2)
    }
    return [200, { data }]
  })
  mock.onPost('/argus/api/scenario').reply(200)
  mock.onPut('/argus/api/scenario').reply(function () {
    scenario = {
      scenario_id: 1,
      app_name: "ARGUS", scenario_name: "ARGUS快速场景修改",
      scenario_type: "动态编排", create_by: "xwx638739",
      create_time: "2019-03-19 10:53", update_time: "2019-03-19 10:53",
      label: ["a", "b", "c"], desc: "描述"
    }
    return [200]
  })
  mock.onGet("/argus/api/scenario/deleteCheck").reply(200, { data: ["Argus"] })
  mock.onDelete('/argus/api/scenario').reply(200, { msg: "场景被应用，无法删除" })
  // 压测脚本
  mock.onGet('/argus/api/script').reply(200, {
    data: [
      {
        type: "folder", script_name: "100.95.133.126",
        commit: "Quick test for http://100.95.133.126:48080/testLongText",
        update_time: "2019-03-19 10:53", version: "224 ", size: ""
      },
      {
        type: "file", script_name: "mockKafka.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka2.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka3.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka4.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka5.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka6.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka7.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka8.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
      {
        type: "file", script_name: "mockKafka9.py",
        commit: "quark Test",
        update_time: "2019-03-19 10:53", version: "162", size: "48"
      },
    ],
    total: 100
  })
  mock.onGet("/argus/api/script/deleteCheck").reply(200, { data: ["xxx.py", "xxx.py"] })
  mock.onDelete('/argus/api/script').reply(200, { msg: "删除失败，请重试" })
  mock.onPut('/argus/api/script').reply(200, { msg: "更新失败" })
  mock.onPost('/argus/api/script/check').reply(200, { data: "共三行错误\n第一行错误！\n第一行错误！\n第一行错误！\n第一行错误！" })
  mock.onGet('/argus/api/script/search').reply(200, {
    data: ["10.1.0.1/TEST_000000000.py", "10.1.0.1/TEST_000000001.py"]
  })
  mock.onGet('/argus/api/script/get').reply(function () {
    return [200, {
      data: {
        script: `from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support.expected_conditions import presence_of_element_located

#This example requires Selenium WebDriver 3.13 or newer
with webdriver.Firefox() as driver:
    wait = WebDriverWait(driver, 10)
    driver.get("https://google.com/ncr")
    driver.find_element(By.NAME, "q").send_keys("cheese" + Keys.RETURN)
    first_result = wait.until(presence_of_element_located((By.CSS_SELECTOR, "h3")))
    print(first_result.get_attribute("textContent"))`,
        script_resource: `resource
aaa`,
        language: "Groovy"
      }
    }]
  })
  mock.onGet('/argus/api/script/host').reply(200, {
    data: [
      { host_id: 1, domain: "zengfan.huawei.com" },
      { host_id: 2, domain: "zengfan.huawei.com" },
      { host_id: 3, domain: "zengfan.huawei.com" },
      { host_id: 4, domain: "zengfan.huawei.com" },
      { host_id: 5, domain: "zengfan.huawei.com" },
      { host_id: 6, domain: "zengfan.huawei.com" },
      { host_id: 7, domain: "zengfan.huawei.com" },
      { host_id: 8, domain: "zengfan.huawei.com" },
      { host_id: 9, domain: "zengfan.huawei.com" },
      { host_id: 10, domain: "zengfan.huawei.com" },
    ]
  })
  mock.onPost('/argus/api/script/host').reply(200, { msg: "创建失败" })
  mock.onDelete('/argus/api/script/host').reply(200, { msg: "删除" })
  let time_a = 90000
  mock.onGet('/argus/api/script/host/chart').reply(function (config) {
    if (!config.params.start) {
      time_a += 1000
      return [200, { data: [{ time: moment(new Date(time_a)).format("mm:ss"), usage: Number(Math.random().toFixed(2)) * 100, memory: 100 + Number(Math.random().toFixed(2)) * 100 }] }]
    }
    return [200, {
      data: Array.from({ length: 91 }, function (_, index) {
        return { time: moment(new Date(index * 1000)).format("mm:ss"), usage: Number(Math.random().toFixed(2)) * 100, memory: 100 + Number(Math.random().toFixed(2)) * 100 }
      })
    }]
  })
  mock.onPost("/argus/api/script").reply(200, { msg: "创建失败" })
  mock.onPost("/argus/api/script/folder").reply(200, { msg: "创建失败" })
  // 压测任务
  mock.onGet('/argus/api/task').reply(200, {
    data: Array.from({ length: 10 }, function (_, index) {
      return {
        test_id: index, status: index === 0 ? "running" : "fail", test_name: "Test for 100.95.133.126",
        status_label: "运行中",
        test_type: "快速压测", script_path: "100.95.133.126/traLongText", owner: "admin",
        start_time: "2019-03-19 10:53", duration: "00:01:00",
        tps: "7.5", mtt: "0", fail_rate: "0%",
        label: ["a", "b"], desc: "描述，长文本长文本长文本长文本长文本长文本"
      }
    }),
    total: 80
  })
  mock.onGet('/argus/api/task/maxAgent').reply(200, {
    data: 10
  })
  mock.onGet('/argus/api/task/tags').reply(function (config) {
    let data = ['场景A', '场景B', '场景C', '场景D']
    if (config.params?.value) {
      data = data.filter(item => item.indexOf(config.params.value) !== -1)
    } else {
      data = data.slice(0, 2)
    }
    return [200, { data }]
  })
  mock.onPost('/argus/api/task').reply(200, { msg: "创建失败" })
  mock.onPut('/argus/api/task/update').reply(200, { msg: '更新失败' })
  let count = 10
  let time_b = 89000
  mock.onGet('/argus/api/task/get').reply(function (config) {
    time_b += 1000
    count--
    return [200, {
      data: {
        status: count > 0 ? "running" : "success",
        status_label: count > 0 ? "运行中" : "结束", test_name: "Test for 100.95.133.126",
        label: ["ARGUS", "快速压测", "ARGUS", "性能压测"],
        desc: "100.95.133.126/traLongText",
        duration: moment(new Date(count * 1000)).format("mm:ss"), vuser: 10, tps: 2.2, tps_peak: 5, avg_time: 4535.26,
        test_count: 115, success_count: 115, fail_count: 0,
        test_comment: "测试注释",
        log_name: ["anent-NONE-log1.zip", "anent-NONE-log2.zip"],
        progress_message: ["第一行失败", "第二行失败"],
        chart: !config.params.start ? [{
          time: moment(new Date(time_b)).format("mm:ss"),
          tps: Number(Math.random().toFixed(1)) * 10,
        }] : Array.from({ length: 91 }, function (_, index) {
          return {
            time: moment(new Date(index * 1000)).format("mm:ss"),
            tps: Number(Math.random().toFixed(1)) * 10,
          }
        })
      },
    }]
  })
  mock.onPost('/argus/api/task/pressurePrediction').reply(200, {
    data: [{
      "time": "1",
      "pressure": 7
    }, {
      "time": "2",
      "pressure": 12
    }, {
      "time": "3",
      "pressure": 11
    }, {
      "time": "4",
      "pressure": 16
    }, {
      "time": "5",
      "pressure": 21
    }, {
      "time": "6",
      "pressure": 18
    }, {
      "time": "7",
      "pressure": 25
    }, {
      "time": "8",
      "pressure": 23
    }, {
      "time": "9",
      "pressure": 28
    }, {
      "time": "10",
      "pressure": 30
    }]
  })
  mock.onDelete('/argus/api/task').reply(200, { msg: "删除出错了，稍后再试" })
  mock.onPost('/argus/api/task/start').reply(200)
  mock.onPost('/argus/api/task/stop').reply(200, { msg: "停止失败" })
  const agent = {
    agent_id: 10, status: "running", domain: "192.168.14.187", port: 12002,
    agent_name: "agent", version: "3.4.2", region: "NONE", licensed: true,
    status_label: "运行中"
  }
  mock.onGet('/argus/api/agent').reply(200, {
    data: [
      agent,
      ...Array.from({ length: 9 }, function (_, index) {
        return {
          agent_id: index, status: "running", status_label: "运行中", domain: "192.168.14.187", port: 12002,
          agent_name: "agent", version: "3.4.2", region: "NONE", licensed: false
        }
      })
    ], total: 13
  })
  mock.onGet('/argus/api/agent/get').reply(200, {
    data: agent
  })
  mock.onGet('/argus/api/agent/chart').reply(function () {
    return [200, {
      data: {
        usage: Number(Math.random().toFixed(2)) * 100,
        memory: 100 + Number(Math.random().toFixed(2)) * 100
      }
    }]
  })
  mock.onDelete('/argus/api/agent').reply(200, { msg: "删除失败，代理运行中" })
  mock.onPost('/argus/api/agent/stop').reply(200, { msg: "停止失败，无响应" })
  mock.onPost('/argus/api/agent/license').reply(200)
  mock.onGet('/argus/api/agent/link').reply(200, {
    data: {
      link: 'ngrinder-agent-3.4.2.tar'
    }
  })
  mock.onGet("/argus/api/report").reply(200, {
    data: [
      {
        report_id: "1211230", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211231", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211232", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211233", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211234", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211235", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211236", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211237", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211238", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
      {
        report_id: "1211239", test_name: "Test for 100.95.133.126", test_type: "快速压测", test_id: 0,
        owner: "admin", start_time: "2019-03-19 10:53", end_time: "2019-3-19 10:53", duration: "00:01:11"
      },
    ],
    total: 12
  })
  mock.onDelete("/argus/api/report").reply(200, { msg: "删除失败，报告不存在" })
  mock.onGet("/argus/api/report/get").reply(200, {
    data: {
      test_name: "argus-test 快速压测",
      label: ["ARGUS", "快速压测", "ARGUS", "性能压测"],
      desc: "argus-test快速压测是一个测试压测，测试压测，测试压测测试压测，测试压测",
      agent: 1, sampling_ignore: 0, plugin: 1, target_host: "100.95.133.12",
      start_time: "2019-03-19 10:20:26", test_time: "00:01:00",
      end_time: "2019-03-19 10:21:26", run_time: "00:01:00",
      process: 1, thread: null, vuser: 10, tps: 2.2, tps_max: 4, avg_time: 4535.26,
      test_count: 115, success_count: 115, fail_count: 0
    }
  })
  mock.onGet("/argus/api/report/chart").reply(function () {
    return [200, {
      data: Array.from({ length: 91 }, function (_, index) {
        return {
          time: moment(new Date(index * 1000)).format("mm:ss"),
          tps: Number(Math.random().toFixed(1)) * 10,
          avg_time: Number(Math.random().toFixed(4)) * 30000,
          receive_avg: Number(Math.random().toFixed(1)) * 10,
          vuser: Number(Math.random().toFixed(1)) * 10,
          fail_count: Number(Math.random().toFixed(1)) * 10,
        }
      })
    }]
  })
  let time_c = 1200000
  mock.onGet("/argus/api/monitor").reply(function (config) {
    function monitor() {
      return {
        cupUseage: 20 + Number(Math.random().toFixed(1)) * 10,
        cusSysUseage: 10 + Number(Math.random().toFixed(1)) * 10,
        cusUserUseage: Number(Math.random().toFixed(1)) * 10,
        memory: 100 + Number(Math.random().toFixed(1)) * 10,
        diskRead: 100 + Number(Math.random().toFixed(1)) * 10,
        diskWrite: 100 + Number(Math.random().toFixed(1)) * 10,
        networkRead: 100 + Number(Math.random().toFixed(1)) * 10,
        networkWrite: 100 + Number(Math.random().toFixed(1)) * 10,
        fullGcCount: 10 + Number(Math.random().toFixed(1)) * 10,
        fullGcSpend: 10 + Number(Math.random().toFixed(1)) * 10,
        ygcCount: 10 + Number(Math.random().toFixed(1)) * 10,
        ygcSpend: 10 + Number(Math.random().toFixed(1)) * 10,
        threadCount: 10 + Number(Math.random().toFixed(1)) * 10,
        threadRunning: 10 + Number(Math.random().toFixed(1)) * 10,
        threadPeak: 10 + Number(Math.random().toFixed(1)) * 10,
        threadDead: 10 + Number(Math.random().toFixed(1)) * 10,
        threadGuard: 10 + Number(Math.random().toFixed(1)) * 10,
        heapMemory: 10 + Number(Math.random().toFixed(1)) * 10,
        nonHeapMemory: 10 + Number(Math.random().toFixed(1)) * 10,
        classRunning: 10 + Number(Math.random().toFixed(1)) * 10,
        classLoading: 10 + Number(Math.random().toFixed(1)) * 10,
        classUnloading: 10 + Number(Math.random().toFixed(1)) * 10,
        jvmCpuUsage: 10 + Number(Math.random().toFixed(1)) * 10,
      }
    }
    if (!config.params) {
      time_c += 20000
      return [200, {
        data: [{
          time: moment(new Date(time_c)).format("mm:ss"),
          ...monitor()
        }]
      }]
    }
    return [200, {
      data: Array.from({ length: 25 }, function (_, index) {
        return {
          time: moment(new Date(index * 50000)).format("mm:ss"),
          ...monitor()
        }
      })
    }]
  })
  mock.onGet("/argus/api/monitor/MemoryPool").reply(200, {
    data: Array.from({ length: 20 }, function (_, index) {
      return { name: "argus vpc" + index, max: 419, used: 200, init: 30, committed: 3 }
    })
  })
  mock.onPost('/argus-user/api/user/logout').reply(200)
  mock.onPost('/argus-user/api/user/login').reply(200)
  const script = {
    script_name: "abc.sh",
    owner: "张三",
    submit_info: "xxx",
    create_time: "2021-01-01 00:00:00",
    comment: "脚本不规范",
    has_pwd: "是",
    pwd_from: "本地",
    param: "a,b",
    content: `#!/bin/bash
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
echo "Hello World !"
`
  }
  mock.onGet('/argus-emergency/api/script').reply(function (config) {
    return [200, {
      data: Array.from({ length: config.params.pageSize }, function (_, index) {
        return {
          ...script,
          script_id: index,
          script_name: index + "run.sh",
          status: ["approved", "approving", "unapproved", "unapproved"][index % 4],
          status_label: ["已审核", "待审核", "新增", "拒绝"][index % 4],
          type: ["shell", "other"][index % 3 % 2]
        }
      }),
      total: 11
    }]
  });
  mock.onDelete('/argus-emergency/api/script').reply(200, { msg: "删除失败" })
  mock.onPost('/argus-emergency/api/script').reply(200)
  mock.onPut('/argus-emergency/api/script').reply(200)
  mock.onPost('/argus-emergency/api/script/debug').reply(200, {
    data: {
      debug_id: 5
    }
  })
  mock.onPost('/argus-emergency/api/script/debugStop').reply(200)
  let line = 0
  mock.onGet("/argus-emergency/api/script/debugLog").reply(function () {
    if (line < 10) {
      line++
      return [200, {
        data: [line + "行"],
        line
      }]
    } else {
      return [200, {
        data: ["最后一行"],
      }]
    }

  })
  mock.onPost('/argus-emergency/api/script/submitReview').reply(200)
  mock.onPost('/argus-emergency/api/script/approve').reply(200)
  mock.onGet('/argus-emergency/api/script/get').reply(200, {
    data: script
  })
  mock.onGet('/argus-emergency/api/script/search').reply(function (config) {
    if (config.params.value) {
      return [200, {
        data: [config.params.value + ".sh"]
      }]
    }
    return [200, {
      data: ["1.sh"]
    }]
  })
  mock.onGet('/argus-emergency/api/script/getByName').reply(200, {
    data: script
  })
  mock.onPost('/argus-emergency/api/script/orchestrate').reply(200, {
    data: {
      script_id: 1
    }
  })
  const orchestrate = {
    data: {
      tree: {
        key: "9639388182803-Root", children: [
          { key: "9639388182804-BeforeProcess" },
          { key: "9639388182805-BeforeThread" },
          { key: "9639388182806-TransactionController" },
          { key: "9639388182807-AfterProcess" },
          { key: "9639388182808-AfterThread" },
          { key: "9639388182809-TestFunc" },
          { key: "9639388182810-HTTPRequest" },
          { key: "9639388182811-Before" },
          { key: "9639388182812-After" },
        ]
      },
      map: {
        "9639388182803-Root": {
          title: "脚本",
          sampling_interval: 2,
          sampling_ignore: 0,
        },
        "9639388182804-BeforeProcess": { title: "@BeforeProcess" },
        "9639388182805-BeforeThread": { title: "@BeforeThread" },
        "9639388182809-TestFunc": { title: "@Test" },
        "9639388182806-TransactionController": { title: "TransactionController" },
        "9639388182807-AfterProcess": { title: "@AfterProcess" },
        "9639388182808-AfterThread": { title: "@AfterThread" },
        "9639388182810-HTTPRequest": { title: "HTTPRequest" },
        "9639388182811-Before": { title: "@Before" },
        "9639388182812-After": { title: "@After" }
      }
    }
  }
  mock.onGet('/argus-emergency/api/script/orchestrate/get').reply(200, orchestrate)
  mock.onPut('/argus-emergency/api/script/orchestrate').reply(200)
  mock.onGet('/argus-emergency/api/script/argus/orchestrate').reply(200, orchestrate)
  mock.onPut('/argus-emergency/api/script/argus/orchestrate').reply(200, {
    data: {
      script: "print('hello')"
    }
  })
  const expand = [
    { key: 1, scena_no: "C01", scena_name: "场景一", channel_type: "SSH", script_name: "C01.sh", submit_info: "提交信息" },
    { key: 2, scena_no: "C01", scena_name: "场景一", task_no: "C01T01", task_name: "任务一", channel_type: "SSH", script_name: "C01T01.sh", submit_info: "提交信息" },
    { key: 3, scena_no: "C01", scena_name: "场景一", task_no: "C01T01", task_name: "任务一", subtask_no: "C01T01S01", subtask_name: "子任务一", channel_type: "SSH", script_name: "C01T01S01.sh", submit_info: "提交信息" },
  ]
  mock.onGet("/argus-emergency/api/plan").reply(200, {
    data: Array.from({ length: 10 }, function (_, index) {
      return {
        plan_id: index,
        plan_no: "CP0" + index,
        plan_name: "A机房XX",
        status: ["approved", "approving", "unapproved", "unapproved", "running", "ran", "ran", "wait"][index % 8],
        status_label: ["已审核", "待审核", "新增", "拒绝", "运行中", "成功", "失败", "预约"][index % 8],
        create_time: "2021-01-01 00:00:00",
        creator: "z30008585",
        comment: "备注，备注",
        history_id: 1,
        expand
      }
    }),
    total: 11
  })
  mock.onGet("/argus-emergency/api/plan/get").reply(200, {
    data: {
      plan_no: "CP001",
      plan_name: "A机房XX",
    }
  })
  mock.onGet("/argus-emergency/api/plan/search/status_label").reply(200, {
    data: ["审核中"]
  })
  mock.onPost("/argus-emergency/api/plan").reply(200, {
    data: {
      plan_id: 1
    }
  })
  mock.onPost("/argus-emergency/api/plan/copy").reply(200, {
    data: {
      plan_id: 1
    }
  })
  mock.onPost('/argus-emergency/api/plan/approve').reply(200)
  mock.onPost("/argus-emergency/api/plan/task").reply(function () {
    return [200, {
      data: {
        key: (Math.random() * 10000).toFixed(0),
        submit_info: "提交信息"
      }
    }]
  })
  mock.onPost("/argus-emergency/api/plan/run").reply(200, {
    data: {
      history_id: 1
    }
  })
  mock.onPost("/argus-emergency/api/plan/cancel").reply(200)
  mock.onPut("/argus-emergency/api/plan").reply(200)
  mock.onGet("/argus-emergency/api/plan/task").reply(200, {
    data: [{
      key: 1,
      title: "任务1，长文本长文本长文本长文本长文本长文本长文本",
      task_no: 1,
      task_name: "任务1",
      channel_type: "SSH",
      script_name: "a.sh",
      submit_info: "xxx",
      sync: "同步",
      children: [{
        key: 2,
        title: "任务2",
        task_no: 2,
        task_name: "任务2",
        channel_type: "SSH",
        script_name: "a.sh",
        submit_info: "xxx",
        sync: "同步",
        children: [{
          key: 3,
          title: "任务3",
          task_no: 3,
          task_name: "任务3",
          channel_type: "SSH",
          script_name: "a.sh",
          submit_info: "xxx",
          sync: "同步",
        },
        {
          key: 4,
          title: "任务4，长文本长文本长文本长文本长文本长文本",
          task_no: 4,
          task_name: "任务4",
          channel_type: "SSH",
          script_name: "a.sh",
          submit_info: "xxx",
          sync: "同步",
          children: [{
            key: 6,
            title: "任务6，长文本长文本长文本长文本长文本长文本",
            task_no: 6,
            task_name: "任务6",
            channel_type: "SSH",
            script_name: "a.sh",
            submit_info: "xxx",
            sync: "同步",
          }]
        }]
      }]
    },
    {
      key: 5,
      title: "任务5",
      task_no: 5,
      task_name: "任务5",
      channel_type: "SSH",
      script_name: "a.sh",
      submit_info: "xxx",
      sync: "同步",
    }]
  })
  const user = {
    nickname: "张三",
    username: "zhangsan",
    role: "管理员",
    update_time: "2021-01-01 00:00:00",
    auth: ["admin", "approver", "operator"] // admin, approver, operator
  }
  // mock.onGet('/argus-user/api/user/me').replyOnce(500)
  mock.onGet('/argus-user/api/user/me').reply(200, {
    data: user
  })
  mock.onGet('/argus-user/api/user').reply(200, {
    data: Array.from({ length: 10 }, function (_, index) {
      return {
        ...user,
        username: "zhangsan" + index,
        status: ["正常", "失效"][index % 2],
        role: ["管理员", "操作员", "审核员"][index % 3]
      }
    }),
    total: 11
  })
  mock.onPost('/argus-user/api/user').reply(200, {
    data: {
      username: "zhangsan",
      password: "Adxe12xdrf"
    }
  })
  mock.onPost("/argus-user/api/user/chagnePwd").reply(200)
  mock.onPost("/argus-user/api/user/resetPwd").reply(200, {
    data: {
      username: "zhangsan",
      password: "Adxe12xdrf"
    }
  })
  mock.onPost("/argus-user/api/user/registe").reply(200)
  mock.onPost("/argus-user/api/user/batchActive").reply(200)
  // 不能禁用自己和admin
  mock.onPost("/argus-user/api/user/batchDeactive").reply(200)

  mock.onGet("/argus-emergency/api/history").reply(200, {
    data: Array.from({ length: 10 }, function (_, index) {
      return {
        history_id: index,
        plan_name: "预案名称",
        status: ["运行中", "成功", "失败", "终止"][index % 4],
        creator: "z30008585",
        start_time: "2021-01-01 00:00:00",
        execute_time: "10:00"
      }
    }),
    total: 11
  })
  mock.onGet("/argus-emergency/api/history/scenario").reply(200, {
    data: Array.from({ length: 4 }, function (_, index) {
      return {
        key: "key" + index,
        scena_name: "A机房分流，长文本长文本长文本长文本长文本长文本长文本长文本",
        scena_id: "id" + index,
        status: ['error', 'process', 'finish', 'wait'][index % 4],
        status_label: ["失败", "运行中", "成功", "待执行"][index % 4]
      }
    })
  })
  mock.onGet("/argus-emergency/api/history/scenario/task").reply(function () {
    return [200, {
      data: Array.from({ length: 4 }, function (_, index) {
        return {
          key: "T" + index,
          task_no: index,
          task_id: index,
          task_name: "A机房分流" + Math.random(),
          operator: "z30008585",
          start_time: "2021-01-01 00:00:00",
          end_time: "2021-01-01 00:00:00",
          sync: "同步",
          status: ['error', 'process', 'finish', 'wait'][index % 4],
          status_label: ["失败", "运行中", "成功", "待执行"][index % 4]
        }
      })
    }]
  })
  mock.onPost("/argus-emergency/api/history/scenario/task/runAgain").reply(200)
  mock.onPost("/argus-emergency/api/history/scenario/task/ensure").reply(200)
  mock.onGet("/argus-emergency/api/history/scenario/task/log").reply(function () {
    line++
    return [200, {
      data: ["第一行"],
      line
    }]
  })
  const hosts = Array.from({ length: 11 }, function (_, index) {
    return {
      status: ["running", "pending", "success", "fail"][index % 4],
      status_label: ["运行中", "准备中", "成功", "失败"][index % 4],
      server_id: index,
      server_name: "服务名称" + index,
      server_ip: "192.168.0.1",
      server_user: "root",
      have_password: "有",
      password_mode: "本地",
      agent_port: "19001",
      licensed: false
    }
  })
  mock.onGet("/argus-emergency/api/host").reply(function (config) {
    const excludes = config.params.excludes as number[] | undefined
    const end = (config.params.current || 1) * 5
    const data = hosts.filter(function (item) {
      return !excludes?.includes(item.server_id)
    })
    return [200, {
      data: data.slice(end - 5, end),
      total: data.length
    }]
  })
  mock.onGet("/argus-emergency/api/host/search").reply(200, {
    data: ["192.168.0.1"]
  })
  mock.onDelete("/argus-emergency/api/host").reply(200)
  mock.onPost("/argus-emergency/api/host/license").reply(200)
  mock.onPost("/argus-emergency/api/host").reply(200)
  mock.onPost("/argus-emergency/api/host/stop").reply(200)
  mock.onGet("/argus-emergency/api/host/search/password_uri").reply(200, {
    data: ["root@192.168.0.1"]
  })
}