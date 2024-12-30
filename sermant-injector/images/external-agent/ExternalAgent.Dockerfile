FROM alpine:latest

WORKDIR /home

# must copy agent file to /home, 'opentelemetry-javaagent.jar' is an example
COPY opentelemetry-javaagent.jar /home
