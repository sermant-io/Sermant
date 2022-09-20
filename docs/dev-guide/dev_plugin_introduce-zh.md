# Sermant 插件开发说明

[简体中文](dev_plugin_introduce-zh.md) | [English](dev_plugin_introduce.md)

## 1. 环境准备

- IDEA
- Maven3.*
- Java idk 8/11

## 2. 通过模版生成插件项目

- IDEA 新建项目  
  ![idea_new_project.png](../binary-docs/idea_new_project.png)

- 增加 Archetype  
  ![add_archetype.png](../binary-docs/add_archetype.png)
  ![archetype_info.png](../binary-docs/archetype_info.png)
```properties
GroupId: com.huaweicloud.sermant.examples
ArtifactId: sermant-examples-archetype
Version: 0.0.1
```
- 创建项目  
  ![create_project_by_template.png](../binary-docs/create_project_by_template.png)

- 生成插件项目  
  ![create_project_by_template_result.png](../binary-docs/create_project_by_template_result.png)

## 3. 新建插件项目

本部分仅说明插件去源码开发文件配置情况，具体插件开发参考[插件模块开发手册](dev_plugin_module.md)和[插件代码开发手册](dev_plugin_code.md)。

### 3.1 IDEA创建maven工程(本文档创建的工程名为**sermant-test**)

- 创建插件模块(本文档创建插件模块名为**template**)
- 在插件模块中创建plugin子模块(本文档创建的子模块名为**template-plugin**)
- 在插件模块中创建service子模块(本文档创建的子模块名为**template-service**)
- 在主项目**sermant-test**下创建config目录
- 在插件**template**模块下创建config目录

### 3.2 在主项目**sermant-test**下config目录增加以下文件：

- bootstrap.properties(启动配置文件，该配置为Sermant实例指定启动信息)

```properties
appName=default
appType=0
instanceName=default
```

- config.properties(核心配置文件，该配置为Sermant启动的核心配置集，指定了Sermant的一些运行机制和服务的运行逻辑)

```properties
# agent config
agent.config.isEnhanceBootStrapEnable=false
agent.config.ignoredPrefixes=com.huawei.sermant,com.huaweicloud.sermant
agent.config.combineStrategy=ALL
agent.config.serviceBlackList=com.huaweicloud.sermant.core.service.heartbeat.HeartbeatServiceImpl,com.huaweicloud.sermant.core.service.send.NettyGatewayClient,com.huaweicloud.sermant.core.service.tracing.TracingServiceImpl
# adaptor config
adaptor.config.isLoadExtAgentEnable=false
# dynamic config
dynamic.config.timeoutValue=30000
dynamic.config.defaultGroup=sermant
dynamic.config.serverAddress=127.0.0.1:2181
dynamic.config.dynamicConfigType=ZOOKEEPER
# heartbeat config
heartbeat.interval=3000
#backend config
backend.nettyIp=127.0.0.1
backend.nettyPort=6888
backend.httpIp=127.0.0.1
backend.httpPort=8900
# service meta config
service.meta.application=default
service.meta.version=1.0.0
service.meta.project=default
service.meta.environment=
#monitor config
monitor.service.address=127.0.0.1
monitor.service.port=12345
monitor.service.isStartMonitor=false
```

- plugins.yaml(插件配置文件，该配置指定Sermant启动时加载的插件和适配器，名称为开发插件时在pom中配置的<package.plugin.name>)

```yaml
plugins:
  - pluginName
```

- logback.xml(日志配置文件，该配置为日志配置，指定了日志的输出规则，在sermant-agentcore-core中对日志依赖进行了shade，所以此处的Appender不可做修改，需按照如下配置进行操作)

```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration scan="true">

    <!-- 定义日志文件 输出位置 -->
    <property name="log.home_dir" value="${sermant_log_dir:-./logs/sermant/core}"/>
    <property name="log.app_name" value="sermant"/>
    <!-- 日志最大的历史 30天 -->
    <property name="log.maxHistory" value="${sermant_log_max_history:-30}"/>
    <property name="log.level" value="${sermant_log_level:-info}"/>
    <property name="log.maxSize" value="${sermant_log_max_size:-5MB}"/>

    <!-- 设置日志输出格式 -->
    <!-- %d{yyyy-MM-dd HH:mm:ss.SSS}日期-->
    <!-- %C类的完整名称-->
    <!-- %F文件名-->
    <!-- %M为method-->
    <!-- %L为行号-->
    <!-- %thread线程名称-->
    <!-- %m或者%msg为信息-->
    <!-- %n换行-->
    <property name="log.pattern" value="%d{yyyy-MM-dd HH:mm:ss.SSS} %le %F %C %M %L [%thread] %m%n"/>

    <!-- ConsoleAppender 控制台输出日志 -->
    <appender name="CONSOLE" class="com.huaweicloud.sermant.dependencies.ch.qos.logback.core.ConsoleAppender">
        <filter class="com.huaweicloud.sermant.dependencies.ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <encoder>
            <pattern>
                ${log.pattern}
            </pattern>
        </encoder>
    </appender>

    <!--设置一个向上传递的appender,所有级别的日志都会输出-->
    <appender name="app" class="com.huaweicloud.sermant.dependencies.ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy
                class="com.huaweicloud.sermant.dependencies.ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${log.home_dir}/app/%d{yyyy-MM-dd}/${log.app_name}-%i.log</fileNamePattern>
            <maxHistory>${log.maxHistory}</maxHistory>
            <MaxFileSize>${log.maxSize}</MaxFileSize>
        </rollingPolicy>
        <encoder>
            <pattern>${log.pattern}</pattern>
        </encoder>
    </appender>

    <!-- root级别   DEBUG -->
    <root>
        <!-- 打印debug级别日志及以上级别日志 -->
        <level value="${log.level}"/>
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="app"/>
    </root>

</configuration>
```

