<script setup lang="ts">
import { reactive } from "@vue/reactivity";
import axios from "axios";
import { Search } from "@element-plus/icons-vue";
import { onMounted } from "@vue/runtime-core";
interface TableData {
  appName: string;
  heartbeatTime: string;
  instanceId: string;
  ip: string;
  lastHeartbeatTime: string;
  pluginInfoMap: any[];
  version: string;
  health: boolean;
  ipAddress: string;
}

interface InnerData {
  appName: string;
  heartbeatTime: string;
  instanceId: string;
  ip: string[];
  lastHeartbeatTime: string;
  pluginInfoMap: any;
  version: string;
  health: boolean;
}

let innerData: InnerData[] = [];
let showData: InnerData[] = [];
const state: {
  total: number;
  ipAddress: string;
  version: string;
  appName: string;
  tableData: TableData[];
  loading: boolean;
  currentPage: number;
  normalCount: number;
  abnormalCount: number;
  pageSize: number;
} = reactive({
  total: 0,
  ipAddress: "",
  version: "",
  appName: "",
  tableData: [],
  loading: false,
  currentPage: 1,
  normalCount: 0,
  abnormalCount: 0,
  pageSize: 10,
});

onMounted(() => {
  getTableData();
});
async function getTableData() {
  try {
    state.loading = true;
    const res = await axios.get(`${window.location.origin}/sermant/getPluginsInfo`);
    innerData = res.data;
    innerData.forEach((item) => {
      if (item.health) {
        state.normalCount++;
      } else {
        state.abnormalCount++;
      }
    });
    showData = JSON.parse(JSON.stringify(res.data));
    state.total = showData.length;
    const result = JSON.parse(JSON.stringify(showData.slice(0, state.pageSize)));
    state.tableData = handle(result);
  } catch (err) {
  } finally {
    state.loading = false;
  }
}
function handle(result: any[]) {
  result.forEach((item: any) => {
    if (item.ip instanceof Array) {
      // item.ip = item.ip.join(",");
      item.ipAddress=item.ip.join(',');
    }
    const heartbeatDate=new Date(item.heartbeatTime);
    item.heartbeatTime = `${heartbeatDate.getFullYear()}-${(heartbeatDate.getMonth()+1)}-${heartbeatDate.getDate()} ${heartbeatDate.getHours()}:${heartbeatDate.getMinutes()}:${heartbeatDate.getSeconds()}`
  });
  return result;
}

function pageChange(page: number) {
  state.currentPage = page;
  if (page * state.pageSize <= showData.length) {
    const result = JSON.parse(
      JSON.stringify(showData.slice((page - 1) * state.pageSize, page * state.pageSize))
    );
    state.tableData = handle(result);
  } else {
    const result = JSON.parse(
      JSON.stringify(showData.slice((page - 1) * state.pageSize))
    );
    state.tableData = handle(result);
  }
}

function search() {
  state.loading = true;
  if (!state.ipAddress && !state.version && !state.appName) {
    showData = JSON.parse(JSON.stringify(innerData));
  } else {
    showData = [];
    innerData.forEach((item) => {
      const isMatch =
        item.appName.toLowerCase().indexOf(state.appName.toLowerCase()) !== -1 &&
        item.ip.join(",").toLowerCase().indexOf(state.ipAddress.toLowerCase()) !== -1 &&
        item.version.toLowerCase().indexOf(state.version.toLowerCase()) !== -1;
      if (isMatch) {
        showData.push(item);
      }
    });
  }
  state.total = showData.length;
  pageChange(1);
  state.loading = false;
}

function searchHealth(health:string){
  state.loading=true;
  showData=[];
if(health==='normal'){
  innerData.forEach((item)=>{
    if(item.health){
      showData.push(item);
    }
  })
}else{
  innerData.forEach((item)=>{
    if(!item.health){
      showData.push(item)
    }
  })
}
state.total=showData.length;
pageChange(1);
state.loading=false
}
</script>

