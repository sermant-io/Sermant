# image name of external-agent, 'opentelemetry-javaagent' is an example
imageName=io.sermant/opentelemetry-javaagent
# image version of external-agent
imageVersion=2.10.0

name=${imageName}:${imageVersion}
docker build -f dockerfile -t ${name} .
