package com.example.chat;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import chat.customJson.CustomJson;
import chat.model.dto.Profile;
import chat.model.entity.User;
import chat.repository.UserRepository;
import chat.service.UserService;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@SpringBootApplication
public class test {

	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
//	@Test
//	@Transactional
//	public void test(){
//		CustomJson<Object> test=new CustomJson<Object>();
//		CustomJson.Head head=new CustomJson.Head("leftchat","",CustomJson.generateCondition());
//		test.setHead(head);
//		test.setData(head);
//		System.out.println(test);
//	}
}
