<template>
  <div style="width: 100%">
    <el-page-header :title="$t('configView.instance')" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> {{ $t('configView.configurationManagement') }} </span>
      </template>
    </el-page-header>
    <el-descriptions class="ep-page-header__content" :title="$t('configView.configurationCenterInformation')"
                     style="margin-top: 30px; width: 800px;"></el-descriptions>
    <el-form :inline="true" style="margin: 15px 0 15px 0">
      <el-form-item>
        <el-input size="large" readonly disabled v-model.trim="configCenterInfo.dynamicConfigType">
          <template #prepend><span color="#606266">{{ $t('configView.type') }}</span></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-input size="large" readonly disabled v-model.trim="configCenterInfo.serverAddress">
          <template #prepend><span color="#606266">{{ $t('configView.address') }}</span></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-input size="large" readonly disabled v-model.trim="configCenterInfo.userName">
          <template #prepend><span color="#606266">{{ $t('configView.user') }}</span></template>
        </el-input>
      </el-form-item>
    </el-form>
    <el-descriptions :title=" $t('configView.configurationInformation')" style="margin-top: 15px; width: 800px;font-weight:600;"></el-descriptions>
    <el-form :inline="true" style="margin: 15px 0 15px 0">
      <el-form-item>
        <div data-v-6e4bcd7a="" class="ep-input ep-input--large ep-input-group ep-input-group--prepend"
             style="width: auto;">
          <div class="ep-input-group__prepend" color="#606266">{{ $t('configView.plugin') }}</div>
        </div>
        <el-select v-model="defaultPluginType.value" :placeholder="$t('configView.configType')" size="large"
                   @change="handlerChangePluginType">
          <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="configCenterInfo.dynamicConfigType == 'nacos'" prop="namespace">
        <el-input size="large" v-model.trim="requestParam.namespace" :placeholder="$t('configView.inputProjectName')">
          <template #prepend>
            <span color="#606266">{{ $t('configView.project') }}</span>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'">
        <el-input size="large" v-model.trim="requestParam.appName" :placeholder="$t('configView.inputApplicationName')">
          <template #prepend><span color="#606266">{{ $t('configView.application') }}</span></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType != 'other'">
        <el-input size="large" v-model.trim="requestParam.serviceName" :placeholder="$t('configView.inputServiceName')">
          <template #prepend><span color="#606266">{{ $t('configView.service') }}</span></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'">
        <el-input size="large" v-model.trim="requestParam.environment" :placeholder="$t('configView.inputEnvironmentName')">
          <template #prepend><span color="#606266">{{ $t('configView.environment') }}</span></template>
        </el-input>
      </el-form-item>
      <el-form-item
          v-if="requestParam.pluginType == 'mq-consume-prohibition' || requestParam.pluginType == 'database-write-prohibition'">
        <el-input size="large" v-model.trim="requestParam.zone" :placeholder="$t('configView.inputZoneName')">
          <template #prepend><span color="#606266">{{ $t('configView.zone') }}</span></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType == 'other'">
        <el-input size="large" v-model.trim="requestParam.group" :placeholder="$t('configView.inputGroup')">
          <template #prepend><span color="#606266">group</span></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType == 'other'">
        <el-input size="large" v-model.trim="requestParam.key" :placeholder="$t('configView.inputKey')">
          <template #prepend><span color="#606266">key</span></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button size="large" @click="getConfigList">
          <el-icon>
            <Search/>
          </el-icon>
        </el-button>
      </el-form-item>
    </el-form>

    <el-table :data="configInfos.configInfos" border style="width: 100%;" :header-cell-style="{'background':'#f5f7fa'}">
      <el-table-column v-if="configCenterInfo.dynamicConfigType == 'nacos'"
                       column-key="namespace" align="center" prop="namespace" width="150px" :label="$t('configView.project')"/>
      <el-table-column column-key="appName" align="center" prop="appName" width="150px" :label="$t('configView.application')"
                       :filters="appNames"
                       :filter-method="filterHandler"/>
      <el-table-column column-key="serviceName" align="center" prop="serviceName" width="300px" :label="$t('configView.service')"
                       :filters="serviceNames"
                       :filter-method="filterHandler"/>
      <el-table-column column-key="environment" align="center" prop="environment" width="150px" :label="$t('configView.environment')"
                       :filters="environments"
                       :filter-method="filterHandler"/>
      <el-table-column
          v-if="currentPluginType.value == 'mq-consume-prohibition' || currentPluginType.value == 'database-write-prohibition'"
          column-key="zone" align="center" prop="zone" width="150px" :label="$t('configView.zone')"
          style="background-color: rgba(147,197,253,.5)"/>
      <el-table-column column-key="group" align="center" prop="group" label="group"/>
      <el-table-column column-key="key" align="center" prop="key" label="key"/>
      <el-table-column fixed="right" align="center" :label="$t('configView.operation')" width="120px">
        <template #default="scope">
          <el-tooltip :content="$t('configView.view')" effect="light">
            <el-button type="primary" @click="toModifyConfig(scope.row)" circle>
              <el-icon>
                <View/>
              </el-icon>
            </el-button>
          </el-tooltip>
          <el-popconfirm :confirm-button-text="$t('configView.yes')" :cancel-button-text="$t('configView.no')"
                         :icon="InfoFilled" icon-color="#626AEF"
                         :title="$t('configView.areYouSureToDeleteTheCurrentConfiguration')" @confirm="deleteConfig(scope.row, scope.$index)">
            <template #reference>
              <div style="display: inline;">
                <el-tooltip :content="$t('configView.delete')" effect="light">
                  <el-button type="danger" :icon="Delete" style="margin-left: 12px;" circle/>
                </el-tooltip>
              </div>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div style="display: flex;align-items: center; margin-top: 30px; width:100%;">
      <el-button type="primary" size="large" style="margin-left: 42.5%; width: 15%;" shouldAddSpace="true"
                 @click="toAddConfig()">{{$t('configView.create')}}
      </el-button>
    </div>
    <div class="pagination-div" style="margin-top: 30px;">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageInfo.currentPage"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="pageInfo.pageSize"
          :total="pageInfo.totalDataCount"
          layout="sizes, prev, pager, next, jumper"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, reactive} from "vue";
