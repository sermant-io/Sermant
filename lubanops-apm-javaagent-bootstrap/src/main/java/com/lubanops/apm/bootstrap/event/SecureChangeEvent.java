package com.lubanops.apm.bootstrap.event;

import com.lubanops.apm.bootstrap.enums.EventType;

/**
 * @author
 * @date 2021/2/3 10:26
 */
public class SecureChangeEvent implements ApmEvent {

    private boolean previous;

    private boolean current;

    public SecureChangeEvent(boolean previous, boolean current) {
        this.previous = previous;
        this.current = current;
    }

    @Override
    public EventType getEventType() {
        return EventType.CONFIG_CHANGE;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isPrevious() {
        return previous;
    }

    public void setPrevious(boolean previous) {
        this.previous = previous;
    }
}
