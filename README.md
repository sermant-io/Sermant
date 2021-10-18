# JavaMesh

## 概述

JavaMesh是一个基于ByteBuddy字节码技术开发的javaagent框架;框架当前提供了流量控制，流量录制插件;基于JavaMesh,只需实现少量的接口即可快速开发自己需要实现的agent功能;框架提供了基于netty的统一消息发送模块;只需部署netty-server服务，即可实现心跳或数据的传输,同时支持自定义消息类型。

## 模块说明

javamesh-agentcore: 核心功能
javamesh-agentcore/javamesh-agentcore-bootstrap: 公共模块
javamesh-agentcore/javamesh-agentcore-core: 核心模块
javamesh-agentcore/javamesh-agentcore-core-ext: 消息发送模块客户端，将合入核心模块
javamesh-agentcore/javamesh-agentcore-premain: 启动入口模块
javamesh-backend: 消息发送模块服务端
javamesh-package: 打包模块  
javamesh-prepare: 初始化模块，现用作清理临时文件
javamesh-samples: 样品模块，内含插件及其相应的后端
javamesh-samples/javamesh-example: 插件示例
javamesh-samples/javamesh-flowcontrol: 流控插件及后端

## [示例插件](javamesh-samples/javamesh-examples/demo-plugin)

- 示例插件中拦截了org.springframework.boot.autoconfigure.SpringBootApplication注解

## [示例插件拦截的应用](javamesh-samples/javamesh-examples/demo-application)

## 快速开始

### 环境安装

