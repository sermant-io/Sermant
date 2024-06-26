<template>
  <div style="width: 100%">
    <el-page-header :title="$t('eventConfigView.event')" @back="goBack">
      <template #content>
        <span class="font-600 mr-3"> {{ $t('eventConfigView.configuration') }} </span>
      </template>
    </el-page-header>
    <h2>{{ $t('eventConfigView.webHookConfiguration') }}</h2>
    <el-table :data="webhooks" :table-layout="'auto'">
      <el-table-column prop="address" :label="$t('eventConfigView.switch')" width="80">
        <template #default="scope">
          <el-switch v-model="scope.row.enable" @change="setWebHook(scope.row)"/>
        </template>
      </el-table-column>
      <el-table-column prop="name" :label="$t('eventConfigView.type')"/>
      <el-table-column prop="url" :label="$t('eventConfigView.address')">
        <template #default="scope">
          <el-input
              @change="setWebHook(scope.row)"
              @blur="closeEditWebHook(scope.row)"
              v-model="scope.row.url"
              :disabled="!scope.row.canEdit"
          />
        </template>
      </el-table-column>
      <el-table-column prop="address" :label="$t('eventConfigView.operation')">
        <template #default="scope">
          <el-button size="large" @click="editWebHook(scope.row)">{{ $t('eventConfigView.edit') }}</el-button>
          <el-button size="large" @click="testWebHook(scope.row)">{{ $t('eventConfigView.testConnection') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import {onBeforeMount, reactive, ref, watch} from "vue";
import {useRouter} from "vue-router";
import axios from "axios";
import i18n from "~/composables/translations";

const router = useRouter();

const count = ref(0);

const value1 = ref(true);

const webhooks = reactive([
  {
    id: 0,
    name: i18n.global.t('eventConfigView.feiShu'),
    url: "",
    enable: false,
    canEdit: false,
  },
  {
    id: 1,
    name: i18n.global.t('eventConfigView.dingTalk'),
    url: "",
    enable: false,
    canEdit: false,
  },
  // 暂不支持welink
  // {
  //   id: 2,
  //   name: "welink",
  //   url: "",
  //   enable: false,
  //   canEdit: false,
  // },
]);

const goBack = () => {
  router.push("/events");
};

onBeforeMount(() => {
  getWebHooks();
});

const getWebHooks = () => {
  axios
      .get(`${window.location.origin}/sermant/event/webhooks`)
      .then(function (response) {
        ElMessage({
          message: i18n.global.t('eventConfigView.successfullyObtainedWebhookConfiguration'),
          type: "success",
        });
        const data = response.data;
        for (let index = 0; index < data.webhooks.length; index++) {
          console.log(data.webhooks[index].id)
          console.log(webhooks)
          console.log(webhooks[0])
          console.log(webhooks[data.webhooks[index].id])
          webhooks[data.webhooks[index].id].url = data.webhooks[index].url;
          webhooks[data.webhooks[index].id].enable = data.webhooks[index].enable;
        }
      })
      .catch(function (error) {
        ElMessage({
          message: i18n.global.t('eventConfigView.failedToObtainWebHookConfiguration'),
          type: "error",
        });
        console.log(error);
      });
};

const editWebHook = (webhook) => {
  webhook.canEdit = true;
};

const closeEditWebHook = (webhook) => {
  webhook.canEdit = false;
};

const setWebHook = (webhook) => {
  console.log(webhook);
  webhook.canEdit = false;
  axios
      .put(`${window.location.origin}/sermant/event/webhooks/` + webhook.id, {
        url: webhook.url,
        enable: webhook.enable,
      })
      .then(function (response) {
        const data = response.data;
        if (data) {
          ElMessage({
            message: i18n.global.t('eventConfigView.success'),
            type: "success",
          });
        } else {
          ElMessage({
            message: i18n.global.t('eventConfigView.failed'),
            type: "error",
          });
        }
      })
      .catch(function (error) {
        ElMessage({
          message: i18n.global.t('eventConfigView.failed'),
          type: "error",
        });
        console.log(error);
      });
};

const testWebHook = (webhook) => {
  axios
      .post(`${window.location.origin}/sermant/event/webhooks/test`, {
        id: webhook.id,
      })
      .then(function (response) {
        ElMessage({
          message: i18n.global.t('eventConfigView.successfullyInitiatedWebHookTest'),
          type: "success",
        });
      })
      .catch(function (error) {
        ElMessage({
          message: i18n.global.t('eventConfigView.failedToInitiateWebHookTest'),
          type: "error",
        });
        console.log(error);
      });
};

watch(() => i18n.global.locale, (newLocale, oldLocale) => {
  webhooks[0].name = i18n.global.t('eventConfigView.feiShu');
  webhooks[1].name = i18n.global.t('eventConfigView.dingTalk');
});
</script>
