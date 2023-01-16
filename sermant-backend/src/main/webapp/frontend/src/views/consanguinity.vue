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
        <th>服务提供者IP或域名</th>
        <th>服务提供者端口</th>
      </tr>
      </thead>
      <tbody v-for='serverInfo in serverInfos'>
      <span v-for='consanguinity in serverInfo.consanguinityList' style="display: contents">
          <tr v-for="provider in consanguinity.providers">
            <td>{{ serverInfo.applicationName }}</td>
            <td>{{ serverInfo.groupName }}</td>
            <td>{{ serverInfo.version }}</td>
            <td>{{ serverInfo.environment }}</td>
            <td>{{ serverInfo.project }}</td>
            <td>{{ serverInfo.zone }}</td>
            <td>{{ consanguinity.url }}</td>
            <td>{{ consanguinity.interfaceName }}</td>
            <td>{{ provider.ip }}</td>
            <td>{{ provider.port }}</td>
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
