#!/bin/bash

if [ $# == 3 ]; then
    DB=$1
    USER=$2
    MY_PWD=$3
else
    TTY_MODES=`stty -g`

    printf "database: "
    read DB

    printf "username: "
    read USER

    printf "password: "
    stty -echo
    read MY_PWD
    stty $TTY_MODES
fi

echo
echo Loading stored procs and triggers to database $DB
echo

echo Loading discourse_delete_sp.sql at $(date "+%Y-%m-%d %H:%M:%S")
mysql -u $USER -p$MY_PWD $DB < discourse_delete_sp.sql

