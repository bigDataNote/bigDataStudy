package com.study;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author zhouhao
 * @create 2020-08-18 21:03
 *
 * 创建一张维护社交好友关系表
 * 表名：relation
 * rowKey：uid
 * 列族：friends
 *
 * 需求：
 * 1. 使用Hbase相关API创建一张结构如上的表
 * 2. 删除好友操作实现（好友关系双向，一方删除好友，另一方也会被迫删除好友）
 *    例如：uid1用户执行删除uid2这个好友，则uid2的好友列表中也必须删除uid1
 *
 * 实现思路：
 * 1. 初始化与HBase集群的连接
 * 2. 实现创建relation表的方法
 * 3. 初始化测试数据的方法
 * 4. 实现删除好友信息的方法
 *
 * 注：双向删除的操作使用协处理器处理，具体实现详见协处理器DeleteProcessor实现代码，
 *     这里不做双向删除操作，只为方便测试
 */
public class HbaseDemo {


    private static Connection conn = null;

    //使用静态方法初始化连接信息，减少重复获取连接信息
    static {
        //获取一个配置文件对象
        Configuration conf = HBaseConfiguration.create();
        //设置连接信息
        conf.set("hbase.zookeeper.quorum", "master,slave1");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        //通过conf获取到hbase集群的连接
        try {
            conn = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建HBase表方法
     * @throws IOException
     */
    private void createRelationTable() throws IOException {
        //获取HBaseAdmin对象用来创建表
        HBaseAdmin admin = (HBaseAdmin) conn.getAdmin();
        //创建HTableDescriptor描述器，表描述器
        HTableDescriptor relation = new HTableDescriptor(TableName.valueOf("relation"));
        //指定列族
        relation.addFamily(new HColumnDescriptor("friends"));
        admin.createTable(relation);
        //关闭HBaseAdmin对象
        admin.close();
        System.out.println("relation表创建成功!");
    }

    /**
     * 初始化数据方法（即插入数据）
     * @throws IOException
     */
    private void initRelationData() throws IOException {
        //需要获取一个table对象
        Table relation = conn.getTable(TableName.valueOf("relation"));
        //由于我们不止初始化一条数据，所以我们先准备一个List列表，用来存放初始化的数据
        ArrayList<Put> puts = new ArrayList<Put>();
        //插入数据需要使用Put对象
        //插入uid1用户
        Put uid1 = new Put(Bytes.toBytes("uid1"));
        uid1.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid2"), Bytes.toBytes("uid2"));
        uid1.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid3"), Bytes.toBytes("uid3"));
        uid1.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid4"), Bytes.toBytes("uid4"));
        puts.add(uid1);
        //插入uid2用户
        Put uid2 = new Put(Bytes.toBytes("uid2"));
        uid2.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid1"), Bytes.toBytes("uid1"));
        uid2.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid3"), Bytes.toBytes("uid3"));
        uid2.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid4"), Bytes.toBytes("uid4"));
        puts.add(uid2);
        //插入uid3用户
        Put uid3 = new Put(Bytes.toBytes("uid3"));
        uid3.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid1"), Bytes.toBytes("uid1"));
        uid3.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid2"), Bytes.toBytes("uid2"));
        uid3.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid4"), Bytes.toBytes("uid4"));
        puts.add(uid3);
        //插入uid4用户
        Put uid4 = new Put(Bytes.toBytes("uid4"));
        uid4.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid1"), Bytes.toBytes("uid1"));
        uid4.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid2"), Bytes.toBytes("uid2"));
        uid4.addColumn(Bytes.toBytes("friends"), Bytes.toBytes("uid3"), Bytes.toBytes("uid3"));
        puts.add(uid4);
        //执行写入
        relation.put(puts);
        //关闭table对象
        relation.close();
        System.out.println("数据初始化成功!");
    }

    /**
     * 删除好友关系信息
     * @param uid rowKey
     * @param friend 需要删除的列
     * @throws IOException
     */
    private void deleteFriends(String uid, String friend) throws IOException {
        //需要获取一个table对象
        Table relation = conn.getTable(TableName.valueOf("relation"));
        //删除需要准备delete对象
        Delete delete = new Delete(Bytes.toBytes(uid));
        delete.addColumn(Bytes.toBytes("friends"), Bytes.toBytes(friend));
        //执行删除
        relation.delete(delete);
        //关闭table对象
        relation.close();
        System.out.println("数据删除成功!");
    }

    public static void main(String[] args) throws IOException {
        HbaseDemo hbaseDemo = new HbaseDemo();
        //创建relation表
        //hbaseDemo.createRelationTable();
        //初始化测试数据
        //hbaseDemo.initRelationData();
        //假设uid1用户删除uid2这个好友关系
        //注：此处为单向删除，双向删除使用协处理器实现哈，具体实现详见协处理器DeleteProcessor实现代码
        hbaseDemo.deleteFriends("uid1", "uid2");
    }
}
