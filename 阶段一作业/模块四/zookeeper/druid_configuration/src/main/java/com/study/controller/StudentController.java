package com.study.controller;

import com.study.entity.Student;
import com.study.service.StudentServiceIF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author zhouhao
 * @create 2020-08-15 01:20
 */

@Controller
@RequestMapping("/student")
public class StudentController {

	@Autowired
	StudentServiceIF studentService;

	// 查询所有学生
	@RequestMapping("/listall")
	public String queryAllStu(Model model) {
		List<Student> stuList = studentService.queryAllStu();
		model.addAttribute("stu", stuList);
		return "index";
	}

}