import {RouteParamsRaw, useRouter} from "vue-router";
import axios from "axios";
import {Delete, InfoFilled, Search} from '@element-plus/icons-vue'
import {ElMessage, TableColumnCtx} from 'element-plus'
import {resultCodeMap, options} from '../composables/config'
import i18n from "~/composables/translations";

const requestParam = reactive<ConfigInfo>({
  appName: "",
  environment: "",
  group: "",
  key: "",
  namespace: "",
  pluginType: "",
  serviceName: "",
  zone: ""
});

const appNames = reactive([
  {text: '', value: ''},
]);

const serviceNames = reactive([
  {text: '', value: ''},
]);

const environments = reactive([
  {text: '', value: ''},
]);

const configCenterInfo = reactive({
  dynamicConfigType: "",
  serverAddress: "",
  userName: "",
  namespace: ""
});

onBeforeMount(() => {
  requestParam.pluginType = 'router';
  requestParam.group = "app"
  requestParam.key = "servicecomb."
  getConfigList();
  getConfigurationCenter();
});

const defaultPluginType = reactive({
  value: 'router'
});

const currentPluginType = reactive({
  value: 'router'
});

const filterHandler = (
    value: string,
    row: ConfigInfo,
    column: TableColumnCtx<ConfigInfo>
) => {
  const property = column['property']
  return row[property] == value;
}

// 路由
const router = useRouter();

const goBack = () => {
  router.push("/");
};

const configInfos = reactive({
  configInfos: [] as ConfigInfo[]
});

const displayState = reactive({
  currentPage: 1,
  totalPage: 1,
  pageSize: 10,
});

