<template>
  <div class="hi">
    <table border="1">
      <td>ip</td>
      <td>version</td>
      <td>heartbeatTime</td>
      <td>plugin</td>
      <tr v-for='info in pluginsInfo' :key='info'>
        <td>{{ info.ip }}</td>
        <td>{{ info.version }}</td>
        <td>{{ info.heartbeatTime }}</td>
        <td>{{ info.pluginsMap}}</td>
      </tr>
    </table>
  </div>
</template>

<script>
export default {
  name: 'PluginsInfo',
  data () {
    return {
      pluginsInfo: []
    }
  },
  mounted () {
    this.$http.get('http://127.0.0.1:8900/sermant/getPluginsInfo').then((response) => {
      console.info(response.body)
      this.pluginsInfo = response.body
    }, (response) => {
      console.error(response)
    })
  }
}
</script>

<style scoped>
.hi {

}
</style>