### 3.3 在插件**template**模块下config目录增加以下文件:

- config.yaml(插件配置文件，根据插件实际情况，有配置文件则需要创建，无配置则不需创建)

### 3.4 pom文件配置

#### 3.4.1 在插件模块(本文档中**template**)项目的pom文件中增加以下内容：

- `<version>`字段根据实际需要可以更改版本。
- `<package.plugin.name>`字段根据实际情况需要修改插件名。

```xml

<properties>
    <sermant.basedir>${pom.basedir}/..</sermant.basedir>
    <package.plugin.name>template</package.plugin.name>
    <shade.common.prefix>com.huaweicloud.sermant.dependencies</shade.common.prefix>
    <package.plugin.dir>${sermant.basedir}/agent/pluginPackage</package.plugin.dir>
    <package.plugin.type>undefined</package.plugin.type>
    <package.output.dir>${package.plugin.dir}/${package.plugin.name}/${package.plugin.type}</package.output.dir>
    <package.plugin.version>${project.version}</package.plugin.version>
    <config.source.dir>../config</config.source.dir>
    <config.output.dir>${package.plugin.dir}/${package.plugin.name}/config</config.output.dir>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>
</properties>
```

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-resources-plugin</artifactId>
            <version>3.1.0</version>
            <inherited>false</inherited>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>copy-resources</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>${sermant.basedir}/agent/config</outputDirectory>
                        <overwrite>true</overwrite>
                        <resources>
                            <resource>
                                <directory>${config.source.dir}</directory>
                                <includes>
                                    <include>bootstrap.properties</include>
                                    <include>config.properties</include>
                                    <include>logback.xml</include>
                                    <include>plugins.yaml</include>
                                </includes>
                            </resource>
                        </resources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-clean-plugin</artifactId>
            <version>2.5</version>
            <inherited>false</inherited>
            <executions>
                <execution>
                    <id>agent-clean</id>
                    <phase>clean</phase>
                    <goals>
                        <goal>clean</goal>
                    </goals>
                    <configuration>
                        <filesets>
                            <fileset>
                                <directory>${sermant.basedir}/agent</directory>
                            </fileset>
                        </filesets>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>3.3.0</version>
            <inherited>false</inherited>
            <executions>
                <execution>
                    <id>copy</id>
                    <phase>package</phase>
                    <goals>
                        <goal>copy</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>com.huaweicloud.sermant</groupId>
                        <artifactId>sermant-agentcore-premain</artifactId>
                        <version>0.4.0</version>
                        <type>jar</type>
                        <overWrite>false</overWrite>
                        <outputDirectory>${sermant.basedir}/agent</outputDirectory>
                        <destFileName>sermant-agent.jar</destFileName>
                    </artifactItem>
                    <artifactItem>
                        <groupId>com.huaweicloud.sermant</groupId>
                        <artifactId>sermant-agentcore-core</artifactId>
                        <version>0.4.0</version>
                        <type>jar</type>
                        <overWrite>false</overWrite>
                        <outputDirectory>${sermant.basedir}/agent/core</outputDirectory>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </plugin>
    </plugins>
    <pluginManagement>
        <!--      该配置用于将插件配置文件放入最终构建的目录      -->
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Sermant-Plugin-Name>${package.plugin.name}</Sermant-Plugin-Name>
                            <Sermant-Plugin-Version>${package.plugin.version}</Sermant-Plugin-Version>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-resources</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>${config.output.dir}</outputDirectory>
                            <overwrite>true</overwrite>
                            <resources>
                                <resource>
                                    <directory>${config.source.dir}</directory>
                                    <includes>
                                        <include>config.yaml</include>
                                    </includes>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!--        此处用于将插件包和插件服务包中涉及到必要shade依赖进行shade，并将插件相关Jar包放入最终构建目录        -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <outputFile>${package.output.dir}/${project.artifactId}-${project.version}.jar</outputFile>
                    <relocations>
                        <relocation>
                            <pattern>net.bytebuddy</pattern>
                            <shadedPattern>${shade.common.prefix}.net.bytebuddy</shadedPattern>
                        </relocation>
                        <relocation>
                            <pattern>org.slf4j</pattern>
                            <shadedPattern>${shade.common.prefix}.org.slf4j</shadedPattern>
                        </relocation>
                    </relocations>
                    <transformers>
                        <transformer
                                implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                        <transformer
                                implementation="org.apache.maven.plugins.shade.resource.ApacheNoticeResourceTransformer"/>
                    </transformers>
                </configuration>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```

#### 3.4.2 在子模块(本文档中template-plugin)项目中的pom文件中增加以下内容：

- `<package.plugin.name>`字段根据实际情况需要修改插件名。

```xml

<properties>
    <sermant.basedir>${pom.basedir}/../..</sermant.basedir>
    <package.plugin.name>template</package.plugin.name>
    <package.plugin.type>plugin</package.plugin.type>
    <config.skip.flag>false</config.skip.flag>
</properties>
```

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

#### 3.4.3 在子模块(本文档template-plugin)项目中的pom文件中增加以下内容：

- `<package.plugin.name>`字段根据实际情况需要修改插件名。

```xml

<properties>
    <sermant.basedir>${pom.basedir}/../..</sermant.basedir>
    <package.plugin.name>template</package.plugin.name>
    <package.plugin.type>service</package.plugin.type>
</properties>
```

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

#### 3.4.5 打包

- 在父工程(本文档中sermant-plugin-template)下执行```mvn clean package -DskipTests```
- 在父工程(本文档中sermant-plugin-template)出现agent目录，即为最终结果。