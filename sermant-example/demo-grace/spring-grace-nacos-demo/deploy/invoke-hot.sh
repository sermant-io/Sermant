#!/bin/bash
endpoint=127.0.0.1:31021
url=${endpoint}/graceHot
while true
do
  echo `curl -s ${url}`
done
