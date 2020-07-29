# Impala作业题

## 业务背景

现有收集到用户的页面点击行为日志数据，数据格式如下：

用户id, 点击时间

```txt
user_id click_time
A,2020-05-15 01:30:00
A,2020-05-15 01:35:00
A,2020-05-15 02:00:00
A,2020-05-15 03:00:10
A,2020-05-15 03:05:00
B,2020-05-15 02:03:00
B,2020-05-15 02:29:40
B,2020-05-15 04:00:00
```

业务：

会话概念：用户的一次会话含义是指用户进入系统开始到用户离开算作一次会话，离开或者重新开始一次会话的概念是指用户的两次行为事件差值大于30分钟，

比如以A用户为例：

第一次会话

```txt
A,2020-05-15 01:30:00
A,2020-05-15 01:35:00
A,2020-05-15 02:00:00

```

第二次会话

```txt
A,2020-05-15 03:00:10
A,2020-05-15 03:05:00
```

判断条件是只要两次时间差值大于30分钟就属于两次会话。

## 需求

对用户的日志数据打上会话内序号，如下

```txt
A,2020-05-15 01:30:00,1
A,2020-05-15 01:35:00,2
A,2020-05-15 02:00:00,3
A,2020-05-15 03:00:10,1
A,2020-05-15 03:05:00,2
B,2020-05-15 02:03:00,1
B,2020-05-15 02:29:40,2
B,2020-05-15 04:00:00,1
```



## 实现

在Hive中完成数据加载

```sql
--创建表
drop table if exists user_clicklog;
create table user_clicklog ( 
        user_id string, 
        click_time string
        )
row format delimited fields terminated by ",";

--加载数据
load data local inpath '/root/impala_data/clicklog.dat' into table user_clicklog; 
```



使用Impala sql完成指标统计...

## 答案

```sql
--思路
--1.根据user_id进行分组按时间升序排序，使用分析函数进行第一次开窗，获取组内相邻两条记录的相隔时间记为wc
--当相隔时间大于30分钟时，wc给固定值1，否则给0
--2.嵌套查询，在第一步的基础上依旧保持根据user_id进行分组按时间升序排序，使用分析函数进行第二次开窗
--对组内数据进行求和（求和规则为组内第一条数据到当前条数据总和），依旧记为wc
--3.以前两步所得数据为临时表，user_id,wc进行分组并按时间升序排序，同时使用分析函数进行开窗
--4.使用排名函数row_number()进行排名，记为rank，rank值即为会话序号
with emp as (
    SELECT user_id,click_time,
        sum(wc) OVER(PARTITION BY user_id ORDER BY unix_timestamp(click_time) 
                    ROWS BETWEEN unbounded preceding and current row) wc
    from (SELECT user_id,click_time,
                case when 
                    (unix_timestamp(click_time)-unix_timestamp(
                     lag(click_time) OVER(PARTITION BY user_id ORDER BY 														unix_timestamp(click_time)))
                     )/60 > 30 then 1 
          		ELSE 0 end wc
            from user_clicklog
        ) tmp
)

select user_id,click_time,
	row_number() over(PARTITION BY user_id,wc 
                  	ORDER BY unix_timestamp(click_time)) as rank
from emp

```

