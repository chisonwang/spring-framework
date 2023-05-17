package org.springframework.context.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;


//public class MyListener implements ApplicationListener<MyEvent> { 也可以直接使用泛型指定
public class MyListener implements ApplicationListener {
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof MyEvent){
			((MyEvent) event).invokeEvent();
		}
	}

	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("");
		ctx.publishEvent(new MyEvent(ctx,"aaaa"));
	}
}
