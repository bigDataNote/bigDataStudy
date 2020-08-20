use test;
alter table user_clicks add
partition(dt='${hiveconf:YESTERDAY}') location
'/user_clicks/${hiveconf:YESTERDAY}/';