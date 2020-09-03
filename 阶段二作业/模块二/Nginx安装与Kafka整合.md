 ## Nginx安装与Kafka整合

1. 安装依赖
```shell
yum install wget git -y

yum install gcc-c++ -y
```
2. 下载kafka的c客户端源码
```shell
git clone https://github.com/edenhill/librdkafka
```
3. 进入到librdkafka，然后进行编译
```shell
cd librdkafka

./configure

make

make install
```
4. 下载nginx
```shell
wget http://nginx.org/download/nginx-1.17.8.tar.gz

tar -zxf nginx-1.17.8.tar.gz -C /opt/study/servers/

cd /opt/study/servers/nginx-1.17.8/

yum install gcc zlib zlib-devel openssl openssl-devel pcre pcre-devel-y
```
5. 下载ngx_kafka_module
```shell
cd /opt/study/software/

git clone https://github.com/brg-liuwei/ngx_kafka_module.git

/opt/study/servers/nginx-1.17.8

./configure --add-module=/opt/study/software/ngx_kafka_module/

make

make install
```
6. 修改nginx的配置文件nginx.conf，因为我是默认安装，所以nginx安装路径在/usr/local/nginx
```shell
cd /usr/local/nginx/conf/
vim nginx.conf

# 在http中增加Kafka信息
kafka;
kafka_broker_list 192.168.52.130:9092 192.168.52.131:9092 192.168.52.132:9092;

# 在http的server中修改location
location = /kafka/log {
		add_header 'Access-Control-Allow-Origin' '*' always;
       add_header 'Access-Control-Allow-Credentials' 'true';
       add_header 'Access-Control-Allow-Methods' 'GET, POST, PATCH, DELETE, PUT, OPTIONS';
       add_header 'Access-Control-Allow-Headers' 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,  Access-Control-Expose-Headers, Token, Authorization';
       add_header 'Access-Control-Max-Age' 1728000;
       add_header 'Content-Type' 'text/plain charset=UTF-8';
       add_header 'Content-Length' 0;
       
       # 配置Kafka的topic名称
		kafka_topic tp_log;
        }
```
7. 启动zk和Kafka（注意创建刚刚配置文件中的那个主题）

8. 启动nginx，如果报错，找不到kafka.so.1的文件
```shell
error while loading shared libraries: librdkafka.so.1: cannot open shared object file: No such file or directory
```
**解决办法：**加载so库
```shell
echo "/usr/local/lib" >> /etc/ld.so.conf
ldconfig
```
9. 测试，向nginx中写入数据，然后观察kafka的消费者能不能消费到数据
```shell
curl localhost/kafka/log -d "message send to kafka topic"
```

