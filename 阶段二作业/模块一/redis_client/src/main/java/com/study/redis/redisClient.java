package com.study.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;

/**
 * @author zhouhao
 * @create 2020-08-26 23:22
 */
public class redisClient {

    public static void main(String[] args) {
        JedisPoolConfig config = new JedisPoolConfig();
        HashSet<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7001));
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7002));
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7003));
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7004));
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7005));
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7006));
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7007));
        hostAndPorts.add(new HostAndPort("192.168.52.129", 7008));
        JedisCluster jedisCluster = new JedisCluster(hostAndPorts, config);
        String result = jedisCluster.set("test:name:002", "002");
        if ("OK".equals(result)) {
            System.out.println("缓存添加成功！");
        }
        String value = jedisCluster.get("test:name:002");
        System.out.println("获取value成功，value："+value);
    }
}