const pageInfo = {
  pagedData: [{
    pluginType: "",
    namespace: "",
    appName: "",
    serviceName: "",
    environment: "",
    zone: "",
    group: "",
    key: "",
  }], // The current paginated data list
  currentPage: 1, // Current page number
  pageSize: 10, // Number of data displayed per page
  totalDataCount: 0 // Total number of data entries
};

const fillAppNames = () => {
  appNames.splice(0, appNames.length);
  configInfos.configInfos.forEach(info => {
    if (info.appName) {
      const existingObj = appNames.find(item => item.value === info.appName);
      if (!existingObj) {
        appNames.push({text: info.appName, value: info.appName});
      }
    }
  });
}

const fillServiceNames = () => {
  serviceNames.splice(0, serviceNames.length);
  configInfos.configInfos.forEach(info => {
    if (info.serviceName) {
      const existingObj = serviceNames.find(item => item.value === info.serviceName);
      if (!existingObj) {
        serviceNames.push({text: info.serviceName, value: info.serviceName});
      }
    }
  });
}

const fillEnvironments = () => {
  environments.splice(0, environments.length);
  configInfos.configInfos.forEach(info => {
    if (info.environment) {
      const existingObj = appNames.find(item => item.value === info.environment);
      if (!existingObj) {
        environments.push({text: info.environment, value: info.environment});
      }
    }
  });
}

