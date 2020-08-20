package com.study.dao;

import com.study.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zhouhao
 * @create 2020-08-15 01:15
 */

@Mapper
public interface StudentMapper {
	
public List<Student> queryAllStu();

	
}
