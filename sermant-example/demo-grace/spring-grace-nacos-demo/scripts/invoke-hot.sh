#!/bin/bash
. ./config.sh
url=${endpoint}/graceHot
while true
do
  echo `curl -s ${url}`
done
