## RedisCluster的安装
```
RedisCluster最少需要三台主服务器，三台从服务器。
我们采取伪分布式部署方式。
端口号分别为：7001~7006

```
**第一步：安装 C 语言需要的 GCC 环境**

```shell
yum install -y gcc-c++ 
yum install -y wget
```

**第二步：下载并解压缩 Redis 源码压缩包**

```shell
# 进入你准备存放Redis 源码压缩包的目录
cd /opt/study/software/
# 执行下载命令
wget http://download.redis.io/releases/redis-5.0.5.tar.gz 
# 解压
tar -zxf redis-5.0.5.tar.gz
```

**第三步：编译 Redis 源码，进入 redis-5.0.5 目录，执行编译命令**

```shell
cd redis-5.0.5/src 
make
```

**第四步：安装 Redis ，需要通过 PREFIX 指定安装路径**

```shell
mkdir /opt/study/servers/redis-cluster/7001 -p
make install PREFIX=/opt/study/servers/redis-cluster/7001
```

**第五步：拷贝 redis-5.0.5/redis.conf 配置文件到 Redis 安装目录的 bin 目录**

```shell
cp /opt/study/software/redis-5.0.5/redis.conf /opt/study/servers/redis-cluster/7001/bin/
```

**第六步：修改 redis.conf**

```shell
cd /opt/study/servers/redis-cluster/7001/bin/
vim redis.conf
```
修改配置如下：
```shell
# 将`daemonize`由`no`改为`yes` 
daemonize yes 
# 默认绑定的是回环地址，默认不能被其他机器访问 
# bind 127.0.0.1 
# 是否开启保护模式，由yes该为no 
protected-mode no
# 修改端口号
port 7001
# 打开cluster-enable yes
cluster-enable yes
```

**第七步：复制7001，创建7002~7006实例，注意端口修改**

```shell
cd /opt/study/servers/redis-cluster
cp -r 7001 7002
cp -r 7001 7003
cp -r 7001 7004
cp -r 7001 7005
cp -r 7001 7006

# 依次修改各个bin目录下的redis.conf端口号
port 7002
...
```

**第八步：创建start.sh，启动所有的实例**

```shell
cd 7001/bin
./redis-server redis.conf
cd ..
cd ..

cd 7002/bin
./redis-server redis.conf
cd ..
cd ..

cd 7003/bin
./redis-server redis.conf
cd ..
cd ..

cd 7004/bin
./redis-server redis.conf
cd ..
cd ..

cd 7005/bin
./redis-server redis.conf
cd ..
cd ..

cd 7006/bin
./redis-server redis.conf
cd ..
cd ..
```

**第九步：执行start.sh**

```shell
sh start.sh
```

**第十步：创建Redis集群（创建时Redis里不要有数据）**

```shell
# cluster-replicas ： 1 1从机 前三个为主 
# 采用机器ip而不采用127.0.0.1 不然外界无法访问
[root@master redis-cluster]# cd 7001/bin/
[root@master bin]# ./redis-cli --cluster create 192.168.52.129:7001 192.168.52.129:7002 192.168.52.129:7003 192.168.52.129:7004 192.168.52.129:7005 192.168.52.129:7006 --cluster-replicas 1
>>> Performing hash slots allocation on 6 nodes...
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383
Adding replica 192.168.52.129:7005 to 192.168.52.129:7001
Adding replica 192.168.52.129:7006 to 192.168.52.129:7002
Adding replica 192.168.52.129:7004 to 192.168.52.129:7003
>>> Trying to optimize slaves allocation for anti-affinity
[WARNING] Some slaves are in the same host as their master
M: b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001
   slots:[0-5460] (5461 slots) master
M: f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002
   slots:[5461-10922] (5462 slots) master
M: 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003
   slots:[10923-16383] (5461 slots) master
S: e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004
   replicates 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d
S: be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005
   replicates b97488638f8d8bfa5362cfc69e2b722637af658f
S: fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006
   replicates f0afac61d17c158c6a2f658a74ea77a4865ba5c9
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
.......
>>> Performing Cluster Check (using node 192.168.52.129:7001)
M: b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
M: f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
M: 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005
   slots: (0 slots) slave
   replicates b97488638f8d8bfa5362cfc69e2b722637af658f
S: e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004
   slots: (0 slots) slave
   replicates 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d
S: fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006
   slots: (0 slots) slave
   replicates f0afac61d17c158c6a2f658a74ea77a4865ba5c9
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```

**第十一步：使用客户端连接集群**

