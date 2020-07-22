##### Hive作业

第一题：找出全部夺得3连贯的队伍

```sql
--思路
--1.根据队伍进行分组并按时间升序排序，并使用row_number()对组内数据进行编号
--2.用年份减去每条数据的编号值得到下一次分组条件gid，并生成一张临时表
--3.从临时表中根据team,gid进行group by，获取队伍名和连续获胜年数（即count（1）数据）
--4.根据需求，筛选count（1）数大于或等于3的数据，即为全部夺得3连贯的队伍
--5.对数据进行处理，是查询结果更符合需求
with tmp as (
select team,
year - row_number() over (partition by team order by year) as gid 
from t1)

select distinct team
from (select team
        from tmp
        group by team,gid
        having count(1) >= 3
    ) tmp2 
```



第二题：找出每个id在在一天之内所有的波峰与波谷值

~~~sql
--思路
--1.为了方便排序，我们拼接日期字符串（假定日期都为2020-01-01，秒数都为0），然后转换成时间戳
--2.根据id分组按时间升序排序，并且使用序列函数获得当前行前一条数据和后一条数据，生成基础数据临时表
--3.从临时表中查询数据，使用case when对数据进行标记
--当当前行数据都大于或等于前一行和后一行数据时，标记为波峰
--当当前行数据都小于或等于前一行和后一行数据时，标记为波谷，其他为0）
--4.嵌套查询，排除标记字段为0的数据，则剩下数据就是波峰波谷数据
WITH tmp as (
    SELECT id,`time`,gid,price,
        lag(price) OVER(PARTITION BY id ORDER BY gid) as lagprice,
        lead(price) OVER(PARTITION BY id ORDER BY gid) as leadprice
    from (select id,`time`,unix_timestamp('2020-01-01'||' '|| `time`|| ':00') gid,price from t2) t1
)

select id,`time`,price,feature
FROM (SELECT id,`time`,price,
            case when price >= lagprice and price >= leadprice then '波峰'
                when price <= lagprice and price <= leadprice then '波⾕'
            ELSE '0' end as feature
        from tmp
    ) tmp2
where feature != '0'
~~~



第三题：

3.1、每个id浏览时⻓、步⻓ 

~~~sql
--思路
--根据id进行GROUP BY
--取时间最大值和最小值之差即为浏览时长，count（1）即为步长
SELECT id,(unix_timestamp(max(dt),'yyyy/MM/dd HH:mm')
           -unix_timestamp(min(dt),'yyyy/MM/dd HH:mm'))/60 sumtime,
	count(1) countnum
from t3 GROUP BY id
~~~

3.2、如果两次浏览之间的间隔超过30分钟，认为是两个不同的浏览时间； 再求每个id浏览时⻓、步⻓

~~~sql
--思路
--1.根据id进行分组并按时间升序排序，同时使用分析函数进行第一次开窗，获取组内相邻两条记录的相隔时间记为wc
--当相隔时间大于30分钟时，wc给固定值1，否则给0
--2.嵌套查询，在第一步的基础上依旧保持根据id进行分组并按时间升序排序，同时使用分析函数进行第二次开窗
--对组内数据进行求和（求和规则为组内第一条数据到当前条数据总和），依旧记为wc
--3.以前两步所得数据为临时表，根据id,wc进行GROUP BY
--取时间最大值和最小值之差即为浏览时长，count（1）即为步长
WITH basetable as (
    SELECT id,dt,browseid,
    sum(wc) OVER(PARTITION BY id 
                 ORDER BY unix_timestamp(dt,'yyyy/MM/dd HH:mm') 
                 ROWS BETWEEN unbounded preceding and current row) wc
FROM (SELECT id,dt,browseid,
            case when 
      			(unix_timestamp(dt,'yyyy/MM/dd HH:mm')-unix_timestamp(
                    lag(dt) OVER(PARTITION BY id ORDER BY unix_timestamp(
                        dt,'yyyy/MM/dd HH:mm')) ,'yyyy/MM/dd HH:mm')
                )/60 > 30 
      		then 1 ELSE 0 end wc
        FROM t3
    ) tmp
)

SELECT id,(unix_timestamp(max(dt),'yyyy/MM/dd HH:mm')
           -unix_timestamp(min(dt),'yyyy/MM/dd HH:mm'))/60 sumtime,
	count(1) countnum
from basetable 
GROUP BY id,wc
~~~

