# Containerized Deployment Guide

[简体中文](injector-zh.md) | [English](injector.md)

In Kubernetes environment, Sermant supports quickly deployment by using **sermant-injector** module to automatically mount sermant-agent package for host application. This document describes the **sermant-injector** module and how to deploy **sermant-injector** and **host application with sermant-agent** in k8s environment.

## Sermant-injector
Sermant-injector is based on the **Kubernetes Admission Controllers.** The admission controller is located in the K8s API Server and is able to intercept requests to the API Server to complete operations such as authentication, authorization, and mutation.

Serment-injector is a MutatingAdmissionWebhook that can intercept and modify requests before creating container resources. After serment-injector is deployed on K8s, just add `sermant-injection:Enabled` to the YAML file of the host application deployment configuration at the `spec > Template > metadata> labels` ' then the host application can automatically mount the sermant-agent package.

### Components
`sermant-injector` contains:

- `deployment`, contains helm package file for deploying the sermant-injector application.
  - `release`
    - `injector_k8s_1.19+` contains helm package file for K8S 1.19+.
    - `injector_k8s_1.21-` contains helm package file for K8S 1.21-.
- `images`
  - `injector`, contains dockerfile and image build script of **sermant-injector**.
  - `sermant-agent`, contains dockerfile and image build script of **sermant-agent**.
- `scripts`, contains certificate generation scripts, etc.
- `src`, contains source code.

## Containerized Deployment Steps

### **Runtime Environment**
[Kubernetes 1.19+](https://kubernetes.io/)

[Helm v3](https://helm.sh/)

### Generate a Certificate for Sermant-injector

The Kubernetes webhook can only be invoked over HTTPS(SSL/TLS), so the SSL key and certificate need to be generated for the sermant-injector.

Execute the `certificate.sh` script under `scripts` in any directory of any K8s node, according to the version of K8s used in the current environment. (K8s version in 1.19-1.21 suits both)

> NOTE：The parameter `NAMESPACE` must be set as the same value as `namespace.name` in `values.yaml` under `deployment/release`. No need to modify other parameters.

### Build Images

Before deploying **sermant-injector**, you need to build the **sermant-agent** image and the **sermant-injector** image.

#### Image of Sermant-agent

##### Download release package

Click [here](https://github.com/huaweicloud/Sermant/releases) to download latest release package `sermant-agent-x.x.x.tar.gz`.

Or you can get above package by executing the following command in the project.

```shell
mvn clean package -Dmaven.test.skip
```

##### Build Image

Modify the values of `sermantVersion`, `imageName` and `imageVerison` in the `build-sermant-image.sh` under `images/sermant-agent` folder:

> 1. `sermantVersion` is the version of the release package.
>
> 2. `imageName` is the name of the built sermant-agent image.
>
> 3. `imageVerison` is the version for the built sermant-agent image.

Move `build-sermant-image.sh` and `Sermant.dockerfile` to the same directory as the release package `sermant-agent-xxx.tar.gz` in one of K8s nodes. Run `build-sermant-image.sh` to build the sermant-agent image.

```shell
sh build-sermant-image.sh
```

#### Image of Sermant-injector

##### Package Sermant-injector

Execute the `mvn clean package` command to generate the `sermant-injector.jar` file in the directory of sermant-injector project.

##### Build Image

Modify the values of `imageName` and `imageVerison` in the `build-injector-image.sh` script under `images/injector` folder:

> 1. `imageName` is the name of the built image of sermant-injector.
> 2. `imageVerison` is the version of the built image of sermant-injector.

Move `build-injector-image.sh`, `start.sh` and `Injector.Dockerfile` to the same directory as the package `sermant-injector.jar`. Run `build-injector-image.sh` to create the sermant-injector image.

```shell
sh build-injector-image.sh
```

### Deploy Workload of Sermant-injector 

Before the host application can be containerized, the workload of sermant-injector needs to be deployed. This project adopts Helm for Kubernetes package management.

Choose either Chart template in`injector_k8s_1.19+` or `injector_k8s_1.21-` under `deploment/release`, according to the version of K8s used in the current environment (K8s version in 1.19-1.21 suits both).

Modify the template variable in `values.yaml` according to the actual environment:

> 1. The value of `namespace.name` must be identical to the `NAMESPACE` in `certificate.sh`
>
> 2. The values of `agent.image.addr` and `injector.image.addr` are the same as the image address when the images are built
>
> 3. `injector.webhook.caBundle` is the K8s certificate, which can be obtained from the K8s node by executing the following command：
>
>    ```shell
>    kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[].cluster.certificate-authority-data}'
>    ```

Once this is done, execute `helm install` to deploy the sermant-injector workload in K8s, taking the Chart of injector_k8s_1.19+ as an example, with the Chart folder path as the last argument:

```shell
helm install sermant-injector ../injector_k8s_1.19+
```

Check that the status of the deployed pod of sermant-injector is running.

At this point, the environment configuration of the host application before deployment is complete.

### Deploy Host Application 

#### Deployment

After the deployment of above sermant-injector, developers should write YAML file to deploy K8s Deployment resources according to the actual application. Simply add `sermant-injection: enabled` at the `spec > Template > Metadata > Labels` to automatically mount the sermant-agent. (If you do not want to mount it later, just delete it and restart the application)

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
        # Please replace it with own image
        image: image:1.0.0
        ports: 
        - containerPort: 8080
  ports:
    - port: 443
      targetPort: 8443
```

If the pod cannot be created, check that the sermant-injector is deployed correctly and that the sermant-agent image is built correctly.

#### Verification

Once the pod is created, execute the following command, where `${pod_name}` is the pod name of host application.

```shell
kubectl get po/${pod_name} -o yaml
```

1. Check if the output contains the environment variable whose name is `JAVA_TOOL_OPTIONS`and value is `-javaagent:/home/sermant-agent/agent/sermant-agent.jar=appName=default` in `spec > containers > - env`.

2. Check if the value of `spec > containers > initContainers > image` is the image address used to build the sermant-agent image.

Execute the following command, where `${pod_name}` is the pod name of host application and `${namespace}` is the namespace name of deployed host application.

```shell
kubectl logs ${pod_name} -n ${namespace}
```

3. Check if the beginning of the pod log in the output of the above command contains:

```
[INFO] Loading sermant agent...
```

If the above information is correct, the sermant-agent has been successfully mounted into the host application.
