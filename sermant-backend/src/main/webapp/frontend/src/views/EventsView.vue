<template>
  <div style="width: 100%">
    <el-page-header :title="'实例'" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> 事件监测 </span>
      </template>
    </el-page-header>
    <el-row class="row-bg" justify="space-around">
      <el-col :span="8">
        <div class="grid-content">
          <el-card
            shadow="hover"
            style="color: #f56c6c; font-weight: 900"
            class="count-card"
          >
            <p>紧急事件</p>
            <h1>{{ eventCount.emergency }}</h1>
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <el-card
            shadow="hover"
            style="color: #e6a23c; font-weight: 900"
            class="count-card"
          >
            <p>重要事件</p>
            <h1>{{ eventCount.important }}</h1>
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <el-card
            shadow="hover"
            style="color: #409eff; font-weight: 900"
            class="count-card"
          >
            <p>一般事件</p>
            <h1>{{ eventCount.normal }}</h1>
          </el-card>
        </div>
      </el-col>
    </el-row>
    <el-form :model="requestParam" :inline="true">
      <el-form-item style="width: 50%">
        <el-input
          size="large"
          :placeholder="searchOption[currentOption]"
          v-model="inputValue"
          autosize
          @change="handleInputConfirm()"
        >
          <template #prefix>
            <el-tag
              v-for="tag in requestParam.service"
              :key="tag"
              class="mx-1"
              closable
              :disable-transitions="false"
              @close="handleCloseServiceTag(tag)"
            >
              服务: {{ tag }}
            </el-tag>
            <el-tag
              v-for="tag in requestParam.ip"
              :key="tag"
              class="mx-1"
              closable
              :disable-transitions="false"
              @close="handleCloseIpTag(tag)"
            >
              IP: {{ tag }}
            </el-tag>
          </template>
          <template #prepend>
            <el-dropdown>
              <span class="el-dropdown-link">
                <el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="currentOption = 0">服务</el-dropdown-item>
                  <el-dropdown-item @click="currentOption = 1">IP</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-date-picker
          size="large"
          v-model="timeRange.timeRange"
          type="datetimerange"
          :shortcuts="shortcuts"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="x"
        />
      </el-form-item>
      <el-form-item>
        <el-button size="large" @click="getEvents">
          <el-icon><Search /></el-icon>
        </el-button>
        <el-button
          size="large"
          @click="changeAutoRefreshState"
          :type="autoRefresh ? 'success' : ''"
          :loading="autoLoadingState"
          >自动刷新
        </el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="events.events"
      :default-sort="{ prop: 'time', order: 'descending' }"
      style="width: 100%"
      border
      @filter-change="filterChange"
    >
      <el-table-column type="expand">
        <template #default="props">
          <div v-if="props.row.type === 'log'" m="4">
            <el-descriptions class="margin-top" title="日志详情" :column="3" border>
              <el-descriptions-item>
                <template #label> 日志级别 </template>
                {{ props.row.info.logLevel }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> 日志信息 </template>
                {{ props.row.info.logMessage }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> 日志触发类 </template>
                {{ props.row.info.logClass }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> 日志触发方法 </template>
                {{ props.row.info.logMethod }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> 日志触发行号 </template>
                {{ props.row.info.logLineNumber }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> 线程ID </template>
                {{ props.row.info.logThreadId }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> 异常类型 </template>
                {{ props.row.info.throwable }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-if="props.row.type !== 'log'" m="4">
            <el-descriptions
              class="margin-top"
              title="事件详情"
              :column="3"
              :size="'large'"
              border
            >
              <el-descriptions-item>
                <template #label> 描述信息 </template>
                {{ props.row.info.description }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="eventInfo.name" label="事件" width="150">
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <span v-if="scope.row.type === 'log'">
              {{ eventName[scope.row.info.logLevel] }}
            </span>
            <span v-if="scope.row.type !== 'log'">{{
              eventName[scope.row.info.name]
            }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="level"
        column-key="level"
        label="事件级别"
        :filters="eventLevel"
        width="150"
      >
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <span v-if="scope.row.level === 'emergency'">
              <el-tag class="ml-2" type="danger">紧急</el-tag>
            </span>
            <span v-if="scope.row.level === 'important'">
              <el-tag class="ml-2" type="warning">重要</el-tag>
            </span>
            <span v-if="scope.row.level === 'normal'">
              <el-tag class="ml-2" type="info">一般</el-tag>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="type"
        column-key="type"
        label="事件类型"
        :filters="eventType"
        width="150"
      >
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <span v-if="scope.row.type === 'log'"> 日志 </span>
            <span v-if="scope.row.type === 'operation'">运行</span>
            <span v-if="scope.row.type === 'governance'">治理</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        column-key="service"
        prop="meta.service"
        label="服务"
        width="150"
      />
      <el-table-column column-key="ip" prop="meta.ip" label="IP" width="150" />
      <el-table-column column-key="scope" prop="scope" label="事件区域">
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <span v-if="scope.row.type !== 'log'">
              {{ scope.row.scope }}
            </span>
            <span v-if="scope.row.type === 'log'">
              {{ logScope(scope.row.info.logClass, scope.row.info.logMethod) }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="time" label="上报时间">
        <template #default="scope">
          <div style="display: flex; align-items: center">
            {{ time(scope.row.time) }}
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-div">
      <el-pagination
        layout="prev, pager, next"
        :page-count="displayState.totalPage"
        @current-change="pageChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onBeforeMount } from "vue";
import { useRouter } from "vue-router";
import qs from "qs";
import moment from "moment";
import axios from "axios";

onBeforeMount(() => {
  const time = new Date().getTime();
  const range = [time - 3600 * 1000 * 0.5, time];
  timeRange.timeRange = range;

  getEvents();
});

// 路由
const router = useRouter();

const goBack = () => {
  router.push("/");
};

// 格式转换
const time = (value) => {
  return moment(value).format("YYYY-MM-DD HH:mm:ss");
};

const logScope = (classNmae, method) => {
  const stringArray = classNmae.split(".");
  const lastElement = stringArray[stringArray.length - 1];
  return lastElement + "::" + method;
};

// 处理筛选标签
const searchOption = ref(["服务", "IP"]);
const currentOption = ref(0);
const inputValue = ref("");

// 过滤参数
const handleCloseServiceTag = (tag: string) => {
  requestParam.service.splice(requestParam.service.indexOf(tag), 1);
  getEvents();
};

const handleCloseIpTag = (tag: string) => {
  requestParam.ip.splice(requestParam.ip.indexOf(tag), 1);
  getEvents();
};

const handleInputConfirm = () => {
  if (inputValue.value === "") {
    return;
  }
  if (inputValue.value) {
    if (currentOption.value === 0) {
      requestParam.service.push(inputValue.value);
    }
    if (currentOption.value === 1) {
      requestParam.ip.push(inputValue.value);
    }
  }
  inputValue.value = "";
  getEvents();
};

const eventLevel = ref([
  { text: "紧急", value: "emergency" },
  { text: "重要", value: "important" },
  { text: "一般", value: "normal" },
]);

const eventType = ref([
  { text: "日志", value: "log" },
  { text: "治理", value: "governance" },
  { text: "运行", value: "operation" },
]);

const filterChange = (event: any) => {
  if (typeof event.type != "undefined") {
    requestParam.type = event.type;
    getEvents();
  }
  if (typeof event.level != "undefined") {
    requestParam.level = event.level;
    getEvents();
  }
};

// 时间选择快照
const shortcuts = [
  {
    text: "最近半小时",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 0.5);
      return [start, end];
    },
  },
  {
    text: "最近一小时",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 1);
      return [start, end];
    },
  },
  {
    text: "最近一天",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24);
      return [start, end];
    },
  },
  {
    text: "最近三天",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
      return [start, end];
    },
  },
  {
    text: "最近一周",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      return [start, end];
    },
  },
];

// 自动刷新事件列表
const autoRefresh = ref(false);

const autoLoadingState = ref(false);

const autoRefreshTimer = ref(0);

const changeAutoRefreshState = () => {
  if (autoRefresh.value) {
    ElMessage({
      message: "关闭自动刷新",
      type: "info",
    });
    autoRefresh.value = false;
    clearAutoRefreshTimer();
  } else {
    ElMessage({
      message: "开启自动刷新",
      type: "info",
    });
    autoRefresh.value = true;
    createAutoRefreshTimer();
  }
};

const createAutoRefreshTimer = () => {
  autoRefreshTimer.value = window.setInterval(function autoRefresh() {
    autoLoadingState.value = true;

    // 默认刷新当前半小时
    const time = new Date().getTime();
    const range = [time - 3600 * 1000 * 0.5, time];
    timeRange.timeRange = range;

    getEvents();
    autoLoadingState.value = false;
  }, 3000);
};

const clearAutoRefreshTimer = () => {
  window.clearInterval(autoRefreshTimer.value);
};
const closeAutoRefresh = () => {
  if (autoRefresh.value) {
    autoRefresh.value = false;
    clearAutoRefreshTimer();
  }
};

// 请求数据
const eventCount: {
  emergency: number;
  important: number;
  normal: number;
} = reactive({
  emergency: 0,
  important: 0,
  normal: 0,
});

interface event {
  meta: {
    service: string;
    ip: string;
  };
  time: number;
  scope: string;
  level: string;
  type: string;
  eventInfo: {
    name: string;
    description: string;
  };
  logInfo: {
    logLevel: string;
    logMessage: string;
    logClass: string;
    logMethod: string;
    logLineNumber: string;
    logThreadId: string;
    throwable: string;
  };
}

interface eventsResponse {
  eventCount: eventCount;
  events: event[];
  totalPage: number;
}

const events = reactive({ events: [] });

// 在此处添加事件
const eventName = reactive({
  // Framework事件
  SERMANT_START: "Agent启动",
  SERMANT_STOP: "Agent停止",
  SERMANT_TRANSFORM_SUCCESS: "增强成功",
  SERMANT_TRANSFORM_FAILURE: "增强失败",
  SERMANT_SERVICE_STOP: "服务停止",
  SERMANT_SERVICE_START: "服务启动",
  SERMANT_PLUGIN_LOAD: "插件加载",
  // 日志事件
  WARNING: "警告日志",
  SEVERE: "错误日志",
  warning: "警告日志",
  severe: "错误日志",
  // Flowcontrol事件
  TRAFFIC_LIMITING: "限流",
  CIRCUIT_BREAKER: "熔断",
  ADAPTIVE_OVERLOAD_PROTECTION: "自适应过载保护",
  // ServiceRegistry事件
  GRACEFUL_ONLINE_BEGIN: "无损上线开始",
  GRACEFUL_ONLINE_END: "无损上线结束",
  GRACEFUL_OFFLINE_BEGIN: "无损下线开始",
  GRACEFUL_OFFLINE_END: "无损下线结束",
  // 路由插件事件
  ROUTER_RULE_TAKE_EFFECT: "路由插件规则生效",
  INSTANCE_REMOVAL: "实例摘除",
  INSTANCE_RECOVERY: "实例恢复"
});

const displayState = reactive({
  totalPage: 1,
  pageSize: 10,
});

const timeRange = reactive({
  timeRange: [new Date().getTime() - 3600 * 1000 * 0.5, new Date().getTime()],
});

const requestParam = reactive({
  service: [],
  ip: [],
  scop: [],
  type: [],
  level: [],
  startTime: timeRange.timeRange[0],
  endTime: timeRange.timeRange[1],
});

const pageChange = (pageNubmber: number) => {
  axios
    .get(`${window.location.origin}/sermant/event/events/page`, {
      params: { page: pageNubmber },
    })
    .then(function (response) {
      const data = response.data;
      events.events = data.events;
    });
  closeAutoRefresh();
};

const getEvents = () => {
  if (timeRange.timeRange != null) {
    requestParam.startTime = timeRange.timeRange[0];
    requestParam.endTime = timeRange.timeRange[1];
  }
  axios
    .get(`${window.location.origin}/sermant/event/events`, {
      params: requestParam,
      paramsSerializer: {
        serialize: (params) => qs.stringify(params, { arrayFormat: "repeat" }),
      },
    })
    .then(function (response) {
      const data = response.data;
      // 更新统计总数
      eventCount.emergency = data.eventCount.emergency;
      eventCount.important = data.eventCount.important;
      eventCount.normal = data.eventCount.normal;
      events.events = data.events;
      displayState.totalPage = data.totalPage;
      ElMessage({
        message: "获取事件成功",
        type: "success",
      });
    })
    .catch(function (error) {
      console.log(error);
      ElMessage({
        message: "获取事件失败",
        type: "error",
      });
    });
};
</script>

<style scoped>
.ep-row {
  margin-top: 15px;
  margin-bottom: 10px;
}

.count-card {
  margin: 5px;
  width: 100%;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  flex-direction: column;
}

.grid-content {
  text-align: center;
  display: flex;
  justify-content: center;
}

.pagination-div {
  margin: 5px;
  display: flex;
  justify-content: space-around;
}
</style>
