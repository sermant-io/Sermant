<template>
  <div style="width: 100%">
    <el-page-header :title="$t('configInfo.configurationManagement')" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> {{ $t('configInfo.configurationDetail') }} </span>
      </template>
    </el-page-header>
    <el-form :inline="true" :model="requestParam" label-width="auto" :rules="checkRule" ref="ruleFormRef"
             style="max-width: 80%; margin-top: 30px;margin-bottom: 15px; margin-left: 20px;">
      <el-descriptions :title="$t('configInfo.pluginInformation')" style="margin-top: 15px;"></el-descriptions>
      <el-form-item prop="pluginType">
        <div data-v-6e4bcd7a="" class="ep-input ep-input--large ep-input-group ep-input-group--prepend"
             style="width: auto;" v-if="!types.modifyFlay">
          <div class="ep-input-group__prepend" color="#606266">{{ $t('configInfo.type') }}</div>
        </div>
        <el-select v-model="requestParam.pluginType" :placeholder="$t('configInfo.selectPlugin')" size="large"
                   :disabled="types.modifyFlay" @change="changePluginType()" v-if="!types.modifyFlay">
          <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
        <el-input size="large" v-model="pluginName.name"
                  :placeholder="$t('configInfo.selectPlugin')"
                  class="input-style" :disabled="types.modifyFlay" @change="changePluginType()"
                  v-if="types.modifyFlay">
          <template #prepend>
            <span color="#606266" class="form-text">{{ $t('configInfo.type') }}</span>
          </template>
        </el-input>
      </el-form-item>
      <el-descriptions v-if="requestParam.pluginType == 'flowcontrol' || requestParam.pluginType == 'loadbalancer'"
                       :title="$t('configInfo.ruleInformation')" style="margin-top: 15px;"></el-descriptions>
      <el-form-item prop="ruleType"
                    v-if="requestParam.pluginType == 'flowcontrol' || requestParam.pluginType == 'loadbalancer'">
        <div data-v-6e4bcd7a="" class="ep-input ep-input--large ep-input-group ep-input-group--prepend"
             style="width: auto;" v-if="!types.modifyFlay">
          <div class="ep-input-group__prepend form-text" color="#606266">{{ $t('configInfo.ruleType') }}</div>
        </div>
        <el-select :placeholder="$t('configInfo.selectRuleType')" v-model="requestParam.ruleType" size="large"
                   :disabled="types.modifyFlay" v-if="!types.modifyFlay"
                   @change="handlerChange(true)">
          <el-option v-for="item in rules" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
        <el-input size="large" v-model="ruleName.name"
                  :placeholder="$t('configInfo.selectRuleType')"
                  class="input-style" :disabled="types.modifyFlay" @change="handlerChange(true)"
                  v-if="types.modifyFlay">
          <template #prepend>
            <span color="#606266" class="form-text">{{ $t('configInfo.ruleType') }}</span>
          </template>
        </el-input>
        <TooltipIcon :content="$t('configInfo.ruleTypeSupportedInPlugin')"/>
      </el-form-item>
      <el-form-item prop="sceneName"
                    v-if="requestParam.pluginType == 'flowcontrol' || requestParam.pluginType == 'loadbalancer'">
        <el-input size="large" v-model="requestParam.sceneName" :placeholder="$t('configInfo.inputSceneName')"
                  class="input-style" :disabled="types.modifyFlay" @change="handlerChange(false)">
          <template #prepend>
            <span color="#606266" class="form-text">{{ $t('configInfo.sceneName') }}</span>
          </template>
        </el-input>
        <TooltipIcon v-if="requestParam.pluginType == 'flowcontrol'" :content="$t('configInfo.flowcontrolRuleNotice')"/>
        <TooltipIcon v-if="requestParam.pluginType == 'loadbalancer'"
                     :content="$t('configInfo.loadbalancerRuleNotice')"/>
      </el-form-item>
      <div v-if="(requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'
      && requestParam.pluginType != 'tag-transmission') || types.configType == 'nacos'">
        <el-descriptions title="service.meta信息" style="margin-top: 15px;"></el-descriptions>
        <el-form-item v-if="types.configType == 'nacos'" prop="namespace">
          <el-input size="large" v-model.trim="requestParam.namespace" :placeholder="$t('configInfo.inputProjectName')"
                    class="input-style" :disabled="types.modifyFlay">
            <template #prepend>
              <span color="#606266" class="form-text">project</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.projectNotice')"/>
        </el-form-item>
        <el-form-item prop="appName" v-if="requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'
                      && requestParam.pluginType != 'tag-transmission'">
          <el-input size="large" v-model.trim="requestParam.appName"
                    :placeholder="$t('configInfo.inputApplicationName')"
                    style="width: 90%; margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">application</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.applicationNotice')"/>
        </el-form-item>
        <el-form-item
            v-if="requestParam.pluginType == 'database-write-prohibition' || requestParam.pluginType == 'mq-consume-prohibition'">
          <el-input size="large" v-model.trim="requestParam.serviceName"
                    :placeholder="$t('configInfo.inputServiceNameOrEmpty')"
                    class="input-style" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266">service</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.metaServiceNotice')"/>
        </el-form-item>
        <el-form-item v-if="requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'
                      && requestParam.pluginType != 'tag-transmission'">
          <el-input size="large" v-model.trim="requestParam.environment"
                    :placeholder="$t('configInfo.inputEnvironmentName')"
                    class="input-style" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">environment</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.environmentNotice')"/>
        </el-form-item>
        <el-form-item prop="zone"
                      v-if="requestParam.pluginType == 'database-write-prohibition' || requestParam.pluginType == 'mq-consume-prohibition'">
          <el-input size="large" v-model.trim="requestParam.zone" :placeholder="$t('configInfo.inputZoneName')"
                    class="input-style" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">zone</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.zoneNotice')"/>
        </el-form-item>
      </div>
      <div v-if="requestParam.pluginType != 'database-write-prohibition' && requestParam.pluginType != 'mq-consume-prohibition'
                       && requestParam.pluginType != 'removal' && requestParam.pluginType != 'other'
                       && requestParam.pluginType != 'tag-transmission'">
        <el-descriptions :title="$t('configInfo.serviceInformation')" style="margin-top: 15px;">
        </el-descriptions>
        <el-form-item prop="serviceName" v-if="requestParam.pluginType == 'flowcontrol'">
          <el-input size="large" v-model.trim="requestParam.serviceName"
                    :placeholder="$t('configInfo.inputServiceName')"
                    class="input-style" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">service</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.serviceNotice')"/>
        </el-form-item>
        <el-form-item v-if="requestParam.pluginType != 'flowcontrol'">
          <el-input v-if="requestParam.pluginType == 'router'" size="large" v-model.trim="requestParam.serviceName"
                    :placeholder="$t('configInfo.inputServiceNameOrEmpty')" class="input-style"
                    :disabled="types.modifyFlay"
                    @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">service</span>
            </template>
          </el-input>
          <el-input size="large" v-model.trim="requestParam.serviceName"
                    :placeholder="$t('configInfo.inputServiceName')"
                    class="input-style" :disabled="types.modifyFlay" @change="handlerChange(false)"
                    v-if="requestParam.pluginType != 'router'">
            <template #prepend>
              <span color="#606266" class="form-text">service</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.serviceNotice')"/>
        </el-form-item>
      </div>
      <div>
        <el-descriptions :title="$t('configInfo.configurationInformation')" style="margin-top: 15px;">
        </el-descriptions>
        <el-form-item prop="group" v-if="requestParam.pluginType == 'other'">
          <el-input size="large" v-model.trim="requestParam.group"
                    :placeholder="$t('configInfo.inputConfigurationGroup')"
                    class="input-style"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">group</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.configurationNotice')"/>
        </el-form-item>
        <el-form-item v-if="requestParam.pluginType != 'other'">
          <el-input size="large" v-model.trim="requestParam.group"
                    :placeholder="$t('configInfo.inputConfigurationGroup')"
                    class="input-style"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">group</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.configurationNotice')"/>
        </el-form-item>
        <el-form-item prop="key" v-if="requestParam.pluginType == 'other'">
          <el-input size="large" v-model.trim="requestParam.key" :placeholder="$t('configInfo.inputConfigurationName')"
                    class="input-style"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">key</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.configurationNotice')"/>
        </el-form-item>
        <el-form-item v-if="requestParam.pluginType != 'other'">
          <el-input size="large" v-model.trim="requestParam.key" placeholder=""
                    class="input-style"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266" class="form-text">key</span>
            </template>
          </el-input>
          <TooltipIcon :content="$t('configInfo.configurationNotice')"/>
        </el-form-item>
      </div>
      <el-descriptions :title="$t('configInfo.configurationContent')" style="margin-top: 15px;">
      </el-descriptions>
      <el-form-item style="display: flex;">
        <el-input v-model="requestParam.content" style="width: 90%;padding-right: 5px;" :autosize="{ minRows: 8 }"
                  type="textarea"
                  :placeholder="$t('configInfo.inputConfigurationContent')"
        />
        <TooltipIcon :content="$t('configInfo.configurationContentNotice')"/>
      </el-form-item>
      <div style="display: flex;align-items: center; margin-left: 30%; margin-top: 30px;width: 90%;">
        <el-button type="primary" size="large" style="width: 20%;" shouldAddSpace="true;"
                   @click="submit(ruleFormRef)">
          {{ $t('configInfo.submit') }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, reactive, ref, watch} from "vue";
