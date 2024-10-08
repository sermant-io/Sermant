<template>
  <el-config-provider namespace="ep" :locale="localeLanguage.value">
    <BaseHeader/>
    <div class="main">
      <RouterView/>
    </div>
  </el-config-provider>
</template>

<style>
#app {
  color: var(--ep-text-color-primary);
}

.main {
  display: flex;
  margin: 1vh;
}
</style>

<script lang="ts" setup>
import {localeLanguage} from '~/composables/translations';
</script>
