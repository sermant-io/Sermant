<template>
  <el-tooltip class="box-item" effect="dark" :content="content" placement="right-start">
    <el-icon color="lightgray" size="20">
      <QuestionFilled/>
    </el-icon>
  </el-tooltip>
</template>

<script setup>
import { defineProps } from 'vue';
import { ElTooltip, ElIcon } from 'element-plus';
import {QuestionFilled} from '@element-plus/icons-vue';

// 定义组件属性
const props = defineProps({
  content: {
    type: String,
    required: true
  }
});
</script>

<style scoped>
</style>