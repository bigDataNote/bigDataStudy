package moduleOne.homework;

/**
 * @author zhouhao
 * @create 2020-07-08 22:00
 */

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * 1 继承Mapper类,选择org.apache.hadoop.mapreduce.Mapper包
 * 2 Mapper类的泛型参数，共4个，两对kv
 * 2.1 第一对kv：map输入的参数类型
 * 2.2 第二对kv：map输出参数类型
 * LongWritable, Text:文本偏移量(后面不会用到)，一行文本内容
 * SortBean, NullWritable：SortBean对象，Null
 * 3 重写Mapper类的map方法
 */
public class SortMapper extends Mapper<LongWritable, Text, SortBean, NullWritable> {

    private SortBean sortBean = new SortBean();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //1.将接收到的文本转换成String
        String str = value.toString();
        //2.按照空格对数据进行切分
        String[] numbers = str.split(" ");
        //3.解析字段并封装到sortBean对象中
        sortBean.setNumberValue(Integer.parseInt(numbers[0]));
        //为了方便，暂时都设置为1
        sortBean.setNumber(1);
        //4.输出对象<SortBean对象，Null>
        context.write(sortBean, NullWritable.get());
    }
}