- [jdk](https://www.oracle.com/java/technologies/downloads/)
- [maven](https://maven.apache.org/download.cgi)
- [idea](https://www.jetbrains.com/idea/)

### 编译出包

- 下载`JavaMesh`源码,用`idea`打开
- 在`File | Settings | Build, Execution, Deployment | Build Tools | Maven`中配置`maven`信息
- 在`idea`中执行`mvn clean package`
- 编译结果文件:`JavaMesh\javamesh-agent-x.x.x.tar`

### 运行

#### 终端

- 打包[示例插件拦截的应用](javamesh-samples/javamesh-examples/demo-application)
-

执行`java -javaagent:${JavaMesh}\javamesh-agent-x.x.x\agent\javamesh-agent.jar=appName=${appName} -jar .\DemoApplication-0.0.1 -SNAPSHOT.jar`
,`${JavaMesh}`是框架项目路径,`${appName}`为应用名称

#### IDEA

- IDEA挂载JavaMesh,需在应用`Run Configuration -> VM options`
  加入`-javaagent:${JavaMesh}\javamesh-agent-x.x.x\agent\javamesh-agent.jar=appName=${appName}`
  即可,其中`${JavaMesh}`是框架项目路径,`${appName}`为应用名称。
- 运行[应用](javamesh-samples/javamesh-examples/demo-application/src/main/java/com/lubanops/demo/DemoApplication.java)

## 插件开发
框架采用SPI机制进行插件的加载，插件的开发需要在resources/META-INF/service创建相应的文件(文件名与实现接口的全限定名一致)
### [增强类接口](javamesh-agentcore-bootstrap/src/main/java/com/huawei/apm/bootstrap/definition/EnhanceDefinition.java)
该接口定义了两个方法：`ClassMatcher enhanceClass()`和`MethodInterceptPoint[] getMethodInterceptPoints()`：  
`ClassMatcher enhanceClass()`用来获取需要增强的目标类，支持单个和多个类，注解，也可以通过前缀匹配需要增强的类；  
`MethodInterceptPoint[] getMethodInterceptPoints()`用来获取封装了待增强目标方法和其拦截器的MethodInterceptPoint(对应的拦截器接口说明在下面详细说明)，支持返回多个不同类型的拦截器。
- [spi文件示例](javamesh-samples/javamesh-examples/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.definition.EnhanceDefinition)  
  文件名为接口类文件的全限定名；  
  文件内容为实现了该接口的类的全限定名；    
  文件位置按照spi的机制应放到模块`resources/META-INF/services`。
- [实现示例](javamesh-samples/javamesh-examples/demo-plugin/src/main/java/com/lubanops/apm/demo/BootInstrumentation.java)
  ```java
  public class BootInstrumentation implements EnhanceDefinition {
    public static final String ENHANCE_ANNOTATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private static final String INTERCEPT_CLASS = "com.lubanops.apm.demo.BootInterceptor";
  
    @Override
    public ClassMatcher enhanceClass() {
      return ClassMatchers.annotationWith(ENHANCE_ANNOTATION);
    }
  
    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
      return new MethodInterceptPoint[]{MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS,
              ElementMatchers.named("main"))
      };
    }
  }
  ```
  在示例代码中增强了`org.springframework.boot.autoconfigure.SpringBootApplication`类，拦截器的类为`com.lubanops.apm.demo.BootInterceptor`，实现了静态方法拦截接口(这部分在下面详细说明)，拦截的方法为`main`方法。
### [拦截器接口](javamesh-agentcore-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/Interceptor.java)
该部分接口的实现不需要通过spi机制加载；  
拦截器接口的实现类用在增强类接口的`getMethodInterceptPoints()`方法中；
根据方法的不同扩展出了三种拦截器接口，分别是静态方法拦截器`StaticMethodInterceptor`，实例方法拦截器`InstanceMethodInterceptor`,构造方法拦截器`ConstructorInterceptor`。
- [静态拦截器](javamesh-agentcore-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/StaticMethodInterceptor.java)  
  该拦截器接口中有三个方法：`before`, `after`, `onThrow`。  
  `before`在拦截方法执行前前运行；`after`在拦截方法执行结束后运行；`onThrow`用于异常处理。
  ```java
  public class BootInterceptor implements StaticMethodInterceptor {
      @Override
      public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) throws Exception {
          System.out.println("[BootInterceptor]-before");
      }
  
      @Override
      public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) throws Exception {
          System.out.println("[BootInterceptor]-after");
          return result;
      }
  
      @Override
      public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {
  
      }
  }
  ```
- [示例拦截器](javamesh-agentcore-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/InstanceMethodInterceptor.java)  
  该拦截器接口中有三个方法：`before`, `after`, `onThrow`。  
  `before`在拦截方法执行前前运行；`after`在拦截方法执行结束后运行；`onThrow`为异常处理。
- [构造拦截器](javamesh-agentcore-bootstrap/src/main/java/com/huawei/apm/bootstrap/interceptors/ConstructorInterceptor.java)
- 该拦截器接口中有一个方法：`onConstruct`。
### [插件配置接口](javamesh-agentcore-bootstrap/src/main/java/com/huawei/apm/bootstrap/config/BaseConfig.java)
插件配置接口实现类中写入插件运行过程中需要的配置信息。
- [spi文件示例](javamesh-samples/javamesh-examples/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.config.BaseConfig)  
  文件名为接口类文件的全限定名；  
  文件内容为实现了该接口的类的全限定名；    
  文件位置按照spi的机制应放到模块`resources/META-INF/services`。
```java
public class DemoConfig implements BaseConfig {

    private String pluginName = "demo";
}
```
### [插件初始化接口](javamesh-agentcore-bootstrap/src/main/java/com/huawei/apm/bootstrap/boot/PluginService.java)
- [spi文件示例](javamesh-samples/javamesh-examples/demo-plugin/src/main/resources/META-INF/services/com.huawei.apm.bootstrap.boot.PluginService)  
  文件名为接口类文件的全限定名；  
  文件内容为实现了该接口的类的全限定名；    
  文件位置按照spi的机制应放到模块`resources/META-INF/services`。  

插件初始化接口用户初始化插件，比如插件的心跳等定时任务的启动。  
该接口有两个方法：`init()`用于启动插件初始化，`stop()`用于停止插件。
下面给出插件通过扩展框架线条功能的初始化示例：
```java
public class FlowrecordService implements PluginService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(
            new FlowrecordThreadFactory("FLOW_RECORD_INIT_THREAD"));

    @Override
    public void init() {
        executorService.execute(new FlowRecordInitTask());
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    static class FlowRecordInitTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // 开启定时任务（发送心跳）
                    HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
                    String msg = heartbeatMessage.registerInformation("name", "flowrecord").generateCurrentMessage();
                    if (msg != null && !"".equals(msg)) {
                        LogFactory.getLogger().log(Level.INFO, "[KafkaHeartbeatSender] heartbeat message=" + msg);
                        NettyClientFactory factory = NettyClientFactory.getInstance();
                        NettyClient nettyClient = factory.getNettyClient(
                                AgentConfigManager.getNettyServerIp(),
                                Integer.parseInt(AgentConfigManager.getNettyServerPort()));
                        nettyClient.sendData(msg.getBytes(StandardCharsets.UTF_8), Message.ServiceData.DataType.SERVICE_HEARTBEAT);
                        Thread.sleep(5000);
                    } else {
                        LogFactory.getLogger().log(Level.SEVERE, "[KafkaHeartbeatSender] heartbeat json conversion error ");
                    }

                } catch (Exception e) {
                    LogFactory.getLogger().warning(String.format("Init Flow record plugin failed, {%s}", e));
                }
            }
        }
    }
}
```