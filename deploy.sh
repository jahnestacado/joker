#!/bin/bash
  
export HOST_IP=`hostname -I | cut -d' ' -f 1`

echo "Setting stack IP to $HOST_IP"

STACK_FILE="docker-stack-arm64v8.yml"
while getopts "d" opt; do
  case "$opt" in

  d)  STACK_FILE="docker-stack.yml"
      ;;

  esac
done


docker stack deploy -c $STACK_FILE joker

