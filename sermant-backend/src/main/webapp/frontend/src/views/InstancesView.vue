<template>
  <div style="width: 100%">
    <el-page-header :title="'事件'" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> 实例状态 </span>
      </template>
    </el-page-header>
    <el-row justify="space-around">
      <el-col :span="8">
        <div class="grid-content">
          <el-card shadow="hover" class="count-card">
            <p>应用</p>
            <h1>{{ tagCount.applicationCount.length }}</h1>
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <el-card shadow="hover" class="count-card">
            <p>服务</p>
            <h1>{{ tagCount.serviceCount.length }}</h1>
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <el-card shadow="hover" class="count-card">
            <p>实例</p>
            <h1>{{ state.total }}</h1>
          </el-card>
        </div>
      </el-col>
    </el-row>
    <el-input
      size="large"
      style="margin-bottom: 15px; width: 50%"
      :placeholder="searchOption[currentOption]"
      v-model="inputValue"
      autosize
      @change="handleInputConfirm()"
    >
      <template #prefix>
        <el-tag
          v-for="tag in serviceTag"
          :key="tag"
          class="mx-1"
          closable
          :disable-transitions="false"
          @close="handleCloseServiceTag(tag)"
        >
          服务: {{ tag }}
        </el-tag>
        <el-tag
          v-for="tag in ipTag"
          :key="tag"
          class="mx-1"
          closable
          :disable-transitions="false"
          @close="handleCloseIpTag(tag)"
        >
          IP: {{ tag }}
        </el-tag>
        <el-tag
          v-for="tag in versionTag"
          :key="tag"
          class="mx-1"
          closable
          :disable-transitions="false"
          @close="handleCloseVersionTag(tag)"
        >
          版本:{{ tag }}
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
              <el-dropdown-item @click="currentOption = 2">版本</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template #append>
        <el-button @click="search">
          <el-icon :color="'#000000'">
            <Search />
          </el-icon>
        </el-button>
      </template>
    </el-input>
    <div>
      <el-table :data="state.tableData" style="width: 100%" :border="true" stripe>
        <el-table-column label="状态" prop="health" width="100">
          <template #default="scope">
            <div class="dot normal-dot" v-if="scope.row.health"></div>
            <div class="dot abnormal-dot" v-if="!scope.row.health"></div>
          </template>
        </el-table-column>
        <el-table-column prop="service" label="服务" width="120"> </el-table-column>
        <el-table-column prop="instanceId" label="实例ID" width="320"> </el-table-column>
        <el-table-column prop="version" label="版本" width="120"> </el-table-column>
        <el-table-column prop="ipAddress" label="IP" width="180"> </el-table-column>
        <el-table-column prop="heartbeatTime" label="心跳时间" width="180">
        </el-table-column>
        <el-table-column label="插件">
          <template #default="props">
            <div class="plugin-box">
              <el-tag
                class="mx-1"
                v-for="(plugin, index) in props.row.pluginInfoMap"
                :key="index"
                >{{ plugin.name }}:{{ plugin.version }}</el-tag
              >
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-div">
        <el-pagination
          layout="prev, pager, next"
          :total="state.total"
          @current-change="pageChange"
          :current-page="state.currentPage"
        >
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, reactive, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

import moment from "moment";

// 路由
const router = useRouter();

onMounted(() => {
  getTableData();
});

const goBack = () => {
  router.push("/events");
};

// 格式转换
const time = (value) => {
  return moment(value).format("YYYY-MM-DD HH:mm:ss");
};

// 处理筛选标签
const searchOption = ref(["服务", "IP", "版本"]);
const currentOption = ref(0);
const inputValue = ref("");

// 过滤参数
const applicationTag = reactive([]);
const serviceTag = reactive([]);
const ipTag = reactive([]);
const versionTag = reactive([]);

const handleCloseApplicationTag = (tag: string) => {
  applicationTag.splice(applicationTag.indexOf(tag), 1);
  search();
};

const handleCloseServiceTag = (tag: string) => {
  serviceTag.splice(serviceTag.indexOf(tag), 1);
  search();
};

const handleCloseIpTag = (tag: string) => {
  ipTag.splice(ipTag.indexOf(tag), 1);
  search();
};

const handleCloseVersionTag = (tag: string) => {
  versionTag.splice(versionTag.indexOf(tag), 1);
  search();
};

const handleInputConfirm = () => {
  if (inputValue.value === "") {
    return;
  }
  if (inputValue.value) {
    console.log(inputValue.value);
    if (currentOption.value === 0) {
      serviceTag.push(inputValue.value);
    }
    if (currentOption.value === 1) {
      ipTag.push(inputValue.value);
    }
    if (currentOption.value === 2) {
      versionTag.push(inputValue.value);
    }
  }
  inputValue.value = "";
  search();
};

