# image name of sermant-injector
imageName=io.sermant/sermant-injector
# image version of sermant-injector
imageVersion=1.0.0

name=${imageName}:${imageVersion}

echo "=====================build sermant-injector image=================================="

docker build -f ./Injector.Dockerfile -t ${name} .

# docker push ${imageName}
