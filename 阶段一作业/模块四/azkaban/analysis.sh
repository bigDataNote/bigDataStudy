#!/bin/bash
#define the date of yesterday
YESTERDAY=`date -d '-1 days' +%Y%m%d`
echo "${YESTERDAY}"
/opt/study/servers/hive-2.3.7/bin/hive --hiveconf YESTERDAY=$YESTERDAY -f analysis.sql