```shell
# 注意：-c 表示是以redis集群方式进行连接
[root@master bin]# ./redis-cli -h 192.168.52.129 -p 7001 -c
192.168.52.129:7001> set name1 test
-> Redirected to slot [12933] located at 192.168.52.129:7003
OK
# 查看集群状态
192.168.52.129:7003>  cluster info
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:6
cluster_size:3
cluster_current_epoch:6
cluster_my_epoch:3
cluster_stats_messages_ping_sent:371
cluster_stats_messages_pong_sent:355
cluster_stats_messages_meet_sent:1
cluster_stats_messages_sent:727
cluster_stats_messages_ping_received:351
cluster_stats_messages_pong_received:372
cluster_stats_messages_meet_received:4
cluster_stats_messages_received:727
# 查看集群中的节点
192.168.52.129:7003> cluster nodes
e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004@17004 slave 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 0 1598431946659 4 connected
99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003@17003 myself,master - 0 1598431945000 3 connected 10923-16383
b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001@17001 master - 0 1598431942629 1 connected 0-5460
f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002@17002 master - 0 1598431945651 2 connected 5461-10922
be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005@17005 slave b97488638f8d8bfa5362cfc69e2b722637af658f 0 1598431943000 5 connected
fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006@17006 slave f0afac61d17c158c6a2f658a74ea77a4865ba5c9 0 1598431944643 6 connected

```
## RedisCluster的扩容
**第一步：创建7007和7008节点**

```shell
# 创建目录
mkdir /opt/study/servers/redis-cluster/7007
# install文件到7007目录
cd /opt/study/software/redis-5.0.5/src/
make install PREFIX=/opt/study/servers/redis-cluster/7007
# 拷贝redis.conf 
cp /opt/study/software/redis-5.0.5/redis.conf /opt/study/servers/redis-cluster/7007/bin/
# 修改redis.conf 
# 修改内容与前面安装一致，仅端口号改为7007
# 拷贝7007到7008并修改端口号
```

**第二步：启动7007，并添加到集群中**

```shell
[root@master bin]# ./redis-server redis.conf

[root@master bin]# ./redis-cli --cluster add-node 192.168.52.129:7007 192.168.52.129:7001
>>> Adding node 192.168.52.129:7007 to cluster 192.168.52.129:7001
>>> Performing Cluster Check (using node 192.168.52.129:7001)
M: b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
M: f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
M: 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
S: be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005
   slots: (0 slots) slave
   replicates b97488638f8d8bfa5362cfc69e2b722637af658f
S: e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004
   slots: (0 slots) slave
   replicates 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d
S: fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006
   slots: (0 slots) slave
   replicates f0afac61d17c158c6a2f658a74ea77a4865ba5c9
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
>>> Send CLUSTER MEET to node 192.168.52.129:7007 to make it join the cluster.
[OK] New node added correctly.
```
查看集群节点信息
```shell
[root@master bin]# ./redis-cli -h 192.168.52.129 -p 7007 -c
192.168.52.129:7007> cluster nodes
e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004@17004 slave 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 0 1598434062395 3 connected
be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005@17005 slave b97488638f8d8bfa5362cfc69e2b722637af658f 0 1598434061388 1 connected
f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002@17002 master - 0 1598434059000 2 connected 5461-10922
2304ff922bc8dadc76ae05555770bddf62afded7 192.168.52.129:7007@17007 myself,master - 0 1598434062000 0 connected
b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001@17001 master - 0 1598434063400 1 connected 0-5460
99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003@17003 master - 0 1598434064408 3 connected 10923-16383
fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006@17006 slave f0afac61d17c158c6a2f658a74ea77a4865ba5c9 0 1598434062000 2 connected
```
**第三步：hash槽重新分配（数据迁移）**
	**注意：添加完主节点需要对主节点进行hash槽分配，这样该主节才可以存储数据。**

1. 连接上集群
    ```shell
    [root@redis bin]#  ./redis-cli --cluster reshard 192.168.52.129:7007
    >>> Performing Cluster Check (using node 192.168.52.129:7007)
    M: 2304ff922bc8dadc76ae05555770bddf62afded7 192.168.52.129:7007
       slots: (0 slots) master
    S: e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004
       slots: (0 slots) slave
       replicates 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d
    S: be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005
       slots: (0 slots) slave
       replicates b97488638f8d8bfa5362cfc69e2b722637af658f
    M: f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002
       slots:[5461-10922] (5462 slots) master
       1 additional replica(s)
    M: b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001
       slots:[0-5460] (5461 slots) master
       1 additional replica(s)
    M: 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003
       slots:[10923-16383] (5461 slots) master
       1 additional replica(s)
    S: fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006
       slots: (0 slots) slave
       replicates f0afac61d17c158c6a2f658a74ea77a4865ba5c9
    [OK] All nodes agree about slots configuration.
    >>> Check for open slots...
    >>> Check slots coverage...
    [OK] All 16384 slots covered.
    ```

2. 输入要分配的槽数量
    ``` shell
    # 输入4000：表示给目标节点分配4000个槽
    How many slots do you want to move (from 1 to 16384)? 4000
    ```

3. 输入接收槽的结点id
    ```shell
    # 此处输入的是你想给哪个节点分配槽
    # 我们这里是准备给7007分配槽，通过cluster nodes查看7007结点id
    # 2304ff922bc8dadc76ae05555770bddf62afded7
    What is the receiving node ID?2304ff922bc8dadc76ae05555770bddf62afded7
    ```

