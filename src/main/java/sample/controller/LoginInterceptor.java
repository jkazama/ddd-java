package sample.controller;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import sample.context.actor.Actor;
import sample.context.actor.Actor.ActorRoleType;
import sample.context.actor.ActorSession;

/**
 * スレッドローカルに利用者を紐付けるAOPInterceptor。
 * low: 今回は認証機能を用意していないのでダミーです。
 * 
 * @author jkazama
 */
@Aspect
@Component
public class LoginInterceptor {

    @Before("execution(* sample.controller.*Controller.*(..))")
    public void bindUser() {
        ActorSession.bind(Actor.builder()
                .id("sample")
                .name("sample")
                .roleType(ActorRoleType.USER)
                .build());
    }

    @Before("execution(* sample.controller.admin.*Controller.*(..))")
    public void bindAdmin() {
        ActorSession.bind(Actor.builder()
                .id("admin")
                .name("admin")
                .roleType(ActorRoleType.INTERNAL)
                .build());
    }

    @Before("execution(* sample.controller.system.*Controller.*(..))")
    public void bindSystem() {
        ActorSession.bind(Actor.System);
    }

    @After("execution(* sample.controller..*Controller.*(..))")
    public void unbind() {
        ActorSession.unbind();
    }

}
