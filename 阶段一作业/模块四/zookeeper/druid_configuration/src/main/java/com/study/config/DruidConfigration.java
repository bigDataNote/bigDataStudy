package com.study.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * @author zhouhao
 * @create 2020-08-15 00:47
 */

@Configuration  
public class DruidConfigration {  
    @Value("${spring.datasource.url}")
    private String dbUrl;  
    @Value("${spring.datasource.username}")
    private String username;  
    @Value("${spring.datasource.password}")  
    private String password;  
    @Value("${spring.datasource.driver-class-name}")  
    private String driverClassName;  
    @Value("${spring.datasource.initialSize}")  
    private int initialSize;  
    @Value("${spring.datasource.minIdle}")  
    private int minIdle;  
    @Value("${spring.datasource.maxActive}")  
    private int maxActive;  
    @Value("${spring.datasource.maxWait}")  
    private int maxWait;  
    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")  
    private int timeBetweenEvictionRunsMillis;  
    @Value("${spring.datasource.minEvictableIdleTimeMillis}")  
    private int minEvictableIdleTimeMillis;  
    @Value("${spring.datasource.validationQuery}")  
    private String validationQuery;  
    @Value("${spring.datasource.testWhileIdle}")  
    private boolean testWhileIdle;  
    @Value("${spring.datasource.testOnBorrow}")  
    private boolean testOnBorrow;  
    @Value("${spring.datasource.testOnReturn}")  
    private boolean testOnReturn;  
    @Value("${spring.datasource.poolPreparedStatements}")  
    private boolean poolPreparedStatements;  
    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")  
    private int maxPoolPreparedStatementPerConnectionSize;  
    @Value("${spring.datasource.filters}")  
    private String filters;  
    @Value("${spring.datasource.connectionProperties}")  
    private String connectionProperties;  
    @Value("${spring.datasource.useGlobalDataSourceStat}")  
    private boolean useGlobalDataSourceStat;

    private final static String DATASOURCE_TAG = "db";

    public DataSource dataSource(){
        // 获取zkClient对象
        ZkClient zkClient = new ZkClient("slave1:2181");
        //设置自定义的序列化类型,否则会报错！！
        zkClient.setZkSerializer(new ZkStrSerializer());

        DruidDataSource datasource = new DruidDataSource();


        //判断节点是否存在，不存在创建节点并赋值
        final boolean exists = zkClient.exists("/MySQL");
        if (exists) {
            Object data = zkClient.readData("/MySQL");
            String[] param = data.toString().split("\\|");
            dbUrl = param[0];
            username = param[1];
        }
        //DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(dbUrl);
        datasource.setUsername(username);  
        datasource.setPassword(password);  
        datasource.setDriverClassName(driverClassName);
        return datasource;
    }

    @Bean
    public void zkWatcher(){
        // 获取zkClient对象
        ZkClient zkClient = new ZkClient("slave1:2181");
        //设置自定义的序列化类型,否则会报错！！
        zkClient.setZkSerializer(new ZkStrSerializer());
        //注册监听器，节点数据改变的类型，接收通知后的处理逻辑定义
        zkClient.subscribeDataChanges("/MySQL", new IZkDataListener() {
            public void handleDataChange(String path, Object data) throws Exception {
                //定义接收通知之后的处理逻辑
                DynamicDataSource  source = RuntimeContext.getBean(DynamicDataSource.class);
                //当检测到数据库地址改变时，重新设置数据源
                source.setTargetDataSources(Collections.singletonMap(DATASOURCE_TAG, dataSource()));
                //调用该方法刷新resolvedDataSources，下次获取数据源时将获取到新设置的数据源
                source.afterPropertiesSet();
            }

            //数据删除--》节点删除
            public void handleDataDeleted(String path) throws Exception {
                System.out.println(path + " is deleted!!");
            }
        });
    }

    @Bean("dataSource")
    public DynamicDataSource dynamicDataSource() {
        DynamicDataSource source = new DynamicDataSource();
        //只有一个数据源，传入的Map的key为db,value为使用的数据源
        source.setTargetDataSources(Collections.singletonMap(DATASOURCE_TAG, dataSource()));
        return source;
    }

    //简单实现AbstractRoutingDataSource，因为只是有一个数据源，所以任何时候选择的数据源都一样
    class DynamicDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() { return DATASOURCE_TAG; }
    }
}