<template>
  <div style="width: 100%">
    <el-page-header :title="'配置管理'" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> 配置详情 </span>
      </template>
    </el-page-header>
    <el-form :inline="true" :model="requestParam" label-width="auto" :rules="checkRule" ref="ruleFormRef"
             style="max-width: 80%; margin-top: 30px;margin-bottom: 15px; margin-left: 20px;">
      <el-descriptions title="插件配置信息" style="margin-top: 15px;"></el-descriptions>
      <el-form-item prop="pluginType">
        <div data-v-6e4bcd7a="" class="ep-input ep-input--large ep-input-group ep-input-group--prepend"
             style="width: auto;" v-if="!types.modifyFlay">
          <div class="ep-input-group__prepend" color="#606266">插件类型</div>
        </div>
        <el-select v-model="requestParam.pluginType" placeholder="请选择插件类型" size="large"
                   :disabled="types.modifyFlay" @change="changePluginType()" v-if="!types.modifyFlay">
          <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
        <el-input size="large" v-model="pluginName.name"
                  placeholder="请选择插件类型"
                  style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="changePluginType()"
                  v-if="types.modifyFlay">
          <template #prepend>
            <span color="#606266">插件类型</span>
          </template>
        </el-input>
      </el-form-item>
      <el-descriptions v-if="requestParam.pluginType == 'flowcontrol' || requestParam.pluginType == 'loadbalancer'"
                       title="规则信息" style="margin-top: 15px;"></el-descriptions>
      <el-form-item prop="ruleType"
                    v-if="requestParam.pluginType == 'flowcontrol' || requestParam.pluginType == 'loadbalancer'">
        <div data-v-6e4bcd7a="" class="ep-input ep-input--large ep-input-group ep-input-group--prepend"
             style="width: auto;" v-if="!types.modifyFlay">
          <div class="ep-input-group__prepend" color="#606266">规则类型</div>
        </div>
        <el-select placeholder="请选择规则类型" v-model="requestParam.ruleType" size="large"
                   :disabled="types.modifyFlay" v-if="!types.modifyFlay"
                   @change="handlerChange(true)">
          <el-option v-for="item in rules.value" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
        <el-input size="large" v-model="ruleName.name"
                  placeholder="请选择规则类型"
                  style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(true)"
                  v-if="types.modifyFlay">
          <template #prepend>
            <span color="#606266">规则类型</span>
          </template>
        </el-input>
        <el-tooltip class="box-item" effect="dark"
                    content="插件支持的规则类型"
                    placement="right-start">
          <el-icon color="lightgray" size="20">
            <Warning/>
          </el-icon>
        </el-tooltip>
      </el-form-item>
      <el-form-item prop="sceneName"
                    v-if="requestParam.pluginType == 'flowcontrol' || requestParam.pluginType == 'loadbalancer'">
        <el-input size="large" v-model="requestParam.sceneName" placeholder="请输入规则场景名称"
                  style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)">
          <template #prepend>
            <span color="#606266">规则场景名称</span>
          </template>
        </el-input>
        <el-tooltip class="box-item" effect="dark"
                    content="流量匹配和具体流控规则的场景名称一致时流控规则才会生效"
                    placement="right-start" v-if="requestParam.pluginType == 'flowcontrol'">
          <el-icon color="lightgray" size="20">
            <Warning/>
          </el-icon>
        </el-tooltip>
        <el-tooltip class="box-item" effect="dark"
                    content="流量标记和负载均衡策略的场景一致时负载均衡策略生效"
                    placement="right-start" v-if="requestParam.pluginType == 'loadbalancer'">
          <el-icon color="lightgray" size="20">
            <Warning/>
          </el-icon>
        </el-tooltip>
      </el-form-item>
      <div v-if="(requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'
      && requestParam.pluginType != 'tag-transmission') || types.configType == 'nacos'">
        <el-descriptions title="service.meta信息" style="margin-top: 15px;"></el-descriptions>
        <el-form-item v-if="types.configType == 'nacos'" prop="namespace">
          <el-input size="large" v-model.trim="requestParam.namespace" placeholder="请输入命名空间"
                    style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay">
            <template #prepend>
              <span color="#606266">project</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="该配置对应sermant配置文件中的service.meta.project"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item prop="application" v-if="requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'
                      && requestParam.pluginType != 'tag-transmission'">
          <el-input size="large" v-model.trim="requestParam.appName" placeholder="请输入应用名称"
                    style="width: 90%; margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266">application</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="该配置对应sermant配置文件中的service.meta.application"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item
            v-if="requestParam.pluginType == 'database-write-prohibition' || requestParam.pluginType == 'mq-consume-prohibition'">
          <el-input size="large" v-model.trim="requestParam.serviceName" placeholder="请输入服务名称，为空时下发全局配置"
                    style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266">service</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="该配置对应sermant配置文件中的service.meta.service"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item v-if="requestParam.pluginType != 'other' && requestParam.pluginType != 'flowcontrol'
                      && requestParam.pluginType != 'tag-transmission'">
          <el-input size="large" v-model.trim="requestParam.environment" placeholder="请输入环境名称"
                    style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266">environment</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="该配置对应sermant配置文件中的service.meta.environment"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item prop="zone"
            v-if="requestParam.pluginType == 'database-write-prohibition' || requestParam.pluginType == 'mq-consume-prohibition'">
          <el-input size="large" v-model.trim="requestParam.zone" placeholder="请输入区域名称"
                    style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)">
            <template #prepend>
              <span color="#606266">zone</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="该配置对应sermant配置文件中的service.meta.zone"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
      </div>
      <div v-if="requestParam.pluginType != 'database-write-prohibition' && requestParam.pluginType != 'mq-consume-prohibition'
                       && requestParam.pluginType != 'removal' && requestParam.pluginType != 'other'
                       && requestParam.pluginType != 'tag-transmission'">
        <el-descriptions title="服务信息" style="margin-top: 15px;">
        </el-descriptions>
        <el-form-item prop="serviceName">
          <el-input size="large" v-model.trim="requestParam.serviceName" placeholder="请输入服务名称，为空时下发全局配置"
                    style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)" v-if="requestParam.pluginType == 'router'">
            <template #prepend>
              <span color="#606266">service</span>
            </template>
          </el-input>
          <el-input size="large" v-model.trim="requestParam.serviceName" placeholder="请输入服务名称"
                    style="width: 90%;margin-right: 5px;" :disabled="types.modifyFlay" @change="handlerChange(false)" v-if="requestParam.pluginType != 'router'">
            <template #prepend>
              <span color="#606266">service</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="微服务的名称，由微服务配置文件的dubbo.application.name、spring.applicaton.name确定"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
      </div>
      <div>
        <el-descriptions title="配置项信息" style="margin-top: 15px;">
        </el-descriptions>
        <el-form-item prop="group">
          <el-input size="large" v-model.trim="requestParam.group" placeholder="请输入配置项的group"
                    style="width: 90%;margin-right: 5px;"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)" v-if="requestParam.pluginType == 'other'">
            <template #prepend>
              <span color="#606266">group</span>
            </template>
          </el-input>
          <el-input size="large" v-model.trim="requestParam.group" placeholder=""
                    style="width: 90%;margin-right: 5px;"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)" v-if="requestParam.pluginType != 'other'">
            <template #prepend>
              <span color="#606266">group</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="配置sermant原生插件配置时，自动生成，无需修改"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item prop="key">
          <el-input size="large" v-model.trim="requestParam.key" placeholder="请输入配置项名称"
                    style="width: 90%;margin-right: 5px;"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)" v-if="requestParam.pluginType == 'other'">
            <template #prepend>
              <span color="#606266">key</span>
            </template>
          </el-input>
          <el-input size="large" v-model.trim="requestParam.key" placeholder=""
                    style="width: 90%;margin-right: 5px;"
                    :disabled="types.modifyFlay || requestParam.pluginType !='other'"
                    @change="handlerChange(false)" v-if="requestParam.pluginType != 'other'">
            <template #prepend>
              <span color="#606266">key</span>
            </template>
          </el-input>
          <el-tooltip class="box-item" effect="dark" content="配置sermant原生插件配置时，自动生成，无需修改"
                      placement="right-start">
            <el-icon color="lightgray" size="20">
              <Warning/>
            </el-icon>
          </el-tooltip>
        </el-form-item>
      </div>
      <el-descriptions title="配置内容" style="margin-top: 15px;">
      </el-descriptions>
      <el-form-item style="display: flex;">
        <el-input v-model="requestParam.content" style="width: 90%;padding-right: 5px;" :autosize="{ minRows: 8 }"
                  type="textarea"
                  placeholder="请输入配置内容"
        />
        <el-tooltip class="box-item" effect="dark" content="sermant原生插件需使用yaml格式"
                    placement="right-start">
          <el-icon color="lightgray" size="20">
            <Warning/>
          </el-icon>
        </el-tooltip>
      </el-form-item>
      <div style="display: flex;align-items: center; margin-left: 30%; margin-top: 30px;width: 90%;">
        <el-button type="primary" size="large" style="width: 20%;" shouldAddSpace="true;"
                   @click="submit(ruleFormRef)">
          提交
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, reactive, ref} from "vue";
import {LocationQuery, useRouter} from "vue-router";
import axios from "axios";
import {Warning} from '@element-plus/icons-vue'
import {ElMessage, FormInstance, FormRules} from "element-plus";