import {LocationQuery, useRouter} from "vue-router";
import axios from "axios";
import TooltipIcon from '../components/layouts/TooltipIcon.vue'
import {ElMessage, FormInstance, FormRules} from "element-plus";
import {options, resultCodeMap} from '~/composables/config'
import i18n from "~/composables/translations";

const ruleFormRef = ref<FormInstance>()

const flowcontrolRules = ref([
  {label: '', value: ''},
]);

const pluginName = reactive({name: ''});

const ruleName = reactive({name: ''});

const rules = ref([
  {label: i18n.global.t('configInfo.trafficMatchingRule'), value: 'matchGroup'}
]);


const loadbalancerRules = ref([
  {label: i18n.global.t('configInfo.trafficTaggingRule'), value: 'matchGroup',},
  {label: i18n.global.t('configInfo.loadBalancingRule'), value: 'loadbalance',}
]);

const updateFlowControlRules = () => {
  flowcontrolRules.value = [
    {label: i18n.global.t('configInfo.trafficMatchingRule'), value: 'matchGroup',},
    {label: i18n.global.t('configInfo.rateLimitingRule'), value: 'rateLimiting',},
    {label: i18n.global.t('configInfo.circuitBreakerRule'), value: 'circuitBreaker',},
    {label: i18n.global.t('configInfo.bulkheadRule'), value: 'bulkhead',},
    {label: i18n.global.t('configInfo.faultInjectionRule'), value: 'faultInjection',},
    {label: i18n.global.t('configInfo.retryRule'), value: 'retry',},
    {label: i18n.global.t('configInfo.systemLevelFlowControl'), value: 'system',},
  ];
}

