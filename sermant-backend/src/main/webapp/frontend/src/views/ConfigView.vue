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
    <el-descriptions :title=" $t('configView.configurationInformation')"
                     style="margin-top: 15px; width: 800px;font-weight:600;"></el-descriptions>
    <el-form :inline="true" style="margin: 15px 0 15px 0">
      <el-form-item>
        <div data-v-6e4bcd7a="" class="ep-input ep-input--large ep-input-group ep-input-group--prepend"
             style="width: auto;">
          <div class="ep-input-group__prepend" color="#606266">{{ $t('configView.plugin') }}</div>
        </div>
        <el-select v-model="requestParam.pluginType" :placeholder="$t('configView.configType')" size="large"
                   @change="handlerChangePluginType">
          <el-option v-for="item in options" :label="language == 'zh' ? item.label:item.value" :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="configCenterInfo.dynamicConfigType == 'nacos'" prop="namespace">
        <el-input size="large" v-model.trim="requestParam.namespace" :placeholder="$t('configView.inputProjectName')">
          <template #prepend>
            <span color="#606266">{{ $t('configView.project') }}</span>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="currentTemplate" prop="groupRule">
        <div class="ep-input ep-input--large ep-input-group ep-input-group--prepend">
          <div class="ep-input-group__prepend form-text" color="#606266">{{ $t('configInfo.groupRule') }}</div>
          <el-select v-model="requestParam.groupRule" :placeholder=" $t('configInfo.groupRulePlaceholder')"
                     size="large">
            <el-option v-for="item in currentTemplate.groupRule" :label="item" :value="item"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item v-if="currentTemplate" prop="keyRule">
        <div class="ep-input ep-input--large ep-input-group ep-input-group--prepend">
          <div class="ep-input-group__prepend form-text" color="#606266">{{ $t('configInfo.keyRule') }}</div>
          <el-select v-model="requestParam.keyRule" :placeholder=" $t('configInfo.keyRulePlaceholder')"
                     size="large">
            <el-option v-for="item in currentTemplate.keyRule" :label="item" :value="item"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType == 'common'">
        <el-input size="large" v-model.trim="requestParam.group" :placeholder="$t('configView.inputGroup')">
          <template #prepend><span color="#606266">group</span></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType == 'common'">
        <el-input size="large" v-model.trim="requestParam.key" :placeholder="$t('configView.inputKey')">
          <template #prepend><span color="#606266">key</span></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="requestParam.pluginType != 'common'">
        <el-switch v-model="exactMatchFlag" inline-prompt active-text="精确匹配" inactive-text="模糊匹配"
                   class="ml-2" style="--ep-switch-on-color: #13ce66; --ep-switch-off-color: #13ce66"/>
      </el-form-item>
      <el-form-item>
        <el-button size="large" @click="getConfigList">
          <el-icon>
            <Search/>
          </el-icon>
        </el-button>
      </el-form-item>
      <div>
        <el-form-item v-if="exactMatchFlag && currentTemplate" v-for="element in currentTemplate.elements">
          <el-input v-if="!element.values?.length" size="large"
                    :placeholder="language == 'zh' ? element.placeholder?.chineseDesc: element.placeholder?.englishDesc"
                    v-model="requestParam[element.name]">
            <template #prepend>
                <span color="#606266" class="form-text">{{
                    language == 'zh' && element.chineseDesc ? element.chineseDesc : element.name
                  }}</span>
            </template>
          </el-input>
          <div v-else class="ep-input ep-input--large ep-input-group ep-input-group--prepend">
            <div class="ep-input-group__prepend form-text" color="#606266">{{ $t('configInfo.ruleType') }}</div>
            <el-select v-model="requestParam[element.name]"
                       :placeholder="language == 'zh'?element.placeholder?.chineseDesc:element.placeholder?.englishDesc"
                       size="large">
              <el-option v-for="item in element.values" :label="language == 'zh'?item.chineseDesc:item.englishDesc"
                         :value="item.name"/>
            </el-select>
          </div>
        </el-form-item>
      </div>
    </el-form>

    <el-table :data="configInfos" border style="width: 100%;" :header-cell-style="{'background':'#f5f7fa'}">
      <el-table-column v-if="configCenterInfo.dynamicConfigType == 'nacos'"
                       column-key="namespace" align="center" prop="namespace" width="150px"
                       :label="$t('configView.project')"/>
      <el-table-column v-for="element in currentConfigTemplate?.elements" align="center" :prop="element.name"
                       :label="language == 'zh' && element.chineseDesc ? element.chineseDesc : element.name">
        <template v-slot="scope">
          <span>{{
              element.values ? getLabelByValue(scope.row[element.name], element.values) : scope.row[element.name]
            }}</span>
        </template>
      </el-table-column>
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
                         :title="$t('configView.areYouSureToDeleteTheCurrentConfiguration')"
                         @confirm="deleteConfig(scope.row, scope.$index)">
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
                 @click="toAddConfig()">{{ $t('configView.create') }}
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
import {ref, onBeforeMount, reactive, watch} from "vue";
import {RouteParamsRaw, useRouter} from "vue-router";
import axios from "axios";
import {Delete, InfoFilled, Search, View} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import {resultCodeMap} from '~/composables/config'
import i18n from "~/composables/translations";

