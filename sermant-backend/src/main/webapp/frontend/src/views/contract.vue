<template>
  <div class="hi">
    <table border="1">
      <thead>
      <tr>
        <th>应用名</th>
        <th>组名</th>
        <th>版本号</th>
        <th>环境</th>
        <th>命名空间</th>
        <th>区域</th>
        <th>方法路径</th>
        <th>接口信息</th>
        <th>方法名</th>
        <th>参数信息</th>
        <th>返回值类型</th>
      </tr>
      </thead>
      <tbody v-for='serverInfo in serverInfos'>
      <span v-for='contract in serverInfo.contractList' style="display: contents">
          <tr v-for="method in contract.methodInfoList">
            <td>{{ serverInfo.applicationName }}</td>
            <td>{{ serverInfo.groupName }}</td>
            <td>{{ serverInfo.version }}</td>
            <td>{{ serverInfo.environment }}</td>
            <td>{{ serverInfo.project }}</td>
            <td>{{ serverInfo.zone }}</td>
            <td>{{ contract.url }}</td>
            <td>{{ contract.interfaceName }}</td>
            <td>{{ method.name }}</td>
            <td>
              <span v-for="(paramInfo, index) in method.paramInfoList">
                <span v-if="index == 0">{{paramInfo.paramType}}</span>
                <span v-if="index != 0">,{{paramInfo.paramType}}</span>
              </span>
            </td>
            <td>
              <span v-if="method.returnInfo != null">{{method.returnInfo.paramType}}</span>
            </td>
          </tr>
        </span>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'Menu-view',
  data() {
    return {
      serverInfos: []
    }
  },
  mounted() {
    this.$http.get('http://127.0.0.1:8900/visibility/getCollectorInfo').then((response) => {
      console.info(response.body)
      this.serverInfos = response.body
    }, (response) => {
      console.error(response)
    })
  }
}
</script>

<style scoped>
</style>
