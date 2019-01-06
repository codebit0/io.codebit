package io.codebit.support.bci;

import io.codebit.support.aspect.annotation.EventHandle;

public class EventHandle1 {

    @EventHandle
    public String handler() {
        return "eventhanlder1";
    }
}