const language = ref(i18n.global.locale.toString());
type stringMap = Record<string, string>;
const templates = ref<pluginTemplate[]>([]);
const currentTemplate = ref<pluginTemplate>();
const currentConfigTemplate = ref<pluginTemplate>();

interface pluginTemplate {
  plugin: {
    chineseName: string,
    englishName: string
  },
  groupRule: string[],
  keyRule: string[]
  elements: element[],
  configTemplates: [{
    key: string,
    value: string
  }]
}

interface element {
  englishDesc: string,
  chineseDesc: string,
  name: string,
  values: [{
    name: string
    englishDesc: string,
    chineseDesc: string,
  }],
  placeholder: {
    englishDesc: string,
    chineseDesc: string,
  },
  notice: {
    englishDesc: string,
    chineseDesc: string,
  },
  required: boolean
}

const getCurrentTemplate = (pluginType: string) => {
  return templates.value?.find(template => template.plugin.englishName === pluginType);
}

interface option {
  label: string,
  value: string
}

const options = ref<option[]>([]);

watch(() => i18n.global.locale, () => {
  language.value = i18n.global.locale.toString();
})

const getLabelByValue = (name: string, mapArray: stringMap[]) => {
  let map = mapArray.find(map => map['name'] === name);
  if (!map) {
    return name;
  }
  return language.value == 'zh' ? map['chineseDesc'] : map['englishDesc'];
}

const getTemplate = async () => {
  axios.get(`${window.location.origin}/sermant/templates`).then(function (response) {
    if (response.data.code == "00") {
      templates.value = response.data.data;
      console.log(templates.value)
      templates.value?.forEach(template => {
        options.value.push({
          label: template.plugin?.chineseName,
          value: template.plugin?.englishName
        });
      });
      options.value.push({
        label: i18n.global.t('common.common'),
        value: 'common'
      });
      currentTemplate.value = getCurrentTemplate(requestParam.value.pluginType);
      if (templates.value && templates.value.length != 0) {
        getConfigList();
      } else {
        intiRequestParam.value.pluginType = 'common'
      }
    } else {
      ElMessage({
        message: i18n.global.t('common.failedToObtainTemplate'),
        type: "error",
      });
      intiRequestParam.value.pluginType = 'common'
    }
  }).catch(function () {
    ElMessage({
      message: i18n.global.t('common.failedToObtainTemplate'),
      type: "error",
    });
    intiRequestParam.value.pluginType = 'common'
  });
};

const intiRequestParam = ref<stringMap>({
  appName: "",
  environment: "",
  group: "",
  key: "",
  namespace: "",
  pluginType: "router",
  serviceName: "",
  zone: "",
  groupRule: '',
  keyRule: '',
});

const requestParam = ref<stringMap>(intiRequestParam.value);

const exactMatchFlag = ref<boolean>(false);

const configCenterInfo = reactive({
  dynamicConfigType: "",
  serverAddress: "",
  userName: "",
  namespace: ""
});

onBeforeMount(async () => {
  await getTemplate();
  getConfigurationCenter();
});
// 路由
const router = useRouter();

const goBack = () => {
  router.push("/");
};

const configInfos = ref<stringMap[]>([]);

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
  }] as stringMap[],
  currentPage: 1,
  pageSize: 10,
  totalDataCount: 0
};

