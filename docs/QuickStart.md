# Quick Start Instructions for Service Registration and Discovery

[简体中文](QuickStart-zh.md) | [English](QuickStart.md) 

Based on the scenario where the host application changes the registration center, we will use the registration plugin of the Sermant framework, and fully show how to use the Sermant plugin to non-invasively extend the functionality of the host application. 

In addition, refer to [List of Plugin Functions](user-guide/feature-list.md) to get more plugin functions.

## Before Adopting Sermant
The following figure shows the example application demonstrated in this documentation is registered with Zookeeper when Sermant is not used.
<img src="binary-docs/before_use_agent.png" width="50%" syt height="50%" />

## After Adopting Sermant
This figure shows When using a Sermant, the Sermant registers the application information which originally registered with Zookeeper to ServiceCenter. Sermant adopts ZooKeeper as the default configuration center to provide dynamic configuration management capabilities to the plugins.

<img src="binary-docs/after_use_agent.png" width="50%" syt height="50%" />

# Get Compiled Products

## Download the release package 
Click [here](https://github.com/huaweicloud/Sermant/releases) to download the release package

## Compile Source Code
- Prepare [git](https://git-scm.com/downloads) , [jdk 8 or 11](https://www.oracle.com/java/technologies/downloads/) , [maven](https://maven.apache.org/download.cgi) for your machine
- execute `git clone -b develop https://github.com/huaweicloud/Sermant.git` to clone the source code
- execute `cd Sermant`to entry into Sermant directory
- execute `mvn clean package -Dmaven.test.skip -Pexample` to compile source code of Sermant

# Startup
- Runtime environment requires [ServiceCenter](https://github.com/apache/servicecomb-service-center/releases), and [zookeeper](https://zookeeper.apache.org/releases.html) as the registration center and configuration center respectively.

- entry into the root directory of the compiled executable file(e.g. **sermant-agent-1.0.0**).

- To start Backend, run the following command. For a detailed description of the backend module and more configuration modifications, please refer to [Backend Module](user-guide/backend.md).
  ```bash
  # windows
  java -jar server\sermant\sermant-backend-x.x.x.jar
  
  # mac, linux
  java -jar server/sermant/sermant-backend-x.x.x.jar
  ```
  
- Execute the following command to launch the sample provider application that mounts the register plugin.
  ```bash
  # windows
  java -javaagent:agent\sermant-agent.jar=appName=provider -jar ..\sermant-example\demo-register\resttemplate-provider\target\resttemplate-provider.jar
  # mac linux
  java -javaagent:agent/sermant-agent.jar=appName=provider -jar ../sermant-example/demo-register/resttemplate-provider/target/resttemplate-provider.jar
  ```
  
- Execute the following command to launch the sample consumer application that mounts the register plugin.
  ```bash
  # windows
  java -javaagent:agent\sermant-agent.jar=appName=consumer -jar ..\sermant-example\demo-register\resttemplate-consumer\target\resttemplate-consumer.jar
  # mac linux
  java -javaagent:agent/sermant-agent.jar=appName=consumer -jar ../sermant-example/demo-register/resttemplate-consumer/target/resttemplate-consumer.jar
  ```
  
- Open a browser to access: http://localhost:8900 to check the running state of Sermant and the enabled plugin.
  ![pic](binary-docs/backend_sermant_info.png)

- Open a browser to access:[http://localhost:30103](http://localhost:30103) to check the application registration status.
  ![pic](binary-docs/register-application.PNG)

- Open a browser to access:[http://localhost:8005/hello](http://localhost:8005/hello) to check if the provider and consumer registered and subscribed successfully 

  <img src="binary-docs/check_application.png" width="50%" syt height="50%" />

## Check Logs
The log directories for Sermant and Backend are located in `./logs/sermant/core ` and `./logs/sermant/backend` respectively in the environment of host application.

## Related Documents 

For plugin development, please refer to [Introduction to Sermant Development and Usage](./README.md).

Frequently asked questions and solution guidance, please refer to [FAQ](./FAQ.md).