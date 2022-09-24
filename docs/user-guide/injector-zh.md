# 容器化部署指导手册

[简体中文](injector-zh.md) | [English](injector.md)

k8s环境下，Sermant支持通过sermant-injector组件实现宿主应用自动挂载sermant-agent包的快速部署方式。本文档主要介绍sermant-injector模块以及上述部署方式的具体流程。

## sermant-injector模块介绍
sermant-injector是基于Kubernetes准入控制器（Admission Controllers）特性开发而来。准入控制器位于k8s API Server中，能够拦截对API Server的请求，完成身份验证、授权、变更等操作。

serment-injector属于变更准入控制器(MutatingAdmissionWebhook), 能够在创建容器资源前对请求进行拦截和修改。serment-injector部署在k8s后，只需在宿主应用部署的YAML文件中`spec > template > metadata> labels`层级加入`sermant-injection: enabled`即可实现自动挂载sermant-agent。

### 组成部分
`sermant-injector`文件夹下

- `deployment`，部署sermant-injector应用的helm包文件
  - `release`
    - `injector_k8s_1.19+` helm包，支持k8s 1.19版本及以上环境部署
    - `injector_k8s_1.21-` helm包，支持k8s 1.21版本及以下环境部署
- `images`
  - `injector` sermant-injector Dockerfile及镜像构建脚本
  - `sermant-agent` sermant-agent Dockerfile及镜像构建脚本
- `scripts` 包含证书生成脚本等
- `src` 项目代码

## 容器化部署流程

### 运行环境
[Kubernetes 1.19+](https://kubernetes.io/)

[Helm v3](https://helm.sh/)

### 生成证书
Kubernetes webhook只能通过HTTPS(SSL/TLS)调用，因此需要为sermant-injector生成ssl key和证书。

按照当前环境使用的k8s的版本，在k8s任一节点上任一目录下执行`scripts`下的`certificate.sh`脚本。(k8s 1.19-1.21版本两者皆可)

> 注意：脚本中`NAMESPACE`参数必须和`deployment/release`下的`values.yaml`中的`namespace.name`保持一致。其他参数无需修改

### 构建镜像

在部署sermant-injector前需要先构建sermant-agent镜像以及sermant-injector镜像。

#### sermant-agent镜像

##### 下载release包

点击 [here](https://github.com/huaweicloud/Sermant/releases)下载release包。

你也可以在项目中执行以下命令来打包：

```shell
mvn clean package -Dmaven.test.skip
```

##### 制作镜像

修改文件夹 `images/sermant-agent`下`build-sermant-image.sh` 脚本中`sermantVersion`,`imageName`和`imageVerison`的值：

> 1. `sermantVersion`为release包的版本
>
> 2. `imageName`为构建的sermant-agent镜像名称
>
> 3. `imageVerison`为构建的sermant-agent镜像版本

在k8s节点下，将`build-sermant-image.sh`和`Sermant.Dockerfile`置于release包`sermant-agent-xxx.tar.gz`同一目录下，执行`build-sermant-image.sh`脚本，完成sermant-agent镜像制作。

```shell
sh build-sermant-image.sh
```

#### sermant-injector镜像

##### sermant-injector打包

在sermant-injector项目下执行`mvn clean package`命令，在项目目录下生成`sermant-injector.jar`文件

##### 制作镜像

修改文件夹 `images/injector`下`build-injector-image.sh` 脚本中`imageName`和`imageVerison`的值：

> 1. `imageName`为构建的sermant-injector镜像名称
> 2. `imageVerison`为构建的sermant-injector镜像版本

在k8s节点下，将`build-injector-image.sh`、`start.sh`和`Injector.Dockerfile`置于sermant-injector包`sermant-injector.jar`同一目录下，执行`build-injector-image.sh`脚本，完成sermant-injector镜像制作。

```shell
sh build-injector-image.sh
```

### 部署sermant-injector实例

在宿主应用容器化部署前，需要先部署sermant-injector实例。本项目采用Helm进行Kubernetes包管理。

按照当前环境使用的k8s的版本，选择`deploment/release`下的`injector_k8s_1.19+`或者`injector_k8s_1.21-`Chart模版(k8s 1.19-1.21版本两者皆可)。

按实际环境修改`values.yaml`中的模版变量：

> 1. `namespace.name`变量与`certificate.sh`中的`NAMESPACE`参数必须保持一致
>
> 2. `agent.image.addr`和`injector.image.addr`变量与构建镜像时的镜像地址保持一致
>
> 3. `injector.webhook.caBundle`变量为k8s证书，可在k8s节点执行以下命令获取：
>
>    ```shell
>    kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[].cluster.certificate-authority-data}'
>    ```

完成后，执行`helm install`命令在k8s中部署sermant-injector实例，以injector_k8s_1.19+ Chart 为例，最后一个参数为Chart模版文件夹路径:

```shell
helm install sermant-injector ../injector_k8s_1.19+
```

检查sermant-injector部署pod状态为running。

至此，宿主应用部署前的环境配置工作完成。

### 部署宿主应用

#### 部署

在完成上述sermant-injector部署后，用户根据实际应用编写YAML部署K8s Deployment资源，只需在`spec > template > metadata> labels`层级加入`sermant-injection: enabled`即可实现自动挂载sermant-agent。(如后续不希望挂载，删除后重新启动应用即可)

```yaml
apiVersion: v1
kind: Deployment
metadata:
  name: demo-test
  labels:
    app: demo-test
spec:
  replicas: 1
  selector:
    app: demo-test
    matchLabels:
      app: demo-test
  template:
    metadata:
      labels:
        app: demo-test
        sermant-injection: enabled
    spec:
      containers:
      - name: image
        # 请替换成您的应用镜像
        image: image:1.0.0
        ports: 
        - containerPort: 8080
  ports:
    - port: 443
      targetPort: 8443
```

若pod无法创建，请检查sermant-injector是否正确部署以及sermant-agent镜像是否正确构建。

#### 验证

pod创建成功后，执行如下命令，其中`${pod_name}`为宿主应用的pod名称

```shell
kubectl get po/${pod_name} -o yaml
```

1. 查看上述命令输出内容`spec > containers > - env`下是否包含环境变量：name为`JAVA_TOOL_OPTIONS`，value为 `-javaagent:/home/sermant-agent/agent/sermant-agent.jar=appName=default`。

2. 查看上述命令输出内容`spec > containers > initContainers > image` 的值是否为构建sermant-agent镜像时的镜像地址。

执行如下命令，其中`${pod_name}`为用户应用的pod名称，`${namespace}`为用户部署应用的namespace名称

```shell
kubectl logs ${pod_name} -n ${namespace}
```

3. 查看上述命令输出内容pod日志开头部分是否包含：

```
[INFO] Loading sermant agent...
```

如果上述信息无误，则表明sermant-agent已成功挂载至用户应用中。

