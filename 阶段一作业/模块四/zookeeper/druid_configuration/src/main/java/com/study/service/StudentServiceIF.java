package com.study.service;

import com.study.entity.Student;

import java.util.List;
/**
 * @author zhouhao
 * @create 2020-08-15 01:11
 */
public interface StudentServiceIF {

	//查询所有学生
	List<Student> queryAllStu();

}