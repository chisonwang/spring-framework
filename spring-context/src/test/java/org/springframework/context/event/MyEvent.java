package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;

public class MyEvent extends ApplicationEvent {

	private Object object;

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public MyEvent(Object source, Object object) {
		super(source);
		this.object=object;

	}

	public void invokeEvent(){
		System.out.println(getObject().toString());
		System.out.println("user custom event");
	}
}
