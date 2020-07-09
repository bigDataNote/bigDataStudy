package moduleOne.homework;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author zhouhao
 * @create 2020-07-08 21:46
 */

/**
 * 过程梳理：
 * 1.自定义对象作为Map的key输出时，需要实现WritableComparable接口
 * 2.定义SortBean属性，number：用来存numberValue在整个排序中的顺序（升序）；numberValue：用来保存当前的数值
 * 3.生成Getter和Setter方法
 * 4.生成空参构造函数和有参构造函数
 * 5.重写序列化write()方法、反序列化readFields()方法、toString()方法
 */
public class SortBean implements WritableComparable<SortBean> {

    private Integer number;//序号
    private Integer numberValue;//序号对应的值

    //空参构造函数
    public SortBean() {
    }

    //有参构造函数
    public SortBean(Integer number, Integer numberValue) {
        this.number = number;
        this.numberValue = numberValue;
    }

    //序列化方法，就是把内容输出到网络或者文本中
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(number);
        dataOutput.writeInt(numberValue);
    }

    //反序列化方法，要求序列化和反序列化方法字段顺序要保持一致
    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.number = dataInput.readInt();
        this.numberValue = dataInput.readInt();
    }

    //指定排序规则，这里我们对numberValue按照升序进行排序
    //返回值有三种：0：等于；1：大于；-1：小于
    @Override
    public int compareTo(SortBean o) {
        if (this.numberValue > o.numberValue) {
            return 1;
        } else if (this.numberValue < o.numberValue) {
            return -1;
        } else {
            return 0;
        }

    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Integer numberValue) {
        this.numberValue = numberValue;
    }

    //为了方便查看输出的值，重写toString()方法
    @Override
    public String toString() {
        return number + "\t" + numberValue;
    }
}
