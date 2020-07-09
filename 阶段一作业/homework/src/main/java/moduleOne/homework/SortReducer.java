package moduleOne.homework;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * @author zhouhao
 * @create 2020-07-08 22:27
 */

/**
 * 1 Reducer,选择org.apache.hadoop.mapreduce.Reducer包
 * 2 Reducer类的泛型参数，共4个，两对kv
 * 2.1 第一对kv：类型要和map输出类型一致：SortBean, NullWritable
 * 2.2 第二对kv：自己设计决定输出的结果数据是什么类型：SortBean, NullWritable
 * 3 重写Reducer类的Reduce方法
 */
public class SortReducer extends Reducer<SortBean, NullWritable, SortBean, NullWritable> {
    //全局变量，记录序号值
    private int number = 0;

    @Override
    protected void reduce(SortBean key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
        //防止我们重写的compareTo()方法导致数值一样时合并了key，所有遍历values获取每一个key
        for (NullWritable value : values) {
            //每获取一个key对象，都需要加1，保证key的顺序
            number++;
            key.setNumber(number);
            context.write(key,value);
        }
    }
}