const ruleFormRef = ref<FormInstance>()

const options = [
  {label: '路由插件配置', value: 'router',},
  {label: 'springboot注册插件配置', value: 'springboot-registry',},
  {label: '注册迁移插件配置', value: 'service-registry',},
  {label: '流控插件配置', value: 'flowcontrol',},
  {label: '离群实例摘除插件配置', value: 'removal',},
  {label: '负载均衡插件配置', value: 'loadbalancer',},
  {label: '标签透传插件配置', value: 'tag-transmission',},
  {label: '消息队列禁止消费', value: 'mq-consume-prohibition',},
  {label: '数据库禁写插件配置', value: 'database-write-prohibition',},
  {label: '其他配置', value: 'other',},
]

const flowcontrolRules = [
  {label: '流量匹配规则', value: 'matchGroup',},
  {label: '限流规则', value: 'rateLimiting',},
  {label: '熔断规则', value: 'circuitBreaker',},
  {label: '隔离规则', value: 'bulkhead',},
  {label: '错误注入', value: 'faultInjection',},
  {label: '重试', value: 'retry',},
  {label: '系统级流控', value: 'system',},
]

const pluginName = reactive({name: '路由插件配置'});

const ruleName = reactive({name: '流量匹配规则'});

const rules = reactive({
  value: [{label: '流量匹配规则', value: 'matchGroup'}]
});

