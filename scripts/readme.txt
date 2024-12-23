/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

This readme document is used to describe the functions of each script:

1. AesUtil.class : generate encryption keys and password ciphertexts

Run the script using the following command and enter your password as prompted:

**********************************************************************
java AesUtil
please input your password
123456
encryption key is T4bUktLn5P01Qs6unSuG5ZZElN05WUDAXOjaJgMB5eM=
encrypted password is u/K+lx/m9w1EpEjkM9R48s8PiVDHEpCUGz+1jWOasyzRrQ==
**********************************************************************

For detailed usage, please refer to the official documentation at https://sermant.io/en/document/faq/encryption.html.

2. AgentLoader.class : sermant agent hot plugging script of java

Run the script using the following command and entering as prompted:

**********************************************************************
# Linux、MacOS
java -cp ./:$JAVA_HOME/lib/tools.jar AgentLoader

# Windows
java -cp "%JAVA_HOME%\lib\tools.jar" AgentLoader
**********************************************************************

For detailed usage, please refer to the official documentation at https://sermant.io/en/document/user-guide/sermant-agent.html.

3. attach_sermant_agent : sermant agent hot plugging script of c language

Run the script using the following command and entering as prompted:

**********************************************************************
./attach_sermant_agent -path={sermant-path}/sermant-agent.jar -pid={pid} -command={COMMAND}
**********************************************************************

For detailed usage, please refer to the official documentation at https://sermant.io/en/document/user-guide/sermant-agent.html#one-click-agent-and-plugin-attachment.
