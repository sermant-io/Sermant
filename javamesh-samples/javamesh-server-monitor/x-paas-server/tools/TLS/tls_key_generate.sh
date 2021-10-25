#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Changes these CN's to match your hosts in your environment if needed.
SERVER_CN=localhost
CLIENT_CN=localhost # Used when doing mutual TLS

echo Generate CA key:
openssl genrsa -passout pass:1111 -des3 -out ca.key 4096
echo Generate CA certificate:
# Generates ca.crt which is the trustCertCollectionFile
openssl req -passin pass:1111 -new -x509 -days 365 -key ca.key -out ca.crt -subj "/CN=${SERVER_CN}"
echo Generate server key:
openssl genrsa -passout pass:1111 -des3 -out server.key 4096
echo Generate server signing request:
openssl req -passin pass:1111 -new -key server.key -out server.csr -subj "/CN=${SERVER_CN}"
echo Self-signed server certificate:
# Generates server.crt which is the certChainFile for the server
openssl x509 -req -passin pass:1111 -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt
echo Remove passphrase from server key:
openssl rsa -passin pass:1111 -in server.key -out server.key
echo Generate client key
openssl genrsa -passout pass:1111 -des3 -out client.key 4096
echo Generate client signing request:
openssl req -passin pass:1111 -new -key client.key -out client.csr -subj "/CN=${CLIENT_CN}"
echo Self-signed client certificate:
# Generates client.crt which is the clientCertChainFile for the client (need for mutual TLS only)
openssl x509 -passin pass:1111 -req -days 365 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt
echo Remove passphrase from client key:
openssl rsa -passin pass:1111 -in client.key -out client.key
echo Converting the private keys to X.509:
# Generates client.pem which is the clientPrivateKeyFile for the Client (needed for mutual TLS only)
openssl pkcs8 -topk8 -nocrypt -in client.key -out client.pem
# Generates server.pem which is the privateKeyFile for the Server
openssl pkcs8 -topk8 -nocrypt -in server.key -out server.pem

#huawei update
#建议在/root/zzbTestJconsole/skywalking-openssl（根据自身需要生成证书的路径配置）路径下面使用命令行执行。
echo Generate CA key:
openssl genrsa -passout pass:1111 -des3 -out /root/zzbTestJconsole/skywalking-openssl/ca.key 4096
echo Generate CA certificate:
#根据需要修改 -subj里面的内容
openssl req -new -x509 -passout pass:1111 -days 365 -key /root/zzbTestJconsole/skywalking-openssl/ca.key -subj "/C=CN/ST=CD/L=PD/O=HuaWei/CN=PerformanceManagement" -out ca.crt
echo Generate server signing request:
#根据服务器的真实ip进行替换 -subj里面的ip
openssl req -passout pass:1111 -newkey rsa:2048 -nodes -keyout server.key -subj "/C=CN/ST=CD/L=PD/O=HuaWei/CN=127.0.0.1" -out /root/zzbTestJconsole/skywalking-openssl/server.csr
echo Self-signed server certificate:
#根据真实服务器ip和dns进行替换
openssl x509 -req -extfile <(printf "subjectAltName=IP:127.0.0.1,DNS:DESKTOP-1BD7JQM") -passin pass:1111 -days 365 -in /root/zzbTestJconsole/skywalking-openssl/server.csr -CA /root/zzbTestJconsole/skywalking-openssl/ca.crt -CAkey /root/zzbTestJconsole/skywalking-openssl/ca.key -CAcreateserial -out /root/zzbTestJconsole/skywalking-openssl/server.crt
echo Converting the private keys to X.509:
openssl pkcs8 -topk8 -nocrypt -in /root/zzbTestJconsole/skywalking-openssl/server.key -out /root/zzbTestJconsole/skywalking-openssl/server.pem