<template>
  <div style="width: 100%">
    <el-page-header :title="$t('instanceView.event')" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> {{ $t('instanceView.instanceStatus') }} </span>
      </template>
    </el-page-header>
    <el-row justify="space-around">
      <el-col :span="8">
        <div class="grid-content">
          <el-card shadow="hover" class="count-card">
            <p>{{ $t('instanceView.application') }}</p>
            <h1>{{ tagCount.applicationCount.length }}</h1>
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <el-card shadow="hover" class="count-card">
            <p>{{ $t('instanceView.service') }}</p>
            <h1>{{ tagCount.serviceCount.length }}</h1>
          </el-card>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content">
          <el-card shadow="hover" class="count-card">
            <p>{{ $t('instanceView.instance') }}</p>
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
          {{ $t('instanceView.service') }}: {{ tag }}
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
          {{ $t('instanceView.version') }}:{{ tag }}
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
              <el-dropdown-item @click="currentOption = 0">{{ $t('instanceView.service') }}</el-dropdown-item>
              <el-dropdown-item @click="currentOption = 1">IP</el-dropdown-item>
              <el-dropdown-item @click="currentOption = 2">{{ $t('instanceView.version') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template #append>
        <el-button @click="search">
          <el-icon :color="'#000000'">
            <Search/>
          </el-icon>
        </el-button>
      </template>
    </el-input>
    <div>
      <el-table :data="state.tableData" style="width: 100%" :border="true" stripe>
        <el-table-column :label="$t('instanceView.status')" prop="health" width="100">
          <template #default="scope">
            <div class="dot normal-dot" v-if="scope.row.health"></div>
            <div class="dot abnormal-dot" v-if="!scope.row.health"></div>
          </template>
        </el-table-column>
        <el-table-column prop="service" :label="$t('instanceView.service')" width="120"></el-table-column>
        <el-table-column prop="instanceId" :label="$t('instanceView.instanceID')" width="320"></el-table-column>
        <el-table-column prop="version" :label="$t('instanceView.version')" width="120"></el-table-column>
        <el-table-column prop="ipAddress" label="IP" width="180"></el-table-column>
        <el-table-column prop="heartbeatTime" :label="$t('instanceView.heartbeat')" width="180">
        </el-table-column>
        <el-table-column :label="$t('instanceView.plugin')">
          <template #default="props">
            <div class="plugin-box">
              <el-tag
                  class="mx-1"
                  v-for="(plugin, index) in props.row.pluginInfoMap"
                  :key="index"
              >{{ plugin.name }}:{{ plugin.version }}
              </el-tag
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
import {computed, onMounted, reactive, ref, watch} from "vue";
import {useRouter} from "vue-router";
import axios from "axios";
import moment from "moment";
import i18n from "~/composables/translations";

// router
const router = useRouter();

onMounted(() => {
  getTableData();
});

const goBack = () => {
  router.push("/events");
};

// format conversion
const time = (value) => {
  return moment(value).format("YYYY-MM-DD HH:mm:ss");
};

// handler filter labels
const searchOption = ref([i18n.global.t('instanceView.service'), "IP", i18n.global.t('instanceView.version')]);
const currentOption = ref(0);
const inputValue = ref("");

// Filter parameters
const applicationTag = reactive([]);
const serviceTag = reactive([]);
const ipTag = reactive([]);
const versionTag = reactive([]);

watch(() => i18n.global.locale, (newLocale, oldLocale) => {
  searchOption.value = [
    i18n.global.t('instanceView.service'),
    'IP',
    i18n.global.t('instanceView.version'),
  ];
});

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

// Statistics of data for each label
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
    axios
        .get(`${window.location.origin}/sermant/getPluginsInfo`)
        .then(function (res) {
          // Clean up statistical services
          tagCount.serviceCount = [];
          innerData = res.data;
          innerData.forEach((item) => {
            if (item.health) {
              state.normalCount++;
            } else {
              state.abnormalCount++;
            }
            // Number of statistical services
            if (tagCount.serviceCount.indexOf(item.service) === -1) {
              tagCount.serviceCount.push(item.service);
            }
          });
          showData = JSON.parse(JSON.stringify(res.data));
          state.total = showData.length;
          const result = JSON.parse(JSON.stringify(showData.slice(0, state.pageSize)));
          state.tableData = handle(result);
          ElMessage({
            message: i18n.global.t('instanceView.successfullyObtainedInstances'),
            type: "success",
          });
        })
        .catch(function (error) {
          ElMessage({
            message: i18n.global.t('instanceView.failedToObtainInstances'),
            type: "error",
          });
          console.log(error);
        });
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
  console.log(searchOption)
  state.loading = true;
  showData = [];
  tagCount.serviceCount = [];
  innerData.forEach((item) => {
    let isMatch = true;
    // Filtering Services
    if (serviceTag.length !== 0) {
      isMatch = isMatch && serviceTag.indexOf(item.service) !== -1;
    }
    // Filter IP
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
    // Filter version
    if (versionTag.length !== 0) {
      isMatch = isMatch && versionTag.indexOf(item.version) !== -1;
    }
    if (isMatch) {
      // Number of statistical services
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
