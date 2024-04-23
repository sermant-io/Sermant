# sermant suffix version of the packaged folder name
sermantVersion=1.0.0
# image name of sermant-agent
imageName=io.sermant/sermant-agent
# image version of sermant-agent
imageVersion=1.0.0

name=${imageName}:${imageVersion}

echo "=====================rename sermant-agent package=================================="

if [ -f "sermant-agent-${sermantVersion}.tar.gz" ]; then
  cp -f sermant-agent-${sermantVersion}.tar.gz sermant-agent.tar.gz
else
  echo "sermant-agent-${sermantVersion}.tar.gz doesn't exist"
  exit 1
fi

echo "=====================build sermant-agent image=================================="

docker build -f ./Sermant.Dockerfile -t ${name} .

# docker push ${name}