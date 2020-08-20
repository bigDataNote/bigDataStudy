package com.study;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @author zhouhao
 * @create 2020-08-18 22:07
 *
 * 实现思路：
 * preDelete和postDelete选择
 * preDelete：删除数据之前执行协处理器中逻辑
 * postDelete：删除数据之后执行协处理器中逻辑
 * 因为后续需要通过判断数据是否存在来跳出递归，因此我们选择重写postDelete方法
 *
 * 具体实现：
 * 1.通过上下文获取relation表table对象
 * 2.通过传递进来的参数Delete对象获取所有的cell对象
 * 3.循环遍历所有的cell对象，依次删除我们需要删除的好友信息
 */
public class DeleteProcessor extends BaseRegionObserver {

    private  static final Logger logger = LoggerFactory.getLogger(DeleteProcessor.class);

    @Override
    public void postDelete(ObserverContext<RegionCoprocessorEnvironment> e, Delete delete, WALEdit edit, Durability durability) throws IOException {
        //获取relation表table对象
        HTableInterface relation = e.getEnvironment().getTable(TableName.valueOf("relation"));
        //获取rowKey
        byte[] uid = delete.getRow();
        //获取所有的cell对象
        NavigableMap<byte[], List<Cell>> familyCellMap = delete.getFamilyCellMap();
        Set<Map.Entry<byte[], List<Cell>>> entrySet = familyCellMap.entrySet();
        for (Map.Entry<byte[], List<Cell>> entry : entrySet) {
            logger.info("列族信息："+ Bytes.toString(entry.getKey()));
            List<Cell> cells = entry.getValue();
            for (Cell cell : cells) {
                byte[] rowKey = CellUtil.cloneRow(cell);
                byte[] column = CellUtil.cloneQualifier(cell);
                logger.info("rowKey信息:"+Bytes.toString(rowKey)+",列信息:"+Bytes.toString(column));
                //因为我们是为了实现双向删除，所以协处理器的触发条件是删除数据时触发，
                //但是当我们第一次触发后，接下来的操作还是删除数据，会再次触发协处理器，
                //eg：hbase删除uid1用户uid2好友信息时，会触发协处理器实现删除uid2用户uid1好友信息，
                //然而，当hbase执行删除uid2用户uid1好友信息时，会再一次触发协处理器，进行删除uid1用户uid2好友信息操作，
                //形成递归操作，会造成资源耗尽，且这一次uid1用户uid2好友信息已经不存在，不需要进行删除操作，
                //因此，我们在执行删除操作之前进行判断需要删除的数据是否存在，存在则执行删除，不存在则不执行，程序结束
                boolean exists = relation.exists(new Get(column).addColumn(Bytes.toBytes("friends"), rowKey));
                if (exists){
                    relation.delete(new Delete(column).addColumn(Bytes.toBytes("friends"), rowKey));
                }
            }

        }
    }
}
