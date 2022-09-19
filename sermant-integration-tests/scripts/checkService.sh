#
# Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

# !/bin/bash
# 该脚本用于判断服务是否完成启动
# 服务请求地址
serviceUrl=$1
# 最大等待时间, 单位秒
maxWaitTime=$2

tryTimes=${maxWaitTime}

for ((i=1; i<=${tryTimes};i++))
do
  echo "try curl ${serviceUrl} at ${i} times"
  code=`curl -I -o /dev/null -s -w %{http_code} ${serviceUrl}`
  echo "http code is ${code}."
  if [ "${code}" == 200 ];then
    echo "Curl service url is success"
    break
  fi
  sleep 1
done