<template>
  <main>
  <div class="logo-box">
  <img src="../assets/img/sermant-logo.png"/>
  </div>
    <div class="header">
      <div class="left-area">
        <el-card class="normal" @click="searchHealth('normal')">
          {{ state.normalCount }}
        </el-card>
        <el-card class="abnormal" @click="searchHealth('abnormal')">
          {{ state.abnormalCount }}
        </el-card>
      </div>
      <div class="search-area">
        <el-row>
          <el-col :span="12">
            <div class="input-item">
              <div>IP:</div>
              <el-input v-model="state.ipAddress" placeholder="Please input"></el-input>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="input-item">
              <div>版本:</div>
              <el-input v-model="state.version" placeholder="Please input"></el-input>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <div class="input-item">
              <div>应用名:</div>
              <el-input v-model="state.appName" placeholder="Please input"></el-input>
            </div>
          </el-col>
          <el-col :span="12">
            <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
          </el-col>
        </el-row>
      </div>
    </div>
    <div class="center">
      <el-table
        :data="state.tableData"
        style="width: 100%"
        :border="true"
        v-loading="state.loading"
        stripe
      >
        <el-table-column prop="health" width="50">
          <template #default="scope">
            <div class="dot normal-dot" v-if="scope.row.health"></div>
            <div class="dot abnormal-dot" v-if="!scope.row.health"></div>
          </template>
        </el-table-column>
        <el-table-column prop="appName" label="应用名" width="100"> </el-table-column>
        <el-table-column prop="instanceId" label="实例ID" width="320"> </el-table-column>
        <el-table-column prop="version" label="版本" width="100"> </el-table-column>
        <el-table-column prop="ipAddress" label="IP" width="180"> </el-table-column>
        <el-table-column prop="heartbeatTime" label="心跳时间" width="180"> </el-table-column>
        <el-table-column label="插件">
          <template #default="props">
            <div class="plugin-box">
              <el-tag v-for="(plugin, index) in props.row.pluginInfoMap" :key="index"
                >{{ plugin.name }}:{{ plugin.version }}</el-tag
              >
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        layout="prev, pager, next"
        :total="state.total"
        @current-change="pageChange"
        :current-page="state.currentPage"
      >
      </el-pagination>
    </div>
    <div class="footer-box">
    <p></p>
    </div>
  </main>
</template>
<style lang="less" scoped>
main {
  width: 100%;
  height: 100%;
  margin: 0 auto;
  border: 1px solid #e7e7e7;
  // padding: 50px;
  background-color: #f5f5f573;
  border-radius: 3px;
  box-sizing: border-box;

  .logo-box{
    width:100%;
    height:60px;
    background-color:#ffffff;
   margin-bottom:20px;
    img{
      height:40px;
      margin-top:10px;
    }
  }
  .header {
    height: 400px;
    width: 100%;
    display: flex;
  }

  .left-area {
    width: 50%;
    display: flex;
    justify-content: space-around;

    .normal {
      color: green;
    }

    .abnormal {
      color: grey;
    }

    .el-card {
      width: calc(50% - 100px);
      height: 200px;
      font-size: 56px;
      font-weight: bold;
      text-align: center;
      line-height: 160px;
      cursor:pointer;
    }
  }
  .search-area {
    width:50%;

    .input-item {
      display: flex;
      align-items: center;

      div {
        margin-right: 10px;
        width: 80px;
      }

      .el-input {
        width: calc(100% - 80px);
      }
    }

    .el-col {
      padding: 0 50px;
    }

    .el-row {
      margin-top: 42px;
    }
  }

  .center {
    .el-pagination {
      margin: 0 auto;
      justify-content: center;
    }
  }
  .plugin-box {
    // margin-left: 50px;
    .el-tag {
      margin-left: 20px;
    }
  }
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin:0 auto;
}

.normal-dot {
  background-color: green;
}

.abnormal-dot {
  background-color: grey;
}

.footer-box{
  width:100%;
  height:60px;
  border-top:1px solid #e7e7e7;
  margin-top:20px;
  p{
    margin:20px auto;
    text-align:center;
  }
}
</style>
