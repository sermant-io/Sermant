#!/bin/sh

function checkArgs()
{
    param_num=$#

    for i in `seq $(($param_num/2))`
    do
        [[ ${1#-} == "master_address" ]] && { masterIp=$2;shift 2;continue; }
        [[ ${1#-} == "app_name" ]] && { appName=$2;shift 2;continue; }
        [[ ${1#-} == "env" ]] && { env=$2;shift 2;continue; }
        [[ ${1#-} == "business" ]] && { business=$2;shift 2;continue; }
        [[ ${1#-} == "sub_business" ]] && { sub_business=$2;shift 2;continue; }
        [[ ${1#-} == "access_key" ]] && { ak=$2;shift 2;continue; }
        [[ ${1#-} == "access_value" ]] && { sk=$2;shift 2;continue; }
    done
}

checkArgs "$@"

if [ ! -z "$appName" ]; then
  sed -i "s#\#app.name={{app_name}}#app.name=$appName#g" apm.config
fi

if [ ! -z "$masterIp" ]; then
  sed -i "s#\#master.address={{master_address}}#master.address=$masterIp#g" apm.config
fi

if [ ! -z "$env" ]; then
  sed -i "s#\#env={{env}}#env=$env#g" apm.config
fi

if [ ! -z "$business" ]; then
  sed -i "s#\#business={{business}}#business=$business#g" apm.config
fi

if [ ! -z "$sub_business" ]; then
  sed -i "s#\#sub.business={{sub_business}}#sub.business=$sub_business#g" apm.config
fi

if [ ! -z "$ak" ]; then
  sed -i "s#\#access.key={{ak}}#access.key=$ak#g" apm.config
fi

if [ ! -z "$sk" ]; then
  sed -i "s#\#secret.key={{sk}}#secret.key=$sk#g" apm.config
fi

cp -rf /paas-apm2/javaagent/* /var/init/javaagent