// 各标签数据统计
const tagCount = reactive({
  applicationCount: [],
  serviceCount: [],
  instanceCount: [],
});

interface TableData {
  service: string;
  heartbeatTime: string;
  instanceId: string;
  ip: string;
  lastHeartbeatTime: string;
  pluginInfoMap: any[];
  version: string;
  health: boolean;
  ipAddress: string;
}

interface InnerData {
  service: string;
  heartbeatTime: string;
  instanceId: string;
  ip: string[];
  lastHeartbeatTime: string;
  pluginInfoMap: any;
  version: string;
  health: boolean;
}

let innerData: InnerData[] = [];
let showData: InnerData[] = [];
const state: {
  total: number;
  ipAddress: string;
  version: string;
  service: string;
  tableData: TableData[];
  loading: boolean;
  currentPage: number;
  normalCount: number;
  abnormalCount: number;
  pageSize: number;
} = reactive({
  total: 0,
  ipAddress: "",
  version: "",
  service: "",
  tableData: [],
  loading: false,
  currentPage: 1,
  normalCount: 0,
  abnormalCount: 0,
  pageSize: 10,
});

async function getTableData() {
  try {
    state.loading = true;
    const res = await axios.get(`${window.location.origin}/sermant/getPluginsInfo`);
    // 清理统计服务
    tagCount.serviceCount = [];
    innerData = res.data;
    innerData.forEach((item) => {
      if (item.health) {
        state.normalCount++;
      } else {
        state.abnormalCount++;
      }
      // 统计服务数
      if (tagCount.serviceCount.indexOf(item.service) === -1) {
        tagCount.serviceCount.push(item.service);
      }
    });
    showData = JSON.parse(JSON.stringify(res.data));
    state.total = showData.length;
    const result = JSON.parse(JSON.stringify(showData.slice(0, state.pageSize)));
    state.tableData = handle(result);
  } catch (err) {
    console.log(err);
  } finally {
    state.loading = false;
  }
}
function handle(result: any[]) {
  result.forEach((item: any) => {
    if (item.ip instanceof Array) {
      item.ipAddress = item.ip.join(",");
    }
    const heartbeatDate = new Date(item.heartbeatTime);
    item.heartbeatTime = time(item.heartbeatTime);
  });
  return result;
}

function pageChange(page: number) {
  state.currentPage = page;
  if (page * state.pageSize <= showData.length) {
    const result = JSON.parse(
      JSON.stringify(showData.slice((page - 1) * state.pageSize, page * state.pageSize))
    );
    state.tableData = handle(result);
  } else {
    const result = JSON.parse(
      JSON.stringify(showData.slice((page - 1) * state.pageSize))
    );
    state.tableData = handle(result);
  }
}

function search() {
  state.loading = true;
  showData = [];
  tagCount.serviceCount = [];
  innerData.forEach((item) => {
    let isMatch = true;
    // 筛选服务
    if (serviceTag.length !== 0) {
      isMatch = isMatch && serviceTag.indexOf(item.service) !== -1;
    }
    // 筛选IP
    if (ipTag.length !== 0) {
      let containIp = false;
      for (let ip of item.ip) {
        if (ipTag.indexOf(ip) !== -1) {
          containIp = true;
          break;
        }
      }
      isMatch = isMatch && containIp;
    }
    // 筛选版本
    if (versionTag.length !== 0) {
      isMatch = isMatch && versionTag.indexOf(item.version) !== -1;
    }
    if (isMatch) {
      // 统计服务数
      if (tagCount.serviceCount.indexOf(item.service) === -1) {
        tagCount.serviceCount.push(item.service);
      }
      showData.push(item);
    }
  });
  state.total = showData.length;
  pageChange(1);
  state.loading = false;
}

function searchHealth(health: string) {
  state.loading = true;
  showData = [];
  if (health === "normal") {
    innerData.forEach((item) => {
      if (item.health) {
        showData.push(item);
      }
    });
  } else {
    innerData.forEach((item) => {
      if (!item.health) {
        showData.push(item);
      }
    });
  }
  state.total = showData.length;
  pageChange(1);
  state.loading = false;
}
</script>

<style scoped>
.count-card {
  border-radius: 10px;
  display: flex;
  justify-content: center;
  flex-direction: column;
  margin: 5px;
  width: 100%;
}
.grid-content {
  text-align: center;
  display: flex;
  justify-content: center;
}

.ep-row {
  margin-top: 15px;
  margin-bottom: 15px;
}

.pagination-div {
  margin: 5px;
  display: flex;
  justify-content: space-around;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin: 0 auto;
}

.normal-dot {
  background-color: green;
}

.abnormal-dot {
  background-color: darkgray;
}
</style>