const updateLoadbalancerRules = () => {
  loadbalancerRules.value = [
    {label: i18n.global.t('configInfo.trafficTaggingRule'), value: 'matchGroup',},
    {label: i18n.global.t('configInfo.loadBalancingRule'), value: 'loadbalance',}
  ];
}

const updateRules = () => {
  const value = requestParam.pluginType;
  if (value == "flowcontrol") {
    rules.value = flowcontrolRules.value
  } else if (value == "loadbalancer") {
    rules.value = loadbalancerRules.value
  }
}

const requestParam: ConfigInfo = reactive({
  pluginType: "",
  namespace: "",
  appName: "",
  serviceName: "",
  environment: "",
  zone: "",
  group: "",
  key: "",
  content: "",
  ruleType: "",
  sceneName: ""
});

interface ConfigInfo {
  pluginType: string,
  namespace: string,
  appName: string,
  serviceName: string,
  environment: string,
  zone: string,
  group: string,
  key: string,
  content: string,
  ruleType: string,
  sceneName: string
}

// 路由
const router = useRouter();

const goBack = () => {
  router.push("/config");
};

const getConfig = () => {
  axios.get(`${window.location.origin}/sermant/config`, {
    params: requestParam,
  }).then(function (response) {
    if (response.data.code == "00") {
      requestParam.content = response.data.data.content;
    } else {
      ElMessage({
        message: resultCodeMap.get(response.data.code),
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: i18n.global.t('configInfo.failedToObtainConfiguration'),
      type: "error",
    });
  });
};

const types = reactive({
  modifyFlay: false,
  configType: "",
});

onMounted(() => {
  updateFlowControlRules();
  updateLoadbalancerRules();
  updateRules();
  console.log(rules.value)
  const param: LocationQuery = router.currentRoute.value.query;
  requestParam.ruleType = "matchGroup";
  types.configType = <string>param.configType;
  if (<string>param.type != "modify") {
    return;
  }
  types.modifyFlay = true;
  requestParam.pluginType = <string>param.pluginType;
  changeRules();
  pluginName.name = getLabelByValue(options.value, requestParam.pluginType);
  requestParam.group = <string>param.group;
  requestParam.key = <string>param.key;
  requestParam.environment = <string>param.environment;
  requestParam.appName = <string>param.appName;
  requestParam.namespace = <string>param.namespace;
  requestParam.serviceName = <string>param.serviceName;
  requestParam.zone = <string>param.zone;
  if (requestParam.pluginType == "flowcontrol" || requestParam.pluginType == "loadbalancer") {
    const parts = requestParam.key.split('.');
    if (parts.length >= 3) {
      requestParam.ruleType = parts[1];
      ruleName.name = getLabelByValue(rules.value, requestParam.ruleType);
      requestParam.sceneName = parts[2];
    }
  }
  getConfig();
});

const changeRules = () => {
  updateRules();
  handlerChange(true);
}

const changePluginType = () => {
  changeRules();
  requestParam.ruleType = "matchGroup";
};

const changePluginTypeToRouter = (isChangeTemplate: boolean) => {
  requestParam.group = "app=" + requestParam.appName + "&environment=" + requestParam.environment;
  if (!requestParam.serviceName) {
    requestParam.key = "servicecomb.globalRouteRule"
  } else {
    requestParam.key = "servicecomb.routeRule." + requestParam.serviceName
  }
  if (!isChangeTemplate) {
    return;
  }
  requestParam.content = "---\n" +
      "- kind: routematcher.sermant.io/flow\n" +
      "  description: test\n" +
      "  rules: \n" +
      "    - precedence: 1\n" +
      "      match:\n" +
      "        attachments:\n" +
      "          id:\n" +
      "            exact: '1'\n" +
      "            caseInsensitive: false\n" +
      "      route:\n" +
      "        - weight: 20\n" +
      "          tags:\n" +
      "            version: 1.0.0\n" +
      "        - weight: 80\n" +
      "          tags:\n" +
      "            version: 1.0.1\n" +
      "- kind: routematcher.sermant.io/tag\n" +
      "  description: sameTag\n" +
      "  rules:\n" +
      "    - precedence: 1\n" +
      "      match:\n" +
      "        tags:\n" +
      "          zone:\n" +
      "            exact: 'hangzhou'\n" +
      "            caseInsensitive: false\n" +
      "        policy:\n" +
      "          triggerThreshold: 20\n" +
      "          minAllInstances: 3\n" +
      "- kind: route.sermant.io/lane\n" +
      "  description: lane\n" +
      "  rules:\n" +
      "    - precedence: 1\n" +
      "      match:\n" +
      "        method: getFoo\n" +
      "        path: \"io.sermant.bar\"\n" +
      "        protocol: dubbo\n" +
      "        attachments:\n" +
      "          id:\n" +
      "            exact: '1'\n" +
      "            caseInsensitive: false\n" +
      "        args:\n" +
      "          args0:\n" +
      "            type: .name\n" +
      "            exact: 'foo'\n" +
      "      route:\n" +
      "        - tag-inject:\n" +
      "            x-sermant-flag2: gray2\n" +
      "          weight: 100";
}

const changePluginTypeToFlowControl = (isChangeTemplate: boolean) => {
  requestParam.group = "service=" + requestParam.serviceName
  requestParam.key = "servicecomb." + requestParam.ruleType + "." + requestParam.sceneName;
  if (!isChangeTemplate) {
    return;
  }
  if (requestParam.ruleType == "matchGroup") {
    requestParam.content = "matches:\n" +
        "  - apiPath:\n" +
        "      exact: /degrade\n" +
        "    headers:\n" +
        "      key:\n" +
        "        exact: value\n" +
        "    method:\n" +
        "      - GET\n" +
        "    name: degrade";
    return;
  }
  if (requestParam.ruleType == "rateLimiting") {
    requestParam.content = "limitRefreshPeriod: 1000\nrate: 2";
    return;
  }
  if (requestParam.ruleType == "circuitBreaker") {
    requestParam.content = "failureRateThreshold: 90\nminimumNumberOfCalls: 3\nslidingWindowSize: 10S\n" +
        "slidingWindowType: time\nslowCallRateThreshold: 80 ";
    return;
  }
  if (requestParam.ruleType == "bulkhead") {
    requestParam.content = "maxConcurrentCalls: 5\nmaxWaitDuration: 10S";
    return;
  }
  if (requestParam.ruleType == "faultInjection") {
    requestParam.content = "type: abort\npercentage: 100\nfallbackType: ReturnNull\nforceClosed: false\nerrorCode: 503";
    return;
  }
  if (requestParam.ruleType == "retry") {
    requestParam.content = "waitDuration: 2000\nretryStrategy: FixedInterval\nmaxAttempts: 2\nretryOnResponseStatus:\n" +
        "  - 500";
    return;
  }
  if (requestParam.ruleType == "system") {
    requestParam.content = "systemLoad: 5\ncpuUsage: 0.6\nqps: 1000\naveRt: 100\nthreadNum: 200";
  }
}

const changePluginTypeToRemoval = (isChangeTemplate: boolean) => {
  requestParam.group = "app=" + requestParam.appName + "&environment=" + requestParam.environment;
  requestParam.key = "sermant.removal.config";
  if (!isChangeTemplate) {
    return;
  }
  requestParam.content = "expireTime: 60000\n" +
      "exceptions:\n" +
      "  - com.alibaba.dubbo.remoting.TimeoutException\n" +
      "  - org.apache.dubbo.remoting.TimeoutException\n" +
      "  - java.util.concurrent.TimeoutException\n" +
      "  - java.net.SocketTimeoutException\n" +
      "enableRemoval: false\n" +
      "recoveryTime: 30000\n" +
      "rules:\n" +
      "  - { key: default-rule, scaleUpLimit: 0.6, minInstanceNum: 1, errorRate: 0.6 }";
}

const changePluginTypeToMqConsumeProhibition = (isChangeTemplate: boolean) => {
  requestParam.group = "app=" + requestParam.appName + "&environment=" + requestParam.environment;
  requestParam.group = requestParam.group + "&zone=" + requestParam.zone;
  if (!requestParam.serviceName) {
    requestParam.key = "sermant.mq.consume.globalConfig"
  } else {
    requestParam.key = "sermant.mq.consume." + requestParam.serviceName
  }
  if (!isChangeTemplate) {
    return;
  }
  requestParam.content = "enableKafkaProhibition: true\n" +
      "kafkaTopics:\n" +
      " - demo-kafka-topic\n" +
      "enableRocketMqProhibition: true\n" +
      "rocketMqTopics:\n" +
      " - demo-rocketmq-topic-1\n" +
      " - demo-rocketmq-topic-2";
}

const changePluginTypeToDatabaseWriteProhibition = (isChangeTemplate: boolean) => {
  requestParam.group = "app=" + requestParam.appName + "&environment=" + requestParam.environment;
  requestParam.group = requestParam.group + "&zone=" + requestParam.zone;
  if (!requestParam.serviceName) {
    requestParam.key = "sermant.database.write.globalConfig"
  } else {
    requestParam.key = "sermant.database.write." + requestParam.serviceName
  }
  if (!isChangeTemplate) {
    return;
  }
  requestParam.content = "enableMongoDbWriteProhibition: true\n" +
      "mongoDbDatabases:\n" +
      " - mongodb-database-1\n" +
      "enableMySqlWriteProhibition: true\n" +
      "mySqlDatabases:\n" +
      " - mysql-database-1\n" +
      "enablePostgreSqlWriteProhibition: true\n" +
      "postgreSqlDatabases:\n" +
      " - postgresql-database-1\n" +
      "enableOpenGaussWriteProhibition: true\n" +
      "openGaussDatabases:\n" +
      " - opengauss-database-1";
}

const changePluginTypeToTagTransmission = (isChangeTemplate: boolean) => {
  requestParam.group = "sermant/tag-transmission-plugin";
  requestParam.key = "tag-config"
  if (!isChangeTemplate) {
    return;
  }
  requestParam.content = "enabled: true\nmatchRule:\n" +
      "  exact: [\"id\", \"name\"]\n" +
      "  prefix: [\"x-sermant-\"]\n" +
      "  suffix: [\"-sermant\"]"
}

const changePluginTypeToSpringbootRegistry = (isChangeTemplate: boolean) => {
  requestParam.group = "app=" + requestParam.appName + "&environment=" + requestParam.environment;
  if (requestParam.serviceName) {
    requestParam.group = requestParam.group + "&service=" + requestParam.serviceName
  }
  requestParam.key = "sermant.plugin.registry"
  if (!isChangeTemplate) {
    return;
  }
  requestParam.content = "strategy: all\n" +
      "value: service-b";
}

const changePluginTypeToServiceRegistry = (isChangeTemplate: boolean) => {
  requestParam.group = "app=" + requestParam.appName + "&environment=" + requestParam.environment;
  if (requestParam.serviceName) {
    requestParam.group = requestParam.group + "&service=" + requestParam.serviceName
  }
  requestParam.key = "sermant.agent.registry";
  if (!isChangeTemplate) {
    return;
  }
  requestParam.content = "origin.__registry__.needClose: true";
}

const changePluginTypeToLoadbalancer = (isChangeTemplate: boolean) => {
  requestParam.group = "app=" + requestParam.appName + "&environment=" + requestParam.environment;
  if (requestParam.serviceName) {
    requestParam.group = requestParam.group + "&service=" + requestParam.serviceName
  }
  requestParam.key = "servicecomb." + requestParam.ruleType + "." + requestParam.sceneName;
  if (!isChangeTemplate) {
    return;
  }
  if (requestParam.ruleType == "matchGroup") {
    requestParam.content = "alias: loadbalancer-rule\n" +
        "matches:\n" +
        "  - serviceName: zk-rest-provider";
    return;
  }
  requestParam.content = "rule: Random";
}

const handlerChange = (isChangeTemplate: boolean) => {
  const value = requestParam.pluginType;
  console.log(!requestParam.serviceName);
  if (value == "router") {
    changePluginTypeToRouter(isChangeTemplate);
    return;
  }
  if (value == "springboot-registry") {
    changePluginTypeToSpringbootRegistry(isChangeTemplate);
    return;
  }
  if (value == "service-registry") {
    changePluginTypeToServiceRegistry(isChangeTemplate);
    return;
  }
  if (value == "loadbalancer") {
    changePluginTypeToLoadbalancer(isChangeTemplate);
    return;
  }
  if (value == "flowcontrol") {
    changePluginTypeToFlowControl(isChangeTemplate);
    return;
  }
  if (value == "removal") {
    changePluginTypeToRemoval(isChangeTemplate);
    return;
  }
  if (value == "mq-consume-prohibition") {
    changePluginTypeToMqConsumeProhibition(isChangeTemplate);
    return;
  }
  if (value == "database-write-prohibition") {
    changePluginTypeToDatabaseWriteProhibition(isChangeTemplate);
    return;
  }
  if (value == "tag-transmission") {
    changePluginTypeToTagTransmission(isChangeTemplate);
    return;
  }
  if (value == "other" && isChangeTemplate) {
    requestParam.key = "";
    requestParam.group = "";
    requestParam.content = "";
  }
}

const checkRule = reactive({
  appName: [
    {required: true, message: i18n.global.t('configInfo.inputApplicationName'), trigger: 'change'}
  ],
  pluginType: [
    {required: true, message: i18n.global.t('configInfo.selectPlugin'), trigger: 'change'}
  ],
  ruleType: [
    {required: true, message: i18n.global.t('configInfo.selectRuleType'), trigger: 'change'}
  ],
  sceneName: [
    {required: true, message: i18n.global.t('configInfo.inputSceneName'), trigger: 'change'}
  ],
  group: [
    {required: true, message: i18n.global.t('configInfo.inputConfigurationGroup'), trigger: 'change'}
  ],
  key: [
    {required: true, message: i18n.global.t('configInfo.inputConfigurationName'), trigger: 'change'}
  ],
  namespace: [
    {required: true, message: i18n.global.t('configInfo.inputProjectName'), trigger: 'change'}
  ],
  zone: [
    {required: true, message: i18n.global.t('configInfo.inputZoneName'), trigger: 'change'}
  ],
  serviceName: [
    {required: true, message: i18n.global.t('configInfo.inputServiceName'), trigger: 'change'}
  ],
});
watch(() => i18n.global.locale, () => {
  updateFlowControlRules();
  updateLoadbalancerRules();
  updateRules();
  checkRule.appName[0].message = i18n.global.t('configInfo.inputApplicationName');
  checkRule.pluginType[0].message = i18n.global.t('configInfo.selectPlugin');
  checkRule.ruleType[0].message = i18n.global.t('configInfo.selectRuleType');
  checkRule.sceneName[0].message = i18n.global.t('configInfo.inputSceneName');
  checkRule.group[0].message = i18n.global.t('configInfo.inputConfigurationGroup');
  checkRule.key[0].message = i18n.global.t('configInfo.inputConfigurationName');
  checkRule.namespace[0].message = i18n.global.t('configInfo.inputProjectName');
  checkRule.zone[0].message = i18n.global.t('configInfo.inputZoneName');
  checkRule.serviceName[0].message = i18n.global.t('configInfo.inputServiceName');
});

const getLabelByValue = (arr: { label: string; value: string; }[], value: string) => {
  const option = arr.find(option => option.value === value);
  return option ? option.label : "";
};

const submit = (formEl: FormInstance | undefined) => {
  if (!formEl) {
    return;
  }
  console.log(formEl);
  formEl.validate((valid) => {
    console.log(valid);
    if (!valid) {
      return;
    }
    if (types.modifyFlay) {
      updateConfig();
      return;
    }
    addConfig();
  })
};

const addConfig = () => {
  axios.post(`${window.location.origin}/sermant/config`, {
    key: requestParam.key,
    group: requestParam.group,
    content: requestParam.content,
    namespace: requestParam.namespace
  }).then(function (response) {
    if (response.data.code == "00") {
      ElMessage({
        message: i18n.global.t('configInfo.successfullyCreatedConfiguration'),
        type: "success",
      });
    } else if (response.data.code == "05") {
      ElMessage({
        message: i18n.global.t('configInfo.failedToCreateConfiguration'),
        type: "error",
      });
    } else {
      ElMessage({
        message: resultCodeMap.get(response.data.code),
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: i18n.global.t('configInfo.failedToCreateConfiguration'),
      type: "error",
    });
  });
}

const updateConfig = () => {
  axios.put(`${window.location.origin}/sermant/config`, {
    key: requestParam.key,
    group: requestParam.group,
    content: requestParam.content,
    namespace: requestParam.namespace
  }).then(function (response) {
    if (response.data.code == "00") {
      ElMessage({
        message: i18n.global.t('configInfo.successfullyUpdatedConfiguration'),
        type: "success",
      });
    } else if (response.data.code == "05") {
      ElMessage({
        message: i18n.global.t('configInfo.failedToUpdateConfiguration'),
        type: "error",
      });
    } else {
      ElMessage({
        message: resultCodeMap.get(response.data.code),
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: i18n.global.t('configInfo.failedToUpdateConfiguration'),
      type: "error",
    });
  });
}
</script>
<style scoped>
:deep(.ep-input-group__prepend) {
  width: 60px !important;
}

:deep(.ep-input__wrapper) {
  width: 350px;
  padding: 1px 11px;
}

:deep(.ep-descriptions__title) {
  font-size: 18px;
  color: var(--ep-text-color-primary);
}

.form-text {
  width: 100px;
  text-align: center;
}

.input-style {
  width: 90%;
  margin-right: 5px;
}

:deep(.ep-form-item) {
  margin-right: 200px !important;
}

:deep(.select-trigger) {
  padding: 1px 4px 1px 0;
}

:deep(.ep-form-item__content) {
  width: 500px;
}

:deep(.ep-select .ep-input) {
  width: 340px;
}

.ep-form-item.is-required > .ep-form-item__content > .ep-input-group--prepend:not(.is-disabled):before {
  content: "*";
  color: var(--el-color-danger);
  margin-right: 4px;
}

.ep-form-item:not(.is-required) > .ep-form-item__content > .ep-input-group--prepend:before {
  content: "*";
  color: var(--el-color-danger);
  margin-right: 4px;
  visibility: hidden;
}

.ep-form-item.is-required > .ep-form-item__content > .ep-input-group--prepend.is-disabled:before {
  content: "*";
  color: var(--el-color-danger);
  margin-right: 4px;
  visibility: hidden;
}
</style>