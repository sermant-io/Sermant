# hercules
一个中间适配层的springboot项目
## 快速开始
  **前提：启动完Hercules-ngrinder中hercules-controller模块**  
  1.直接运行mvn clean package打包成jar包；  
  2.运行：
```
  nohup \
  java -jar hercules-0.0.1.jar \
  --controller.engine.url=http://xxxx:8080/hercules-controller-0.0.1 \
  --monitor.agent.url=http://localhost:9095/agent \
  > log.out 2>&1 &  
```
  **备注:controller.engine.url值为项目Hercules-ngrinder中hercules-controller模块部署到Tomcat的访问地址**
  **备注:monitor.agent.url值为项目javamesh-server-monitor模块部署的访问地址**

  3.启动后访问地址：http://xxxx:9091/argus

