package com.study.service;

import com.study.dao.StudentMapper;
import com.study.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * @author zhouhao
 * @create 2020-08-15 01:11
 */

@Transactional
@Service
@ComponentScan("com.study.dao")
public class StudentServiceImpl implements StudentServiceIF {

	
	@Autowired
	StudentMapper studentMapper;
	
	//查询所有学生
	@Override
	public List<Student> queryAllStu(){
		return studentMapper.queryAllStu();
	}
}
