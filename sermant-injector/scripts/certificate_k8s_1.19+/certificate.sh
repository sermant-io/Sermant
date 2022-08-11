#! /bin/sh

APP="sermant-injector"

# 此处必须和deployment/release/helm/values.yaml中namespace保持一致
NAMESPACE="default"

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

kubectl create ns ${NAMESPACE}

kubectl delete csr ${APP}.${NAMESPACE}.svc

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