FROM registry-cbu.huawei.com/op_svc_apm/busybox2:latest
RUN mkdir -p /paas-apm2/javaagent

ADD target/apm-javaagent-*.tar /paas-apm2/javaagent/

RUN find /paas-apm2/javaagent/ -type f | xargs chmod 666 -R && find /paas-apm2/javaagent/ -type d  | xargs chmod 777 && find /paas-apm2/javaagent/ -name "*.sh" | xargs -i chmod 555 {}
