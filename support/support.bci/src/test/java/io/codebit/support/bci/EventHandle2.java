package io.codebit.support.bci;

import io.codebit.support.aspect.annotation.EventHandle;

public class EventHandle2 {

    @EventHandle
    public static String handler() {
        return "eventhanlder2";
    }
}
