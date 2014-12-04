#!/bin/sh

usage() {
  local message="$1"
  if [[ -n $message ]]; then
    echo "$message"
  fi
  exit
}

[[ $EUID -ne 0 ]] && usage "Need to run as root."

