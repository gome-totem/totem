package org.z.global.evernbus;

public class TestEvent {
    protected  int message;
    public TestEvent(Integer message) {        
        this.message = message;
        System.out.println("event message:"+message);
    }
    public int getMessage() {
        return message;
    }
}
