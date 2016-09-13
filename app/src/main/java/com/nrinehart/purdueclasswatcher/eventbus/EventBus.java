package com.nrinehart.purdueclasswatcher.eventbus;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by noahrinehart on 8/29/16.
 */

public class EventBus {

    private static Bus otto;

    public static Bus getBus() {
        if(otto == null) {
            otto = new Bus(ThreadEnforcer.ANY);
        }
        return otto;
    }

}