const getConfigList = () => {
  if (configCenterInfo.dynamicConfigType == 'nacos' && !requestParam.value.namespace) {
    ElMessage({
      message: i18n.global.t('configView.theProjectCannotBeEmpty'),
      type: "error",
    });
    return;
  }
  if (requestParam.value.pluginType == 'common' && !requestParam.value.group) {
    ElMessage({
      message: i18n.global.t('configView.theGroupCannotBeEmpty'),
      type: "error",
    });
    return;
  }
  let pluginType = requestParam.value.pluginType
  let params = {
    pluginType: requestParam.value.pluginType,
    group: requestParam.value.group,
    key: requestParam.value.key,
    groupRule: requestParam.value.groupRule,
    keyRule: requestParam.value.keyRule,
    exactMatchFlag: exactMatchFlag.value,
    namespace: requestParam.value.namespace
  }
  if (params.groupRule && exactMatchFlag) {
    params.group = replaceTemplate(params.groupRule, requestParam.value)
  }
  if (params.keyRule && exactMatchFlag) {
    params.key = replaceTemplate(params.keyRule, requestParam.value)
  }
  if (pluginType == 'common') {
    params.groupRule = params.group
    params.keyRule = params.key
    params.exactMatchFlag = true
  }
  axios.get(`${window.location.origin}/sermant/configs`, {
    params: params
  }).then(function (response) {
    const data = response.data;
    if (data.code == "00") {
      currentConfigTemplate.value = getCurrentTemplate(pluginType)
      configInfos.value = data.data;
      if (params.pluginType != 'common') {
        configInfos.value.forEach(configInfo => {
          extractVariables(configInfo['keyRule'], <string>configInfo['key'], configInfo);
          extractVariables(configInfo['groupRule'], <string>configInfo['group'], configInfo);
        });
        if (currentConfigTemplate.value?.elements) {
          configInfos.value = configInfos.value.filter(configInfo => {
            let flag = true;
            currentConfigTemplate.value?.elements.forEach(e => {
              if (e.values && !e.values.some(value => value.name == configInfo[e.name])) {
                flag = false;
                return
              }
            })
            return flag;
          });
        }
      }
      displayState.totalPage = Math.ceil(data.data.length / displayState.pageSize);
      displayState.currentPage = 1;
      ElMessage({
        message: i18n.global.t('configView.successfullyObtainedConfiguration'),
        type: "success",
      });
    } else {
      configInfos.value = [];
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

function extractVariables(template: string, instance: string, configInfo: stringMap) {
  const regex = /\$\{(\w+)\}/g;
  let match: RegExpExecArray | null;
  let name = '';
  let currantTemplate = template;
  while ((match = regex.exec(template)) !== null) {
    if (name) {
      let str = currantTemplate.substring(3 + name.length, match.index + currantTemplate.length - template.length)
      configInfo[name] = instance.substring(0, instance.indexOf(str));
      instance = instance.substring(instance.indexOf(str) + str.length)
      currantTemplate = currantTemplate.substring(match.index + currantTemplate.length - template.length)
      name = match[1]
    } else {
      name = match[1]
      instance = instance.substring(match.index)
      currantTemplate = currantTemplate.substring(match.index)
    }
  }
  if (3 + name.length <= currantTemplate.length && !name) {
    let str = currantTemplate.substring(3 + name.length)
    configInfo[name] = instance.substring(0, instance.lastIndexOf(str))
  } else {
    configInfo[name] = instance
  }
}

const replaceTemplate = (template: string, variables: stringMap) => {
  return template.replace(/\$\{\s*(\w+)\s*\}/g, (match, p1) => variables[p1] || match);
};

const getConfigurationCenter = () => {
  axios.get(`${window.location.origin}/sermant/ConfigurationCenter`).then(function (response) {
    const data = response.data.data;
    configCenterInfo.serverAddress = data.serverAddress;
    configCenterInfo.dynamicConfigType = data.dynamicConfigType.toLowerCase();
    configCenterInfo.userName = data.userName;
    configCenterInfo.namespace = data.namespace;
    requestParam.value.namespace = data.namespace;
  }).catch(function (error) {
    console.log(error);
  });
};

const handlerChangePluginType = (value: string) => {
  requestParam.value = {};
  requestParam.value.pluginType = value
  currentTemplate.value = getCurrentTemplate(requestParam.value.pluginType);
}

const fetchData = () => {
  const startIndex = (pageInfo.currentPage - 1) * pageInfo.pageSize;
  const endIndex = startIndex + pageInfo.pageSize;
  pageInfo.pagedData = configInfos.value.slice(startIndex, endIndex);
};
const handleSizeChange = (newSize: number) => {
  pageInfo.pageSize = newSize;
  pageInfo.currentPage = 1; // 回到第一页
  fetchData();
};

const handleCurrentChange = (newPage: number) => {
  pageInfo.currentPage = newPage;
  fetchData();
};

const deleteConfig = (row: stringMap, index: number) => {
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

const toModifyConfig = (row: stringMap) => {
  const params: RouteParamsRaw = {
    pluginType: row.pluginType,
    namespace: row.namespace,
    appName: row.appName,
    serviceName: row.serviceName,
    environment: row.environment,
    zone: row.zone,
    group: row.group,
    key: row.key,
    groupRule: row.groupRule,
    keyRule: row.keyRule,
    type: "modify",
    configType: configCenterInfo.dynamicConfigType
  };
  router.push({name: "configInfo", query: params});
};

const deleteRow = (index: number) => {
  configInfos.value.splice(index, 1)
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
