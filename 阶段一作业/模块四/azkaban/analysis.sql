use test;
insert into table user_info select count(1) as active_num, '${hiveconf:YESTERDAY}' as record_date from (select distinct id from user_clicks) t;
