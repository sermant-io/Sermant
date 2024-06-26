<script lang="ts" setup>
import {toggleDark} from "~/composables";
import {useRouter} from "vue-router";
import {ref} from "vue";
import {switchLocale} from '~/composables/translations';

const activeName = ref("first");

const router = useRouter();

const toHome = () => {
  router.replace("/");
};

const toInstances = () => {
  router.replace("/instances");
};

const toEvents = () => {
  router.replace("/events");
};

const toEventsConfig = () => {
  router.replace("/events-config");
};

const toConfig = () => {
  router.replace("/config");
};

const getAssetsImge = (url: string) => {
  return new URL(`../../assets/${url}`, import.meta.url).href;
};
</script>

<template>
  <el-menu class="el-menu-demo" mode="horizontal">
    <el-image
        style="width: 15vh; margin-left: 10px"
        :src="getAssetsImge('Sermant-logo.png')"
        :fit="'scale-down'"
    >
    </el-image>
    <el-menu-item index="2" @click="toInstances"> {{ $t('header.instanceStatus') }}</el-menu-item>
    <el-sub-menu index="3">
      <template #title>{{ $t('header.eventManagement') }}</template>
      <el-menu-item index="3-1" @click="toEvents"> {{ $t('header.monitoring') }}</el-menu-item>
      <el-menu-item index="3-2" @click="toEventsConfig">{{ $t('header.configuration') }}</el-menu-item>
    </el-sub-menu>
    <el-menu-item index="4" @click="toConfig">{{ $t('header.configurationManagement') }}</el-menu-item>

    <el-sub-menu index="5">
      <template #title>
        <i class="el-icon el-tooltip__trigger" data-v-dd9c9540="" style="font-size: 24px;">
          <svg preserveAspectRatio="xMidYMid meet" viewBox="0 0 24 24" width="1.2em" height="1.2em" data-v-dd9c9540="">
            <path fill="currentColor"
                  d="m18.5 10l4.4 11h-2.155l-1.201-3h-4.09l-1.199 3h-2.154L16.5 10h2zM10 2v2h6v2h-1.968a18.222 18.222 0
                  0 1-3.62 6.301a14.864 14.864 0 0 0 2.336 1.707l-.751 1.878A17.015 17.015 0 0 1 9 13.725a16.676 16.676
                  0 0 1-6.201 3.548l-.536-1.929a14.7 14.7 0 0 0 5.327-3.042A18.078 18.078 0 0 1 4.767
                  8h2.24A16.032 16.032 0 0 0 9 10.877a16.165 16.165 0 0 0 2.91-4.876L2 6V4h6V2h2zm7.5 10.885L16.253
                  16h2.492L17.5 12.885z">
            </path>
          </svg>
        </i>
      </template>
      <el-menu-item index="5-1" @click="switchLocale('zh')">中文</el-menu-item>
      <el-menu-item index="5-2" @click="switchLocale('en')">English</el-menu-item>
    </el-sub-menu>
    <el-menu-item h="full" @click="toggleDark()">
      <button
          class="border-none w-full bg-transparent cursor-pointer"
          style="height: var(--ep-menu-item-height)"
      >
        <i inline-flex i="dark:ep-moon ep-sunny"/>
      </button>
    </el-menu-item>
  </el-menu>
</template>
