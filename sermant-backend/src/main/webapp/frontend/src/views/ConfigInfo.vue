<template>
  <div style="width: 100%">
    <el-page-header :title="$t('configInfo.configurationManagement')" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> {{ $t('configInfo.configurationDetail') }} </span>
      </template>
    </el-page-header>
    <el-form :inline="true" :model="configInfo" label-width="auto" ref="ruleFormRef" :rules="checkRule"
             style="max-width: 80%; margin-top: 2%;margin-bottom: 1%; margin-left: 2%;"
             :validate-on-rule-change="false">
      <el-descriptions :title="$t('configInfo.pluginInformation')" style="margin-top: 1%;"></el-descriptions>
      <el-form-item prop="pluginType">
        <div data-v-6e4bcd7a="" class="ep-input ep-input--large ep-input-group ep-input-group--prepend"
             style="width: auto;">
          <div class="ep-input-group__prepend" color="#606266">{{ $t('configInfo.type') }}</div>
        </div>
        <el-select v-model="configInfo.pluginType" :placeholder="$t('configInfo.selectPlugin')" size="large"
                   @change="changePluginType()" :disabled="modifyFlag">
          <el-option v-for="item in options" :label="language == 'zh' ? item.label:item.value" :value="item.value"/>
        </el-select>
      </el-form-item>
      <div v-for="template in templates">
        <div v-if="template.plugin.englishName == configInfo.pluginType">
          <el-descriptions :title="$t('configInfo.configElement')" style="margin-top: 1%;"></el-descriptions>
          <el-form-item v-for="element in template.elements" :prop="element.name">
            <el-input v-if="!element.values?.length" size="large"
                      :placeholder="language == 'zh' ? element.placeholder?.chineseDesc: element.placeholder?.englishDesc"
                      v-model="configInfo[element.name]" @change="changeElement(template)" :disabled="modifyFlag">
              <template #prepend>
                <span color="#606266" class="form-text">{{
                    language == 'zh' && element.chineseDesc ? element.chineseDesc : element.name
                  }}</span>
              </template>
            </el-input>
            <div v-else class="ep-input ep-input--large ep-input-group ep-input-group--prepend">
              <div class="ep-input-group__prepend form-text" color="#606266">{{ $t('configInfo.ruleType') }}</div>
              <el-select v-model="configInfo[element.name]"
                         :placeholder="language == 'zh'?element.placeholder?.chineseDesc:element.placeholder?.englishDesc"
                         size="large" @change="changeElement(template)" :disabled="modifyFlag">
                <el-option v-for="item in element.values" :label="language == 'zh'?item.chineseDesc:item.englishDesc"
                           :value="item.name"/>
              </el-select>
            </div>
            <TooltipIcon :content="language == 'zh'?element.notice?.chineseDesc:element.notice?.englishDesc"/>
          </el-form-item>
          <el-descriptions :title="$t('configInfo.configRule')" style="margin-top: 1%;"></el-descriptions>
          <el-form-item prop="groupRule">
            <div class="ep-input ep-input--large ep-input-group ep-input-group--prepend">
              <div class="ep-input-group__prepend form-text" color="#606266">{{ $t('configInfo.groupRule') }}</div>
              <el-select v-model="configInfo.groupRule"
                         :placeholder=" $t('configInfo.groupRulePlaceholder')"
                         size="large" @change="changeRule(template)" :disabled="modifyFlag">
                <el-option v-for="item in template.groupRule" :label="item" :value="item"/>
              </el-select>
            </div>
            <TooltipIcon :content="$t('configInfo.keyRuleNotice')"/>
          </el-form-item>
          <el-form-item prop="keyRule">
            <div class="ep-input ep-input--large ep-input-group ep-input-group--prepend">
              <div class="ep-input-group__prepend form-text" color="#606266">{{ $t('configInfo.keyRule') }}</div>
              <el-select v-model="configInfo.keyRule"
                         :placeholder=" $t('configInfo.keyRulePlaceholder')"
                         size="large" @change="changeRule(template)" :disabled="modifyFlag">
                <el-option v-for="item in template.keyRule" :label="item" :value="item"/>
              </el-select>
            </div>
            <TooltipIcon :content="$t('configInfo.keyRuleNotice')"/>
          </el-form-item>
        </div>
      </div>
      <el-descriptions :title="$t('configInfo.configurationInformation')" style="margin-top: 15px;"/>
      <el-form-item prop="namespace" v-if="configInfo.configType == 'nacos'">
        <el-input size="large" v-model.trim="configInfo.namespace"
                  :placeholder="$t('configInfo.inputProjectName')"
                  class="input-style"
                  :disabled="modifyFlag">
          <template #prepend>
            <span color="#606266" class="form-text">project</span>
          </template>
        </el-input>
        <TooltipIcon :content="$t('configInfo.projectNotice')"/>
      </el-form-item>
      <el-form-item prop="group">
        <el-input size="large" v-model.trim="configInfo.group"
                  :placeholder="$t('configInfo.inputConfigurationGroup')"
                  class="input-style"
                  :disabled="configInfo.pluginType != 'common' || modifyFlag">
          <template #prepend>
            <span color="#606266" class="form-text">group</span>
          </template>
        </el-input>
        <TooltipIcon :content="$t('configInfo.configurationNotice')"/>
      </el-form-item>
      <el-form-item prop="key">
        <el-input size="large" v-model.trim="configInfo.key"
                  :placeholder="$t('configInfo.inputConfigurationName')"
                  class="input-style"
                  :disabled="configInfo.pluginType != 'common' || modifyFlag">
          <template #prepend>
            <span color="#606266" class="form-text">key</span>
          </template>
        </el-input>
        <TooltipIcon :content="$t('configInfo.configurationNotice')"/>
      </el-form-item>
      <el-descriptions :title="$t('configInfo.configurationContent')" style="margin-top: 15px;">
      </el-descriptions>
      <el-form-item style="display: flex;">
        <el-input v-model="configInfo.content" style="width: 90%;padding-right: 5px;" :autosize="{ minRows: 8 }"
                  type="textarea"
                  :placeholder="$t('configInfo.inputConfigurationContent')">
          <template #prepend>
            <span color="#606266" class="form-text"></span>
          </template>
        </el-input>
        <TooltipIcon :content="$t('configInfo.configurationContentNotice')"/>
      </el-form-item>
      <div style="display: flex;align-items: center; margin-left: 30%; margin-top: 30px;width: 90%;">
        <el-button type="primary" size="large" style="width: 20%;" shouldAddSpace="true;" @click="submit(ruleFormRef)">
          {{ $t('configInfo.submit') }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import {ref, watch} from "vue";
import axios from "axios";
import {ElMessage, FormInstance} from "element-plus";
import {LocationQuery, useRouter} from "vue-router";
import TooltipIcon from "~/components/layouts/TooltipIcon.vue";
import i18n from "~/composables/translations";
import {resultCodeMap} from "~/composables/config";

const router = useRouter();
const ruleFormRef = ref<FormInstance>()
const templates = ref<pluginTemplate[]>([]);
type stringMap = Record<string, string>;
const modifyFlag = ref<boolean>(false);
const configInfo = ref<stringMap>({
  pluginType: '',
  namespace: '',
  appName: '',
  serviceName: '',
  environment: '',
  zone: '',
  group: '',
  key: '',
  content: '',
  groupRule: '',
  keyRule: '',
  configType: ''
});

const options = ref<option[]>([]);

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

interface option {
  label: string,
  value: string
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

const getTemplate = () => {
  axios.get(`${window.location.origin}/sermant/templates`).then(function (response) {
    if (response.data.code == "00") {
      templates.value = response.data.data;
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
      checkRule.value = initRule.value;
      initPageTemplate();
    } else {
      ElMessage({
        message: i18n.global.t('common.failedToObtainTemplate'),
        type: "error",
      });
    }
  }).catch(function () {
    ElMessage({
      message: i18n.global.t('common.failedToObtainTemplate'),
      type: "error",
    });
  });
};

const submit = (formEl: FormInstance | undefined) => {
  formEl?.validate((valid) => {
    console.log(valid);
    if (!valid) {
      return;
    }
    if (modifyFlag.value) {
      updateConfig();
      return;
    }
    addConfig();
  })
};

interface rule {
  required: boolean;
  message: string;
  trigger: string;
}

interface checkRule {
  [key: string]: rule[];
}

const initRule = ref<checkRule>({
  group: [
    {required: true, message: i18n.global.t('configInfo.inputConfigurationGroup'), trigger: 'change'}
  ],
  key: [
    {required: true, message: i18n.global.t('configInfo.inputConfigurationName'), trigger: 'change'}
  ],
  pluginType: [
    {required: true, message: i18n.global.t('configInfo.selectPlugin'), trigger: 'change'}
  ],
  keyRule: [
    {required: true, message: i18n.global.t('configInfo.keyRulePlaceholder'), trigger: 'change'}
  ],
  groupRule: [
    {required: true, message: i18n.global.t('configInfo.groupRulePlaceholder'), trigger: 'change'}
  ],
  namespace: [
    {required: true, message: i18n.global.t('configInfo.inputProjectName'), trigger: 'change'}
  ],
});

const checkRule = ref<checkRule>({});

const goBack = () => {
  router.push("/config");
};

const language = ref(i18n.global.locale.toString());

watch(() => i18n.global.locale, () => {
  language.value = i18n.global.locale.toString();
  templates.value.forEach(template => {
    template.elements?.forEach(element => {
      let message = language.value === 'zh' ? element.placeholder?.chineseDesc : element.placeholder?.englishDesc;
      if (checkRule.hasOwnProperty(element.name)) {
        checkRule.value[element.name][0].message = message;
      }
    })
  });
  checkRule.value.pluginType[0].message = i18n.global.t('configInfo.selectPlugin');
  checkRule.value.keyRule[0].message = i18n.global.t('configInfo.keyRulePlaceholder');
  checkRule.value.groupRule[0].message = i18n.global.t('configInfo.groupRulePlaceholder');
  ruleFormRef?.value?.clearValidate()
});

getTemplate();

function initPageTemplate() {
  const param: LocationQuery = router.currentRoute.value.query;
  if (<string>param.type != "modify") {
    return;
  }
  modifyFlag.value = true
  configInfo.value.pluginType = <string>param.pluginType;
  configInfo.value.group = <string>param.group;
  configInfo.value.key = <string>param.key;
  configInfo.value.namespace = <string>param.namespace;
  configInfo.value.configType = <string>param.configType;
  configInfo.value.keyRule = <string>param.keyRule;
  configInfo.value.groupRule = <string>param.groupRule;
  console.log(param)
  getConfig();
  let template = templates.value.find(template =>
      template.plugin.englishName === configInfo.value.pluginType
  );
  if (!template) {
    return;
  }
  changeRule(template);
  extractVariables(configInfo.value.keyRule, <string>param.key);
  extractVariables(configInfo.value.groupRule, <string>param.group);
}

function extractVariables(template: string, instance: string) {
  const regex = /\$\{(\w+)\}/g;
  let match: RegExpExecArray | null;
  let name = '';
  let currantTemplate = template;
  while ((match = regex.exec(template)) !== null) {
    if (name) {
      let str = currantTemplate.substring(3 + name.length, match.index - template.length + currantTemplate.length)
      configInfo.value[name] = instance.substring(0, instance.indexOf(str));
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
    configInfo.value[name] = instance.substring(0, instance.lastIndexOf(str))
  } else {
    configInfo.value[name] = instance
  }
}

function changeElement(template: pluginTemplate) {
  if (!configInfo.value.keyRule || !configInfo.value.groupRule) {
    return;
  }
  configInfo.value.group = replaceTemplate(configInfo.value.groupRule, configInfo.value);
  configInfo.value.key = replaceTemplate(configInfo.value.keyRule, configInfo.value);
  template.configTemplates.forEach(configTemplate => {
    const pattern = new RegExp(configTemplate.key.replace("*", ".*"));
    if (pattern.test(configInfo.value.key)) {
      configInfo.value.content = configTemplate.value
    }
  })
}

function replaceTemplate(template: string, values: stringMap) {
  return template.replace(/\$\{\s*(\w+)\s*\}/g, (_, key) => values[key] || '');
}

function changePluginType() {
  let template = templates.value.find(template =>
      template.plugin.englishName === configInfo.value.pluginType
  );
  if (!template) {
    return
  }
  configInfo.value = {
    content: configInfo.value.content,
    namespace: configInfo.value.namespace,
    pluginType: configInfo.value.pluginType,
    keyRule: template.keyRule[0],
    groupRule: template.groupRule[0]
  };
  changeRule(template);
  changeElement(template);
}

function initCheckRule(template: pluginTemplate) {
  template.elements?.forEach(element => {
    let message = language.value === 'zh' ? element.placeholder?.chineseDesc : element.placeholder?.englishDesc;
    if ((containsPlaceholder(element.name, configInfo.value.keyRule)
        || containsPlaceholder(element.name, configInfo.value.groupRule)) && element.required) {
      checkRule.value[element.name] = [{required: true, message: message, trigger: 'change'}];
    }
  })
}

function containsPlaceholder(str: string, content: string) {
  const pattern = new RegExp(`\\$\\{\\s*${str}\\s*\\}`, 'i');
  return pattern.test(content);
}

function changeRule(template: pluginTemplate) {
  checkRule.value = initRule.value;
  initCheckRule(template);
}

const getConfig = () => {
  axios.get(`${window.location.origin}/sermant/config`, {
    params: {
      key: configInfo.value.key,
      group: configInfo.value.group,
      namespace: configInfo.value.namespace
    }
  }).then(function (response) {
    if (response.data.code == "00") {
      configInfo.value.content = response.data.data.content;
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

const addConfig = () => {
  axios.post(`${window.location.origin}/sermant/config`, {
    key: configInfo.value.key,
    group: configInfo.value.group,
    content: configInfo.value.content,
    namespace: configInfo.value.namespace
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
    key: configInfo.value.key,
    group: configInfo.value.group,
    content: configInfo.value.content,
    namespace: configInfo.value.namespace
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

.ep-input-group {
  width: auto;
}

:deep(.ep-input__wrapper) {
  width: 260px;
}

:deep(.ep-input-group__prepend) {
  width: 90px;
}
</style>