package sample.controller;

import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sample.context.actor.*;
import sample.context.actor.Actor.ActorRoleType;

/**
 * スレッドローカルに利用者を紐付けるAOPInterceptor。
 * low: 今回は認証機能を用意していないのでダミーです。
 * 
 * @author jkazama
 */
@Aspect
@Component
public class LoginInterceptor {

	@Autowired
	private ActorSession session;
	
	@Before("execution(* sample.controller.*Controller.*(..))")
	public void bindUser() {
		session.bind(new Actor("sample", ActorRoleType.USER));
	}
	
	@Before("execution(* sample.controller.admin.*Controller.*(..))")
	public void bindAdmin() {
		session.bind(new Actor("admin", ActorRoleType.INTERNAL));
	}
	
	@Before("execution(* sample.controller.system.*Controller.*(..))")
	public void bindSystem() {
		session.bind(Actor.System);
	}
	
	@After("execution(* sample.controller..*Controller.*(..))")
	public void unbind() {
		session.unbind();
	}
	
}