const getConfigList = () => {
  if (configCenterInfo.dynamicConfigType == 'nacos' && !requestParam.namespace) {
    ElMessage({
      message: i18n.global.t('configView.theProjectCannotBeEmpty'),
      type: "error",
    });
    return;
  }
  if (requestParam.pluginType == 'other' && !requestParam.group) {
    ElMessage({
      message: i18n.global.t('configView.theGroupCannotBeEmpty'),
      type: "error",
    });
    return;
  }
  axios.get(`${window.location.origin}/sermant/configs`, {
    params: requestParam,
  }).then(function (response) {
    currentPluginType.value = requestParam.pluginType;
    const data = response.data;
    if (data.code == "00") {
      configInfos.configInfos = data.data;
      displayState.totalPage = Math.ceil(data.data.length / displayState.pageSize);
      displayState.currentPage = 1;
      if (configInfos.configInfos.length > 0) {
        configInfos.configInfos.forEach(info => {
          if (info.group.includes("&environment=") && !info.environment) {
            info.environment = "-";
          } else if (!info.group.includes("&environment=") && !info.environment) {
            info.environment = "N/A";
          }
          if (info.group.includes("&service=") && !info.serviceName) {
            info.serviceName = "-";
          } else if (!info.group.includes("&service=") && !info.serviceName) {
            info.serviceName = "N/A";
          }
          if (info.group.includes("&zone=") && !info.zone) {
            info.zone = "-";
          } else if (!info.group.includes("&zone=") && !info.zone) {
            info.zone = "N/A";
          }
          if (info.group.includes("app=") && !info.appName) {
            info.appName = "-";
          } else if (!info.group.includes("app=") && !info.appName) {
            info.appName = "N/A";
          }
          if (info.group.includes("GROUP=") && configCenterInfo.dynamicConfigType == 'kie') {
            info.group = info.group.replace("GROUP=", "");
          }
        });
        fillAppNames();
        fillServiceNames();
        fillEnvironments();
      }
      ElMessage({
        message: i18n.global.t('configView.successfullyObtainedConfiguration'),
        type: "success",
      });
    } else {
      configInfos.configInfos = [];
      ElMessage({
        message: resultCodeMap.get(data.code),
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: i18n.global.t('configView.failedObtainedConfiguration'),
      type: "error",
    });
  });
};

const getConfigurationCenter = () => {
  axios.get(`${window.location.origin}/sermant/ConfigurationCenter`).then(function (response) {
    const data = response.data.data;
    configCenterInfo.serverAddress = data.serverAddress;
    configCenterInfo.dynamicConfigType = data.dynamicConfigType.toLowerCase();
    configCenterInfo.userName = data.userName;
    configCenterInfo.userName = data.namespace;
    requestParam.namespace = data.namespace;
  }).catch(function (error) {
    console.log(error);
  });
};

const handlerChangePluginType = (value: string) => {
  requestParam.pluginType = value;
  if (value == "router") {
    requestParam.group = "app="
    requestParam.key = "servicecomb."
  } else if (value == "springboot-registry") {
    requestParam.group = "app="
    requestParam.key = "sermant.plugin.registry"
  } else if (value == "service-registry") {
    requestParam.group = "app="
    requestParam.key = "sermant.agent.registry"
  } else if (value == "flowcontrol") {
    requestParam.group = "service="
    requestParam.key = "servicecomb."
  } else if (value == "removal") {
    requestParam.group = "app="
    requestParam.key = "sermant.removal.config"
  } else if (value == "loadbalancer") {
    requestParam.group = "app="
    requestParam.key = "servicecomb."
  } else if (value == "tag-transmission") {
    requestParam.group = "sermant/tag-transmission-plugin"
    requestParam.key = "tag-config"
  } else if (value == "mq-consume-prohibition") {
    requestParam.group = "app="
    requestParam.key = "sermant.mq.consume."
  } else if (value == "database-write-prohibition") {
    requestParam.group = "app="
    requestParam.key = "sermant.database.write."
  } else if (value == "other") {
    requestParam.group = ""
    requestParam.key = ""
  }
  requestParam.appName = "";
  requestParam.serviceName = "";
  requestParam.zone = "";
  requestParam.environment = "";
}

interface ConfigInfo {
  pluginType: string,
  namespace: string,
  appName: string,
  serviceName: string,
  environment: string,
  zone: string,
  group: string,
  key: string,
}

const fetchData = () => {
  // Based on the current page number and the number of displayed items per page, capture the data for the current page
  // break
  const startIndex = (pageInfo.currentPage - 1) * pageInfo.pageSize;
  const endIndex = startIndex + pageInfo.pageSize;
  pageInfo.pagedData = configInfos.configInfos.slice(startIndex, endIndex);
};
const handleSizeChange = (newSize: number) => {
  // Trigger when the number of displayed items per page changes
  pageInfo.pageSize = newSize;
  pageInfo.currentPage = 1; // 回到第一页
  fetchData();
};

const handleCurrentChange = (newPage: number) => {
  // Trigger when the current page number changes
  pageInfo.currentPage = newPage;
  fetchData();
};

const deleteConfig = (row: ConfigInfo, index: number) => {
  const params = {
    group: row.group,
    key: row.key,
    namespace: row.namespace
  };
  axios.delete(`${window.location.origin}/sermant/config`, {
    params: params,
  }).then(function (response) {
    const data = response.data;
    if (data.code == "00") {
      ElMessage({
        message: i18n.global.t('configView.successfullyDeletedConfiguration'),
        type: "success",
      });
      deleteRow(index);
    } else {
      ElMessage({
        message: resultCodeMap.get(data.code),
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: i18n.global.t('configView.failedDeletedConfiguration'),
      type: "error",
    });
  });
};

const toAddConfig = () => {
  const params = {
    type: "add",
    configType: configCenterInfo.dynamicConfigType
  };
  router.push({name: "configInfo", query: params});
};

const toModifyConfig = (row: ConfigInfo) => {
  const params: RouteParamsRaw = {
    pluginType: row.pluginType,
    namespace: row.namespace,
    appName: row.appName,
    serviceName: row.serviceName,
    environment: row.environment,
    zone: row.zone,
    group: row.group,
    key: row.key,
    type: "modify",
    configType: configCenterInfo.dynamicConfigType
  };
  router.push({name: "configInfo", query: params});
};

const deleteRow = (index: number) => {
  configInfos.configInfos.splice(index, 1)
}
</script>

<style scoped>
.pagination-div {
  margin: 5px;
  display: flex;
  justify-content: space-around;
}

:deep(.ep-input-group__prepend) {
  width: 70px !important;
}

:deep(.ep-form-item__content .ep-select) {
  width: 200px;
}

:deep(.ep-descriptions__title) {
  font-size: 18px;
  color: var(--ep-text-color-primary);
}
</style>
