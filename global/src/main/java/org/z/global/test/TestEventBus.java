package org.z.global.test;

import org.junit.Test;

import com.google.common.eventbus.EventBus;

public class TestEventBus {
    @Test
    public void testReceiveEvent() throws Exception {
        EventBus eventBus = new EventBus("test");
        EventListener listener = new EventListener();
        eventBus.register(listener);
        eventBus.post(new TestEvent(200));
        eventBus.post(new TestEvent(300));
        eventBus.post(new TestEvent(400));
        System.out.println("LastMessage:"+listener.getLastMessage());
        ;
    }
}
