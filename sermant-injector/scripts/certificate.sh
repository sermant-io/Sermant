#! /bin/sh
set -e

APP="sermant-injector"

# 此处必须和deployment/release/helm/values.yaml中namespace保持一致
NAMESPACE="default"

# 0: csr api is not beta
# 1: csr api is beta
IS_BETA=0

checkIsBeta() {
  if [ $(kubectl api-versions | grep -x "certificates.k8s.io/v1" | wc -l) == 0 ];then
    IS_BETA=1
  fi
}

checkNsAndCsr() {
  if [ $(kubectl get ns | grep -w ${NAMESPACE} | wc -l) == 0 ];then
    kubectl create ns ${NAMESPACE}
  fi

  if [ $(kubectl get csr -o name | grep -x "certificatesigningrequest.certificates.k8s.io/${APP}.${NAMESPACE}.svc" | wc -l) != 0 ];then
    kubectl delete csr ${APP}.${NAMESPACE}.svc
  fi
}

generateCsr() {
cat >${APP}.conf<<EOF
[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C = CN
CN = system:node:sermant-injector-node
O = system:nodes

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = ${APP}
DNS.2 = ${APP}.${NAMESPACE}
DNS.3 = ${APP}.${NAMESPACE}.svc
DNS.4 = ${APP}.${NAMESPACE}.svc.cluster
DNS.5 = ${APP}.${NAMESPACE}.svc.cluster.local

[ v3_ext ]
authorityKeyIdentifier=keyid,issuer:always
basicConstraints=CA:FALSE
keyUsage=keyEncipherment,dataEncipherment
extendedKeyUsage=serverAuth,clientAuth
subjectAltName=@alt_names
EOF

  openssl genrsa -out ${APP}.key 2048
  openssl req -new -key ${APP}.key -out ${APP}.csr -config ${APP}.conf
}

createAndApproveCsr() {
kubectl apply -f - <<EOF
apiVersion: certificates.k8s.io/v1
kind: CertificateSigningRequest
metadata:
  name: ${APP}.${NAMESPACE}.svc
  namespace: ${NAMESPACE}
spec:
  request: $(cat ${APP}.csr | base64 -w 0)
  signerName: kubernetes.io/kubelet-serving
  usages:
  - digital signature
  - key encipherment
  - server auth
EOF

  kubectl certificate approve ${APP}.${NAMESPACE}.svc
}

createAndApproveCsrForBeta() {
kubectl apply -f - <<EOF
apiVersion: certificates.k8s.io/v1beta1
kind: CertificateSigningRequest
metadata:
  name: ${APP}.${NAMESPACE}.svc
  namespace: ${NAMESPACE}
spec:
  request: $(cat ${APP}.csr | base64 -w 0)
  usages:
  - digital signature
  - key encipherment
  - server auth
EOF

  kubectl certificate approve ${APP}.${NAMESPACE}.svc
}

createSecret() {
kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: ${APP}-secret
  namespace: ${NAMESPACE}
data:
  ${APP}.key: $(cat ${APP}.key | base64 -w 0)
  ${APP}.pem: $(kubectl get csr ${APP}.${NAMESPACE}.svc -o jsonpath='{.status.certificate}')
EOF
}

clearResource() {
  rm -f ${APP}.conf
  rm -f ${APP}.key
  rm -f ${APP}.csr
}

checkIsBeta
checkNsAndCsr
generateCsr
if [ $IS_BETA == 0 ];then
  createAndApproveCsr
else
  createAndApproveCsrForBeta
fi
createSecret
clearResource