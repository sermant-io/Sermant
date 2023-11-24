#
# Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

import json
import unittest
import time

import requests


class TestBackend(unittest.TestCase):
    request_url_query_event = "http://127.0.0.1:8900/sermant/event/events"
    request_url_query_page = "http://127.0.0.1:8900/sermant/event/events/page?page="
    request_url_query_webhook = "http://127.0.0.1:8900/sermant/event/webhooks"
    request_url_set_webhook = "http://127.0.0.1:8900/sermant/event/webhooks/"
    sermant_start = "SERMANT_START"
    sermant_stop = "SERMANT_STOP"
    sermant_service_stop = "SERMANT_SERVICE_STOP"
    sermant_service_start = "SERMANT_SERVICE_START"
    sermant_transform_success = "SERMANT_TRANSFORM_SUCCESS"
    sermant_transform_failure = "SERMANT_TRANSFORM_FAILURE"
    log_warn = "WARNING"
    log_error = "SEVERE"
    level_normal = "normal"
    level_important = "important"

    def get_all_event(self, param=None):
        """
        获取满足条件的所有事件
        :param param: 查询条件
        :return: 事件
        """
        query_event_param = {
                "service": [],
                "ip": [],
                "scope": "",
                "type": [],
                "level": [],
                "startTime": 0,
                "endTime": 0
            }
        if param is None:
            param = {}
        result = []
        session = requests.session()
        now_time = int(round(time.time() * 1000))
        query_event_param.update({
            "startTime": 0,
            "endTime": now_time
        })
        query_event_param.update(param)
        resp = session.get(self.request_url_query_event, params=query_event_param).json()
        total_page = resp.get("totalPage")
        result.extend(resp.get("events"))
        for page in range(2, total_page + 1):
            result.extend(session.get(self.request_url_query_page + str(page)).json().get("events"))
        session.close()
        return result

    def event_field_verify(self, verify_name):
        """
        验证是否包含某种事件
        :param verify_name: 事件名
        :return: 验证结果
        """
        result = False
        events = self.get_all_event()
        if any(verify_name in json.dumps(event) for event in events):
            result = True
        return result

    def test_sermant_start(self):
        """
        验证sermant启动事件
        :return: None
        """
        self.assertTrue(self.event_field_verify(self.sermant_start))

    def test_sermant_stop(self):
        """
        验证sermant停止事件
        :return: None
        """
        self.assertTrue(self.event_field_verify(self.sermant_stop))

    def test_sermant_service_start(self):
        """
        验证核心服务启动事件
        :return: None
        """
        self.assertTrue(self.event_field_verify(self.sermant_service_start))

    def test_sermant_service_stop(self):
        """
        验证核心服务停止事件
        :return: None
        """
        self.assertTrue(self.event_field_verify(self.sermant_service_stop))

    def test_sermant_transform_success(self):
        """
        验证增强成功事件
        :return: None
        """
        self.assertTrue(self.event_field_verify(self.sermant_transform_success))

    def test_backend_query_by_time(self):
        """
        验证查询顺序
        :return: None
        """
        result = True
        events = self.get_all_event()
        if any(events[i].get("time") < events[i + 1].get("time") for i in range(0, len(events) - 1)):
            result = False
        self.assertTrue(result)

    def test_backend_query_by_service_name(self):
        """
        验证服务名查询
        :return: None
        """
        result = False
        events = self.get_all_event(param={"service": ["default"]})
        if len(events) > 0 and all(event.get("meta").get("service") == "default" for event in events):
            result = True
        self.assertTrue(result)

    def test_backend_query_by_ip(self):
        """
        验证ip查询
        :return: None
        """
        result = False
        ip = "172.17.0.1"
        events = self.get_all_event(param={"ip": [ip]})
        if len(events) > 0 and all(event.get("meta").get("ip") == ip for event in events):
            result = True
        self.assertTrue(result)

    def test_backend_query_by_event_type(self):
        """
        验证事件类型查询
        :return: None
        """
        result = False
        events = self.get_all_event(param={"type": ["operation", "log"]})
        if len(events) > 0 and all(event.get("type") in ["operation", "log"] for event in events):
            result = True
        self.assertTrue(result)

    def test_backend_query_by_event_level(self):
        """
        验证事件级别查询
        :return: None
        """
        result = False
        events = self.get_all_event(param={"level": ["normal", "important"]})
        if len(events) > 0 and all(event.get("level") in ["normal", "important"] for event in events):
            result = True
        self.assertTrue(result)

    def test_backend_webhook_info(self):
        """
        验证webhook查询
        :return: None
        """
        resp = requests.get(self.request_url_query_webhook).json()
        self.assertEqual(resp.get("total"), 2)

    def test_backend_set_webhook(self):
        """
        验证webhook设置
        :return: None
        """
        headers = {'content-type': 'application/json'}
        self.assertTrue(
            requests.put(self.request_url_set_webhook + '0',
                         data=json.dumps({"url": "url", "enable": True}),
                         headers=headers).json())
        self.assertTrue(
            requests.put(self.request_url_set_webhook + '1',
                         data=json.dumps({"url": "url", "enable": True}),
                         headers=headers).json())
        resp = requests.get(self.request_url_query_webhook).json()
        self.assertEqual(resp.get("total"), 2)
        for webhook in resp.get("webhooks"):
            self.assertTrue(webhook.get("enable"))
            self.assertEqual(webhook.get("url"), "url")


if __name__ == "__main__":
    unittest.main()
