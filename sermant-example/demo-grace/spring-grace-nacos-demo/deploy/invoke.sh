#!/bin/bash
endpoint=127.0.0.1:31021
openUrl=${endpoint}/graceDownOpen
closeUrl=${endpoint}/graceDownClose
while true
do
  echo `curl -s ${openUrl}`
  echo `curl -s ${closeUrl}`
done
