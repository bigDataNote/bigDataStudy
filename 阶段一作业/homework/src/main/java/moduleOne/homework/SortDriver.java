package moduleOne.homework;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @author zhouhao
 * @create 2020-07-08 22:35
 */
public class SortDriver {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        //1.获取配置文件对象，获取job对象实例
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "SortDriver");
        //2.指定程序jar的本地路径
        job.setJarByClass(SortDriver.class);
        //3.指定Mapper类/Reducer类
        job.setMapperClass(SortMapper.class);
        job.setReducerClass(SortReducer.class);
        //4.指定Mapper输出的kv数据类型
        job.setMapOutputKeyClass(SortBean.class);
        job.setMapOutputValueClass(NullWritable.class);
        //5.指定Reducer最终输出的kv数据类型
        job.setOutputKeyClass(SortBean.class);
        job.setOutputValueClass(NullWritable.class);
        //6.指定job处理原始数据路径
        FileInputFormat.setInputPaths(job,new Path("J:\\input"));
        //7.指定job最终结果输出的路径
        FileOutputFormat.setOutputPath(job,new Path("J:\\out"));
        //8.提交job作业
        boolean flag = job.waitForCompletion(true);
        //9.jvm退出,正常退出0，非0则是错误推出
        System.exit(flag?0:1);
    }
}
