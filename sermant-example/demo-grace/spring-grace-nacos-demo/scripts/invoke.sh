#!/bin/bash
. ./config.sh
openUrl=${endpoint}/graceDownOpen
closeUrl=${endpoint}/graceDownClose
while true
do
  echo `curl -s ${openUrl}`
  echo `curl -s ${closeUrl}`
done
