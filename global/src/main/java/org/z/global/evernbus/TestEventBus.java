package org.z.global.evernbus;

import java.util.concurrent.Executors;

import org.junit.Test;

import com.google.common.eventbus.AsyncEventBus;

public class TestEventBus {
    @Test
    public void testReceiveEvent() throws Exception {
    	 AsyncEventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(3));
        EventListener listener = new EventListener();
        eventBus.register(listener);
        eventBus.post(new TestEvent(200));
        eventBus.post(new TestEvent(300));
        eventBus.post(new TestEvent(400));
        System.out.println("LastMessage:"+listener.getLastMessage());
        ;
    }
}
