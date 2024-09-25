#
# Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

import requests
import time
import unittest


class TestConfig(unittest.TestCase):
    backend_server_address_use_zookeeper = "http://127.0.0.1:8901"
    backend_server_address_use_kie = "http://127.0.0.1:8902"
    backend_server_address_use_nacos = "http://127.0.0.1:8903"
    request_url_query_configs = "/sermant/configs"
    request_url_common = "/sermant/config"
    header = {'content-type': 'application/json'}
    add_router_config_param = {
        "key": "servicecomb.routeRule.spring-provider",
        "group": "app=default&environment=default",
        "content": "---\n" +
                   "- kind: routematcher.sermant.io/tag\n" +
                   "  description: sameTag\n" +
                   "  rules:\n" +
                   "    - precedence: 1\n" +
                   "      match:\n" +
                   "        tags:\n" +
                   "          zone:\n" +
                   "            exact: 'hangzhou'\n" +
                   "            caseInsensitive: false\n" +
                   "        policy:\n" +
                   "          triggerThreshold: 20\n" +
                   "          minAllInstances: 3\n" +
                   "      route:\n" +
                   "        - tags:\n" +
                   "            zone: CONSUMER_TAG",
        "namespace": "sermant"
    }
    query_router_configs_param = {
        "groupRule": "",
        "keyRule": "",
        "namespace": "sermant",
        "pluginType": "router"
    }
    query_router_config_param = {
        "key": "servicecomb.routeRule.spring-provider",
        "group": "app=default&environment=default",
        "namespace": "sermant"
    }
    update_router_config_param = {
        "key": "servicecomb.routeRule.spring-provider",
        "group": "app=default&environment=default",
        "content": "---\n" +
                   "- kind: routematcher.sermant.io/tag\n" +
                   "  description: sameTag\n" +
                   "  rules:\n" +
                   "    - precedence: 1\n" +
                   "      match:\n" +
                   "        tags:\n" +
                   "          zone:\n" +
                   "            exact: 'guizhou'\n" +
                   "            caseInsensitive: false\n" +
                   "        policy:\n" +
                   "          triggerThreshold: 20\n" +
                   "          minAllInstances: 3\n" +
                   "      route:\n" +
                   "        - tags:\n" +
                   "            zone: CONSUMER_TAG",
        "namespace": "sermant"
    }
    delete_router_config_param = {
        "key": "servicecomb.routeRule.spring-provider",
        "group": "app=default&environment=default",
        "namespace": "sermant"
    }

    add_springboot_config_param = {
        "key": "sermant.plugin.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "content": "strategy: all",
        "namespace": "sermant"
    }
    query_springboot_configs_param = {
        "groupRule": "",
        "keyRule": "sermant.plugin.registry",
        "namespace": "sermant",
        "pluginType": "springboot-registry"
    }
    query_springboot_config_param = {
        "key": "sermant.plugin.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "namespace": "sermant"
    }
    update_springboot_config_param = {
        "key": "sermant.plugin.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "content": "strategy: white",
        "namespace": "sermant"
    }
    delete_springboot_config_param = {
        "key": "sermant.plugin.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "namespace": "sermant"
    }

    add_service_registry_config_param = {
        "key": "sermant.agent.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "content": "origin.__registry__.needClose: true",
        "namespace": "sermant"
    }
    query_service_registry_configs_param = {
        "groupRule": "",
        "keyRule": "sermant.agent.registry",
        "namespace": "sermant",
        "pluginType": "service-registry"
    }
    query_service_registry_config_param = {
        "key": "sermant.agent.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "namespace": "sermant"
    }
    update_service_registry_config_param = {
        "key": "sermant.agent.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "content": "origin.__registry__.needClose: false",
        "namespace": "sermant"
    }
    delete_service_registry_config_param = {
        "key": "sermant.agent.registry",
        "group": "app=default&environment=default&service=spring-provider",
        "namespace": "sermant"
    }

    add_flowcontrol_config_param = {
        "key": "servicecomb.matchGroup.rateLimitingScene",
        "group": "service=flowcontrol",
        "content": "matches:          \n  - apiPath:          \n      exact: /rateLimiting",
        "namespace": "sermant"
    }
    query_flowcontrol_configs_param = {
        "groupRule": "",
        "keyRule": "",
        "namespace": "sermant",
        "serviceName":"flowcontrol",
        "pluginType": "flowcontrol"
    }
    query_flowcontrol_config_param = {
        "key": "servicecomb.matchGroup.rateLimitingScene",
        "group": "service=flowcontrol",
        "namespace": "sermant"
    }
    update_flowcontrol_config_param = {
        "key": "servicecomb.matchGroup.rateLimitingScene",
        "group": "service=flowcontrol",
        "content": "matches:          \n  - apiPath:          \n      exact: /circuitBreakerScene",
        "namespace": "sermant"
    }
    delete_flowcontrol_config_param = {
        "key": "servicecomb.matchGroup.rateLimitingScene",
        "group": "service=flowcontrol",
        "namespace": "sermant"
    }

    add_removal_config_param = {
        "key": "sermant.removal.config",
        "group": "app=default&environment=default",
        "content": "expireTime: 60000\n" +
                   "exceptions:\n" +
                   "  - com.alibaba.dubbo.remoting.TimeoutException\n" +
                   "  - org.apache.dubbo.remoting.TimeoutException\n" +
                   "  - java.util.concurrent.TimeoutException\n" +
                   "  - java.net.SocketTimeoutException\n" +
                   "enableRemoval: false\n" +
                   "recoveryTime: 30000\n" +
                   "rules:\n" +
                   "  - { key: default-rule, scaleUpLimit: 0.6, minInstanceNum: 1, errorRate: 0.6 }",
        "namespace": "sermant"
    }
    query_removal_configs_param = {
        "groupRule": "",
        "keyRule": "sermant.removal.config",
        "namespace": "sermant",
        "pluginType": "removal",
        "exactMatchFlag": "false"
    }
    query_removal_config_param = {
        "key": "sermant.removal.config",
        "group": "app=default&environment=default",
        "namespace": "sermant"
    }
    update_removal_config_param = {
        "key": "sermant.removal.config",
        "group": "app=default&environment=default",
        "content": "expireTime: 60000\n" +
                   "exceptions:\n" +
                   "  - com.alibaba.dubbo.remoting.TimeoutException\n" +
                   "  - org.apache.dubbo.remoting.TimeoutException\n" +
                   "  - java.util.concurrent.TimeoutException\n" +
                   "  - java.net.SocketTimeoutException\n" +
                   "enableRemoval: false\n" +
                   "recoveryTime: 30000\n" +
                   "rules:\n" +
                   "  - { key: default-rule, scaleUpLimit: 0.7, minInstanceNum: 2, errorRate: 0.7 }",
        "namespace": "sermant"
    }
    delete_removal_config_param = {
        "key": "sermant.removal.config",
        "group": "app=default&environment=default",
        "namespace": "sermant"
    }

    add_loadbalancer_config_param = {
        "key": "servicecomb.matchGroup.testLb",
        "group": "app=default&environment=&service=zk-rest-consumer",
        "content": "alias: loadbalancer-rule\nmatches:\n  - serviceName: zk-rest-provider",
        "namespace": "sermant"
    }
    query_loadbalancer_configs_param = {
        "groupRule": "",
        "keyRule": "",
        "namespace": "sermant",
        "pluginType": "loadbalancer"
    }
    query_loadbalancer_config_param = {
        "key": "servicecomb.matchGroup.testLb",
        "group": "app=default&environment=&service=zk-rest-consumer",
        "namespace": "sermant"
    }
    update_loadbalancer_config_param = {
        "key": "servicecomb.matchGroup.testLb",
        "group": "app=default&environment=&service=zk-rest-consumer",
        "content": "alias: loadbalancer-rule\nmatches:\n  - serviceName: zk-rest-consumer",
        "namespace": "sermant"
    }
    delete_loadbalancer_config_param = {
        "key": "servicecomb.matchGroup.testLb",
        "group": "app=default&environment=&service=zk-rest-consumer",
        "namespace": "sermant"
    }

    add_tag_transmission_config_param = {
        "key": "tag-config",
        "group": "sermant/tag-transmission-plugin",
        "content": "enabled: true\nmatchRule:\n  exact: [ \"id\", \"name\" ]\n  prefix: [ \"x-sermant-\" ]\n  suffix: [ \"-sermant\" ]",
        "namespace": "sermant"
    }
    query_tag_transmission_configs_param = {
        "groupRule": "sermant/tag-transmission-plugin",
        "keyRule": "tag-config",
        "namespace": "sermant",
        "pluginType": "tag-transmission"
    }
    query_tag_transmission_config_param = {
        "key": "tag-config",
        "group": "sermant/tag-transmission-plugin",
        "namespace": "sermant"
    }
    update_tag_transmission_config_param = {
        "key": "tag-config",
        "group": "sermant/tag-transmission-plugin",
        "content": "enabled: false\nmatchRule:\n  exact: [ \"id\", \"name\" ]\n  prefix: [ \"x-sermant-1\" ]\n  suffix: [ \"-sermant\" ]",
        "namespace": "sermant"
    }
    delete_tag_transmission_config_param = {
        "key": "tag-config",
        "group": "sermant/tag-transmission-plugin",
        "namespace": "sermant"
    }

    add_mq_consume_prohibition_config_param = {
        "key": "sermant.mq.consume.globalConfig",
        "group": "app=default&environment=&zone=default",
        "content": "enableKafkaProhibition: true\nkafkaTopics:\n  - demo-test-topic",
        "namespace": "sermant"
    }
    query_mq_consume_prohibition_configs_param = {
        "groupRule": "",
        "keyRule": "",
        "namespace": "sermant",
        "pluginType": "mq-consume-prohibition"
    }
    query_mq_consume_prohibition_config_param = {
        "key": "sermant.mq.consume.globalConfig",
        "group": "app=default&environment=&zone=default",
        "namespace": "sermant"
    }
    update_mq_consume_prohibition_config_param = {
        "key": "sermant.mq.consume.globalConfig",
        "group": "app=default&environment=&zone=default",
        "content": "enableKafkaProhibition: false\nkafkaTopics:\n  - demo-test-topic1",
        "namespace": "sermant"
    }
    delete_mq_consume_prohibition_config_param = {
        "key": "sermant.mq.consume.globalConfig",
        "group": "app=default&environment=&zone=default",
        "namespace": "sermant"
    }

    add_database_write_prohibition_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "content": "enableMongoDbWriteProhibition: true\nmongoDbDatabases:\n  - mongodb-database",
        "namespace": "sermant"
    }

    query_database_write_prohibition_configs_param = {
        "groupRule": "",
        "keyRule": "",
        "namespace": "sermant",
        "pluginType": "database-write-prohibition"
    }
    query_database_write_prohibition_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "namespace": "sermant"
    }
    update_database_write_prohibition_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "content": "enableMongoDbWriteProhibition: true\nmongoDbDatabases:\n  - mongodb-database-1",
        "namespace": "sermant"
    }
    delete_database_write_prohibition_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "namespace": "sermant"
    }

    add_other_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "content": "enableMongoDbWriteProhibition: true\nmongoDbDatabases:\n  - mongodb-database",
        "namespace": "sermant"
    }

    query_other_configs_param = {
        "groupRule": "app=default&environment=&zone=default",
        "namespace": "sermant",
        "pluginType": "common",
        "exactMatchFlag": "true"

    }
    query_other_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "namespace": "sermant"
    }
    update_other_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "content": "enableMongoDbWriteProhibition: true\nmongoDbDatabases:\n  - mongodb-database-1",
        "namespace": "sermant"
    }
    delete_other_config_param = {
        "key": "sermant.database.write.globalConfig",
        "group": "app=default&environment=&zone=default",
        "namespace": "sermant"
    }

    def get_configs(self, param=None, url=None):
        session = requests.session()
        path = url + self.request_url_query_configs
        resp = session.get(path, params=param).json()
        session.close()
        return resp

    def get_config(self, param=None, url=None):
        session = requests.session()
        path = url + self.request_url_common
        resp = session.get(path, params=param).json()
        session.close()
        return resp

    def add_config(self, param=None, url=None):
        session = requests.session()
        path = url + self.request_url_common
        resp = session.post(path, json=param, headers=self.header).json()
        session.close()
        time.sleep(1)
        return resp

    def update_config(self, param=None, url=None):
        session = requests.session()
        path = url + self.request_url_common
        resp = session.put(path, json=param, headers=self.header).json()
        session.close()
        time.sleep(1)
        return resp

    def delete_config(self, param=None, url=None):
        session = requests.session()
        path = url + self.request_url_common
        resp = session.delete(path, params=param, headers=self.header).json()
        session.close()
        time.sleep(1)
        return resp

    def config_test_function(self, addConfigParam=None, queryConfigsParam=None, queryConfigParam=None,
                             updateConfigParam=None, deleteConfigParam=None):
        self.assertTrue(self.add_config(addConfigParam, self.backend_server_address_use_zookeeper).get("code") == "00")
        self.assertTrue(self.add_config(addConfigParam, self.backend_server_address_use_kie).get("code") == "00")
        self.assertTrue(self.add_config(addConfigParam, self.backend_server_address_use_nacos).get("code") == "00")
        self.assertTrue(self.add_config(addConfigParam, self.backend_server_address_use_zookeeper).get("code") == "03")
        self.assertTrue(self.add_config(addConfigParam, self.backend_server_address_use_kie).get("code") == "03")
        self.assertTrue(self.add_config(addConfigParam, self.backend_server_address_use_nacos).get("code") == "03")
        self.assertTrue(len(self.get_configs(queryConfigsParam, self.backend_server_address_use_zookeeper).get("data")) == 1)
        self.assertTrue(len(self.get_configs(queryConfigsParam, self.backend_server_address_use_kie).get("data")) == 1)
        self.assertTrue(len(self.get_configs(queryConfigsParam, self.backend_server_address_use_nacos).get("data")) == 1)
        groupRule = queryConfigsParam.get("groupRule")
        queryConfigsParam["groupRule"] = "bpp="
        self.assertTrue(len(self.get_configs(queryConfigsParam, self.backend_server_address_use_zookeeper).get("data")) == 0)
        self.assertTrue(len(self.get_configs(queryConfigsParam, self.backend_server_address_use_kie).get("data")) == 0)
        self.assertTrue(len(self.get_configs(queryConfigsParam, self.backend_server_address_use_nacos).get("data")) == 0)
        queryConfigsParam["groupRule"] = groupRule
        self.assertTrue(self.get_config(queryConfigParam, self.backend_server_address_use_zookeeper).get("data").get(
            "content") == addConfigParam.get("content"))
        self.assertTrue(self.get_config(queryConfigParam, self.backend_server_address_use_kie).get("data").get(
            "content") == addConfigParam.get("content"))
        self.assertTrue(self.get_config(queryConfigParam, self.backend_server_address_use_nacos).get("data").get(
            "content") == addConfigParam.get("content"))
        self.assertTrue(
            self.update_config(updateConfigParam, self.backend_server_address_use_zookeeper).get("code") == "00")
        self.assertTrue(self.update_config(updateConfigParam, self.backend_server_address_use_kie).get("code") == "00")
        self.assertTrue(
            self.update_config(updateConfigParam, self.backend_server_address_use_nacos).get("code") == "00")
        self.assertTrue(self.get_config(queryConfigParam, self.backend_server_address_use_zookeeper).get("data").get(
            "content") == updateConfigParam.get("content"))
        self.assertTrue(self.get_config(queryConfigParam, self.backend_server_address_use_kie).get("data").get(
            "content") == updateConfigParam.get("content"))
        self.assertTrue(self.get_config(queryConfigParam, self.backend_server_address_use_nacos).get("data").get(
            "content") == updateConfigParam.get("content"))
        self.assertTrue(
            self.delete_config(deleteConfigParam, self.backend_server_address_use_zookeeper).get("code") == "00")
        self.assertTrue(self.delete_config(deleteConfigParam, self.backend_server_address_use_kie).get("code") == "00")
        self.assertTrue(
            self.delete_config(deleteConfigParam, self.backend_server_address_use_nacos).get("code") == "00")
        self.assertTrue(
            self.get_configs(queryConfigsParam, self.backend_server_address_use_zookeeper).get("data") == [])
        self.assertTrue(self.get_configs(queryConfigsParam, self.backend_server_address_use_kie).get("data") == [])
        self.assertTrue(self.get_configs(queryConfigsParam, self.backend_server_address_use_nacos).get("data") == [])
        self.assertTrue(
            self.delete_config(deleteConfigParam, self.backend_server_address_use_zookeeper).get("code") == "07")
        self.assertTrue(self.delete_config(deleteConfigParam, self.backend_server_address_use_kie).get("code") == "07")
        self.assertTrue(
            self.delete_config(deleteConfigParam, self.backend_server_address_use_nacos).get("code") == "07")

    def test_all_plugin_config(self):
        self.config_test_function(self.add_router_config_param, self.query_router_configs_param,
                                  self.query_router_config_param, self.update_router_config_param,
                                  self.delete_router_config_param)
        self.config_test_function(self.add_springboot_config_param, self.query_springboot_configs_param,
                                  self.query_springboot_config_param, self.update_springboot_config_param,
                                  self.delete_springboot_config_param)
        self.config_test_function(self.add_service_registry_config_param, self.query_service_registry_configs_param,
                                  self.query_service_registry_config_param, self.update_service_registry_config_param,
                                  self.delete_service_registry_config_param)
        self.config_test_function(self.add_flowcontrol_config_param, self.query_flowcontrol_configs_param,
                                  self.query_flowcontrol_config_param, self.update_flowcontrol_config_param,
                                  self.delete_flowcontrol_config_param)
        self.config_test_function(self.add_removal_config_param, self.query_removal_configs_param,
                                  self.query_removal_config_param, self.update_removal_config_param,
                                  self.delete_removal_config_param)
        self.config_test_function(self.add_loadbalancer_config_param, self.query_loadbalancer_configs_param,
                                 self.query_loadbalancer_config_param, self.update_loadbalancer_config_param,
                                 self.delete_loadbalancer_config_param)
        self.config_test_function(self.add_tag_transmission_config_param, self.query_tag_transmission_configs_param,
                                  self.query_tag_transmission_config_param, self.update_tag_transmission_config_param,
                                  self.delete_tag_transmission_config_param)
        self.config_test_function(self.add_mq_consume_prohibition_config_param, self.query_mq_consume_prohibition_configs_param,
                                  self.query_mq_consume_prohibition_config_param, self.update_mq_consume_prohibition_config_param,
                                  self.delete_mq_consume_prohibition_config_param)
        self.config_test_function(self.add_database_write_prohibition_config_param, self.query_database_write_prohibition_configs_param,
                                  self.query_database_write_prohibition_config_param, self.update_database_write_prohibition_config_param,
                                  self.delete_database_write_prohibition_config_param)
        self.config_test_function(self.add_other_config_param, self.query_other_configs_param,
                                  self.query_other_config_param, self.update_other_config_param,
                                  self.delete_other_config_param)
        


if __name__ == "__main__":
    unittest.main()
