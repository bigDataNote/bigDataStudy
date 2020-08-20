package com.study.config;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

/**
 * @author zhouhao
 * @create 2020-08-15 00:47
 */

public class ZkStrSerializer implements ZkSerializer {

    //序列化，数据--》byte[]
    public byte[] serialize(Object o) throws ZkMarshallingError {
        return String.valueOf(o).getBytes();
    }

    //反序列化，byte[]--->数据
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        return new String(bytes);
    }
}
