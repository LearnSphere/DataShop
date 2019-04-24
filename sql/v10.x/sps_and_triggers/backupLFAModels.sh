#!/usr/bin/env bash

db=$1
user=$2
pass=$3

shift 3
models=$*

mysql $db -u $user -p$pass -e "CALL lfa_backup_models('$models');"

