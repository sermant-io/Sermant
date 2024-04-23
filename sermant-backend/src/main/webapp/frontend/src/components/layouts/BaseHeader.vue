<script lang="ts" setup>
import {toggleDark} from "~/composables";
import {useRouter} from "vue-router";
import {ref} from "vue";

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
    <el-menu-item index="2" @click="toInstances"> 实例状态 </el-menu-item>
    <el-sub-menu index="3">
      <template #title>事件管理</template>
      <el-menu-item index="3-1" @click="toEvents"> 监测 </el-menu-item>
      <el-menu-item index="3-2" @click="toEventsConfig"> 配置 </el-menu-item>
    </el-sub-menu>
    <el-menu-item h="full" @click="toggleDark()">
      <button
        class="border-none w-full bg-transparent cursor-pointer"
        style="height: var(--ep-menu-item-height)"
      >
        <i inline-flex i="dark:ep-moon ep-sunny" />
      </button>
    </el-menu-item>
  </el-menu>
</template>