const loadbalancerRules = [
  {label: '流量标记规则', value: 'matchGroup',},
  {label: '负载均衡规则', value: 'loadbalance',}
]

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
        message: "获取配置失败",
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: "获取配置失败",
      type: "error",
    });
  });
};

const types = reactive({
  modifyFlay: false,
  configType: "",
});

onMounted(() => {
  const param: LocationQuery = router.currentRoute.value.query;
  requestParam.ruleType = "matchGroup";
  types.configType = <string>param.configType;
  if (<string>param.type != "modify") {
    return;
  }
  types.modifyFlay = true;
  requestParam.pluginType = <string>param.pluginType;
  changeRules();
  pluginName.name = getLabelByValue(options, requestParam.pluginType);
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
  const value = requestParam.pluginType;
  if (value == "flowcontrol") {
    replaceArray(flowcontrolRules, rules.value);
  } else if (value == "loadbalancer") {
    replaceArray(loadbalancerRules, rules.value);
  }
  handlerChange(true);
}

const replaceArray = (source: { label: string; value: string; }[], target: { label: string; value: string; }[]) => {
  target.splice(0, target.length); // 清空数组 A
  source.forEach(item => {
    target.push(item); // 将数组 B 的内容添加到数组 A
  });
};

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
  if (value == "other") {
    requestParam.key = "";
    requestParam.group = "";
    requestParam.content = "";
  }
}

const checkServiceName = (rule: any, value: any, callback: any) => {
  if (requestParam.pluginType != 'flowcontrol') {
    return callback();
  }
  if (!value) {
    return callback(new Error('请输入服务名称'));
  }
  return callback();
}

const checkRule = reactive<FormRules>({
  application: [
    {required: true, message: '请输入应用名称', trigger: 'blur'}
  ],
  pluginType: [
    {required: true, message: '请选择插件类型', trigger: 'blur'}
  ],
  ruleType: [
    {required: true, message: '请选择规则类型', trigger: 'blur'}
  ],
  sceneName: [
    {required: true, message: '请输入场景名称', trigger: 'blur'}
  ],
  group: [
    {required: true, message: '请输入group名称', trigger: 'blur'}
  ],
  key: [
    {required: true, message: '请输入配置key', trigger: 'blur'}
  ],
  nacos: [
    {required: true, message: '请输入命名空间', trigger: 'blur'}
  ],
  zone: [
    {required: true, message: '请输入区域名称', trigger: 'blur'}
  ],
  serviceName: [
    {validator: checkServiceName, message: '请输入服务名称', trigger: 'blur'}
  ],
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
        message: "新增配置成功",
        type: "success",
      });
    } else {
      ElMessage({
        message: "新增配置失败",
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: "新增配置失败",
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
        message: "更新配置成功",
        type: "success",
      });
    } else {
      ElMessage({
        message: "更新配置失败",
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: "更新配置失败",
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
  width: 350px;
}
</style>