4. 输入源结点id
    ```shell
    # 这里是选择从哪些节点分出槽给7007
    # all 代表从每一个节点分出一部分给7007
    # done 代表你手动选择节点分出槽给7007
    # 我们这里选择all
    Please enter all the source node IDs.
      Type 'all' to use all the nodes as source nodes for the hash slots.
      Type 'done' once you entered all the source nodes IDs.
    Source node #1: all
    ```

5. 输入yes开始移动槽到目标结点id 
    ```shell
    Do you want to proceed with the proposed reshard plan (yes/no)? yes
    ```

 6. 查看结果
    ```shell
    192.168.52.129:7001> cluster nodes
    f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002@17002 master - 0 1598453697489 2 connected 6795-10922
    99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003@17003 master - 0 1598453699000 3 connected 12256-16383
    be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005@17005 slave b97488638f8d8bfa5362cfc69e2b722637af658f 0 1598453699502 5 connected
    2304ff922bc8dadc76ae05555770bddf62afded7 192.168.52.129:7007@17007 master - 0 1598453696000 7 connected 0-1332 5461-6794 10923-12255
    e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004@17004 slave 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 0 1598453698496 4 connected
    b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001@17001 myself,master - 0 1598453700000 1 connected 1333-5460
    fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006@17006 slave f0afac61d17c158c6a2f658a74ea77a4865ba5c9 0 1598453700510 6 connected
    ```

**第四步：添加从节点**
    **添加7008从结点，将7008作为7007的从结点**
执行命令

```shell
[root@redis bin]# ./redis-cli --cluster add-node 192.168.52.129:7008 192.168.52.129:7007 --cluster-slave --cluster-master-id 2304ff922bc8dadc76ae05555770bddf62afded7
>>> Adding node 192.168.52.129:7008 to cluster 192.168.52.129:7007
>>> Performing Cluster Check (using node 192.168.52.129:7007)
M: 2304ff922bc8dadc76ae05555770bddf62afded7 192.168.52.129:7007
   slots:[0-1332],[5461-6794],[10923-12255] (4000 slots) master
S: e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004
   slots: (0 slots) slave
   replicates 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d
S: be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005
   slots: (0 slots) slave
   replicates b97488638f8d8bfa5362cfc69e2b722637af658f
M: f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002
   slots:[6795-10922] (4128 slots) master
   1 additional replica(s)
M: b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001
   slots:[1333-5460] (4128 slots) master
   1 additional replica(s)
M: 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003
   slots:[12256-16383] (4128 slots) master
   1 additional replica(s)
S: fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006
   slots: (0 slots) slave
   replicates f0afac61d17c158c6a2f658a74ea77a4865ba5c9
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
>>> Send CLUSTER MEET to node 192.168.52.129:7008 to make it join the cluster.
Waiting for the cluster to join

>>> Configure node as replica of 192.168.52.129:7007.
[OK] New node added correctly.
```

**注意：** 
如果原来该结点在集群中的配置信息已经生成到cluster-config-file指定的配置文件中（如果 cluster-config-file没有指定则默认为nodes.conf），这时可能会报错：
```shell
[ERR] Node XXXXXX is not empty. Either the node already knows other nodes (check with CLUSTER NODES) or contains some key in database 0
```
**解决方法：**
删除生成的配置文件nodes.conf，删除后再执行./redis-cli --cluster add-node 指令

**第五步：查看集群节点信息，确认添加节点成功**

```shell
192.168.52.129:7001> cluster nodes
f0afac61d17c158c6a2f658a74ea77a4865ba5c9 192.168.52.129:7002@17002 master - 0 1598454380000 2 connected 6795-10922
99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 192.168.52.129:7003@17003 master - 0 1598454380570 3 connected 12256-16383
be909af55129ff99757e2b0890b60f5efbc1393b 192.168.52.129:7005@17005 slave b97488638f8d8bfa5362cfc69e2b722637af658f 0 1598454380000 5 connected
2304ff922bc8dadc76ae05555770bddf62afded7 192.168.52.129:7007@17007 master - 0 1598454382586 7 connected 0-1332 5461-6794 10923-12255
e3afdc169aa28b391df83e34f36b7d8e4f06d2fe 192.168.52.129:7004@17004 slave 99f2bda42a0456f33491bcd92bdeaf7201ed2a7d 0 1598454382000 4 connected
848025f2015f376789fbb0c32ab13ed907ffbc93 192.168.52.129:7008@17008 slave 2304ff922bc8dadc76ae05555770bddf62afded7 0 1598454379563 7 connected
b97488638f8d8bfa5362cfc69e2b722637af658f 192.168.52.129:7001@17001 myself,master - 0 1598454380000 1 connected 1333-5460
fd337227351543fb567ba2a42515184b66ebe0e5 192.168.52.129:7006@17006 slave f0afac61d17c158c6a2f658a74ea77a4865ba5c9 0 1598454379000 6 connected
```