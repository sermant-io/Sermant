<template>
  <div style="width: 100%">
    <el-page-header :title="$t('eventViews.instance')" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> {{ $t('eventViews.eventMonitoring') }} </span>
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
            <p>{{ $t('eventViews.emergencyEvent') }}</p>
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
            <p>{{ $t('eventViews.importantEvent') }}</p>
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
            <p>{{ $t('eventViews.normalEvent') }}</p>
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
              {{ $t('eventViews.service') }}: {{ tag }}
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
                  <arrow-down/>
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="currentOption = 0">{{ $t('eventViews.service') }}</el-dropdown-item>
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
            :start-placeholder="$t('eventViews.startTime')"
            :end-placeholder="$t('eventViews.endTime')"
            value-format="x"
        />
      </el-form-item>
      <el-form-item>
        <el-button size="large" @click="getEvents">
          <el-icon>
            <Search/>
          </el-icon>
        </el-button>
        <el-button
            size="large"
            @click="changeAutoRefreshState"
            :type="autoRefresh ? 'success' : ''"
            :loading="autoLoadingState"
        >{{ $t('eventViews.autoRefresh') }}
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
            <el-descriptions class="margin-top" :title="$t('eventViews.logDetail')" :column="3" border>
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.logLevel') }}</template>
                {{ props.row.info.logLevel }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.logInformation') }}</template>
                {{ props.row.info.logMessage }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.logRelatedClass') }}</template>
                {{ props.row.info.logClass }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.logRelatedMethod') }}</template>
                {{ props.row.info.logMethod }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.logLine') }}</template>
                {{ props.row.info.logLineNumber }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.threadID') }}</template>
                {{ props.row.info.logThreadId }}
              </el-descriptions-item>
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.exceptionType') }}</template>
                {{ props.row.info.logThrowable }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-if="props.row.type !== 'log'" m="4">
            <el-descriptions
                class="margin-top"
                :title="$t('eventViews.eventDetail')"
                :column="3"
                :size="'large'"
                border
            >
              <el-descriptions-item>
                <template #label> {{ $t('eventViews.description') }}</template>
                {{ props.row.info.description }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="eventInfo.name" :label="$t('eventViews.event')" width="150">
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
          :label="$t('eventViews.level')"
          :filters="eventLevel"
          width="150"
      >
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <span v-if="scope.row.level === 'emergency'">
              <el-tag class="ml-2" type="danger">{{ $t('eventViews.emergency') }}</el-tag>
            </span>
            <span v-if="scope.row.level === 'important'">
              <el-tag class="ml-2" type="warning">{{ $t('eventViews.important') }}</el-tag>
            </span>
            <span v-if="scope.row.level === 'normal'">
              <el-tag class="ml-2" type="info">{{ $t('eventViews.normal') }}</el-tag>
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
          prop="type"
          column-key="type"
          :label="$t('eventViews.type')"
          :filters="eventType"
          width="150"
      >
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <span v-if="scope.row.type === 'log'"> {{ $t('eventViews.log') }} </span>
            <span v-if="scope.row.type === 'operation'">{{ $t('eventViews.operation') }}</span>
            <span v-if="scope.row.type === 'governance'">{{ $t('eventViews.governance') }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column
          column-key="service"
          prop="meta.service"
          :label="$t('eventViews.service')"
          width="150"
      />
      <el-table-column column-key="ip" prop="meta.ip" label="IP" width="150"/>
      <el-table-column column-key="scope" prop="scope" :label="$t('eventViews.area')">
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
      <el-table-column prop="time" :label="$t('eventViews.time')">
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
          :current-page="displayState.currentPage"
          @current-change="pageChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, reactive, ref, watch} from "vue";
import {useRouter} from "vue-router";
import qs from "qs";
import moment from "moment";
import axios from "axios";
import i18n from "~/composables/translations";

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
const searchOption = ref([i18n.global.t('eventViews.service'), "IP"]);
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
  {text: i18n.global.t('eventViews.emergency'), value: "emergency"},
  {text: i18n.global.t('eventViews.important'), value: "important"},
  {text: i18n.global.t('eventViews.normal'), value: "normal"},
]);

const eventType = ref([
  {text: i18n.global.t('eventViews.log'), value: "log"},
  {text: i18n.global.t('eventViews.governance'), value: "governance"},
  {text: i18n.global.t('eventViews.operation'), value: "operation"},
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
const shortcuts = ref([
  {
    text: i18n.global.t('eventViews.lastHalfHour'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 0.5);
      return [start, end];
    },
  },
  {
    text: i18n.global.t('eventViews.lastHour'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 1);
      return [start, end];
    },
  },
  {
    text: i18n.global.t('eventViews.lastDay'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24);
      return [start, end];
    },
  },
  {
    text: i18n.global.t('eventViews.lastThreeDays'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
      return [start, end];
    },
  },
  {
    text: i18n.global.t('eventViews.lastWeek'),
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
      return [start, end];
    },
  },
]);

// 自动刷新事件列表
const autoRefresh = ref(false);

const autoLoadingState = ref(false);

const autoRefreshTimer = ref(0);

const changeAutoRefreshState = () => {
  if (autoRefresh.value) {
    ElMessage({
      message: i18n.global.t('eventViews.disableAutoRefresh'),
      type: "info",
    });
    autoRefresh.value = false;
    clearAutoRefreshTimer();
  } else {
    ElMessage({
      message: i18n.global.t('eventViews.enableAutoRefresh'),
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
  }, 30000);
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

const events = reactive({events: []});

// 在此处添加事件
const eventName = reactive({
  // Framework事件
  SERMANT_START: i18n.global.t('eventViews.agentStartup'),
  SERMANT_STOP: i18n.global.t('eventViews.agentShutdown'),
  SERMANT_TRANSFORM_SUCCESS: i18n.global.t('eventViews.enhanceSuccess'),
  SERMANT_TRANSFORM_FAILURE: i18n.global.t('eventViews.enhanceFail'),
  SERMANT_SERVICE_STOP: i18n.global.t('eventViews.serviceStop'),
  SERMANT_SERVICE_START: i18n.global.t('eventViews.serviceStart'),
  SERMANT_PLUGIN_LOAD: i18n.global.t('eventViews.pluginInstall'),
  // 日志事件
  WARNING: i18n.global.t('eventViews.warningLog'),
  SEVERE: i18n.global.t('eventViews.errorLog'),
  warning: i18n.global.t('eventViews.warningLog'),
  severe: i18n.global.t('eventViews.errorLog'),
  // Flowcontrol事件
  TRAFFIC_LIMITING: i18n.global.t('eventViews.rateLimiting'),
  CIRCUIT_BREAKER: i18n.global.t('eventViews.circuitBreaker'),
  ADAPTIVE_OVERLOAD_PROTECTION: i18n.global.t('eventViews.adaptiveOverloadProtection'),
  // ServiceRegistry事件
  GRACEFUL_ONLINE_BEGIN: i18n.global.t('eventViews.gracefulOnlineBegin'),
  GRACEFUL_ONLINE_END: i18n.global.t('eventViews.gracefulOnlineEnd'),
  GRACEFUL_OFFLINE_BEGIN: i18n.global.t('eventViews.gracefulOfflineBegin'),
  GRACEFUL_OFFLINE_END: i18n.global.t('eventViews.gracefulOfflineEnd'),
  // 路由插件事件
  ROUTER_RULE_REFRESH: i18n.global.t('eventViews.routerRuleRefresh'),
  SAME_TAG_RULE_MATCH: i18n.global.t('eventViews.sameTagRuleMatch'),
  SAME_TAG_RULE_MISMATCH: i18n.global.t('eventViews.sameTagRuleMismatch'),
  INSTANCE_REMOVAL: i18n.global.t('eventViews.instanceRemoval'),
  INSTANCE_RECOVERY: i18n.global.t('eventViews.instanceRecovery'),
  SPRINGBOOT_REGISTRY: i18n.global.t('eventViews.springbootRegistry'),
  SPRINGBOOT_UNREGISTRY: i18n.global.t('eventViews.springbootUnRegistry'),
  SPRINGBOOT_GRAY_CONFIG_REFRESH: i18n.global.t('eventViews.springbootGrayConfigRefresh'),
});

const displayState = reactive({
  currentPage: 1,
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
  displayState.currentPage = pageNubmber;
  axios
      .get(`${window.location.origin}/sermant/event/events/page`, {
        params: {page: pageNubmber},
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
          serialize: (params) => qs.stringify(params, {arrayFormat: "repeat"}),
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
        displayState.currentPage = 1;
        ElMessage({
          message: i18n.global.t('eventViews.successfullyObtainedEvents'),
          type: "success",
        });
      })
      .catch(function (error) {
        console.log(error);
        ElMessage({
          message: i18n.global.t('eventViews.failedToObtainEvents'),
          type: "error",
        });
      });
};

watch(() => i18n.global.locale, (newLocale, oldLocale) => {
  searchOption.value = [i18n.global.t('eventViews.service'), "IP"];
  eventLevel.value = [
    {text: i18n.global.t('eventViews.emergency'), value: "emergency"},
    {text: i18n.global.t('eventViews.important'), value: "important"},
    {text: i18n.global.t('eventViews.normal'), value: "normal"},
  ];
  eventType.value = [
    {text: i18n.global.t('eventViews.log'), value: "log"},
    {text: i18n.global.t('eventViews.governance'), value: "governance"},
    {text: i18n.global.t('eventViews.operation'), value: "operation"},
  ];
  eventName.SERMANT_START=i18n.global.t('eventViews.agentStartup');
  eventName.SERMANT_STOP=i18n.global.t('eventViews.agentShutdown');
  eventName.SERMANT_TRANSFORM_SUCCESS=i18n.global.t('eventViews.enhanceSuccess');
  eventName.SERMANT_TRANSFORM_FAILURE=i18n.global.t('eventViews.enhanceFail');
  eventName.SERMANT_SERVICE_STOP=i18n.global.t('eventViews.serviceStop');
  eventName.SERMANT_SERVICE_START=i18n.global.t('eventViews.serviceStart');
  eventName.SERMANT_PLUGIN_LOAD=i18n.global.t('eventViews.pluginInstall');
  eventName.WARNING=i18n.global.t('eventViews.warningLog');
  eventName.SEVERE=i18n.global.t('eventViews.errorLog');
  eventName.warning=i18n.global.t('eventViews.warningLog');
  eventName.severe=i18n.global.t('eventViews.errorLog');
  eventName.TRAFFIC_LIMITING=i18n.global.t('eventViews.rateLimiting');
  eventName.CIRCUIT_BREAKER=i18n.global.t('eventViews.circuitBreaker');
  eventName.ADAPTIVE_OVERLOAD_PROTECTION=i18n.global.t('eventViews.adaptiveOverloadProtection');
  eventName.GRACEFUL_ONLINE_BEGIN=i18n.global.t('eventViews.gracefulOnlineBegin');
  eventName.GRACEFUL_ONLINE_END=i18n.global.t('eventViews.gracefulOnlineEnd');
  eventName.GRACEFUL_OFFLINE_BEGIN=i18n.global.t('eventViews.gracefulOfflineBegin');
  eventName.GRACEFUL_OFFLINE_END=i18n.global.t('eventViews.gracefulOfflineEnd');
  eventName.ROUTER_RULE_REFRESH=i18n.global.t('eventViews.routerRuleRefresh');
  eventName.SAME_TAG_RULE_MATCH=i18n.global.t('eventViews.sameTagRuleMatch');
  eventName.SAME_TAG_RULE_MISMATCH=i18n.global.t('eventViews.sameTagRuleMismatch');
  eventName.INSTANCE_REMOVAL=i18n.global.t('eventViews.instanceRemoval');
  eventName.INSTANCE_RECOVERY=i18n.global.t('eventViews.instanceRecovery');
  eventName.SPRINGBOOT_REGISTRY=i18n.global.t('eventViews.springbootRegistry');
  eventName.SPRINGBOOT_UNREGISTRY=i18n.global.t('eventViews.springbootUnRegistry');
  eventName.SPRINGBOOT_GRAY_CONFIG_REFRESH=i18n.global.t('eventViews.springbootGrayConfigRefresh');
  shortcuts.value = [
    {
      text: i18n.global.t('eventViews.lastHalfHour'),
      value: () => {
        const end = new Date();
        const start = new Date();
        start.setTime(start.getTime() - 3600 * 1000 * 0.5);
        return [start, end];
      },
    },
    {
      text: i18n.global.t('eventViews.lastHour'),
      value: () => {
        const end = new Date();
        const start = new Date();
        start.setTime(start.getTime() - 3600 * 1000 * 1);
        return [start, end];
      },
    },
    {
      text: i18n.global.t('eventViews.lastDay'),
      value: () => {
        const end = new Date();
        const start = new Date();
        start.setTime(start.getTime() - 3600 * 1000 * 24);
        return [start, end];
      },
    },
    {
      text: i18n.global.t('eventViews.lastThreeDays'),
      value: () => {
        const end = new Date();
        const start = new Date();
        start.setTime(start.getTime() - 3600 * 1000 * 24 * 3);
        return [start, end];
      },
    },
    {
      text: i18n.global.t('eventViews.lastWeek'),
      value: () => {
        const end = new Date();
        const start = new Date();
        start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
        return [start, end];
      },
    },
  ];
  eventName
});
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
