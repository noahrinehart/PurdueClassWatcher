package com.nrinehart.purdueclasswatcher.eventbus;

import com.nrinehart.purdueclasswatcher.PurdueClass;

/**
 * Created by noahrinehart on 8/29/16.
 */

public class ClassInfoResultEvent {

    private PurdueClass purdueClass;

    public ClassInfoResultEvent(PurdueClass purdueClass) {
        this.purdueClass = purdueClass;
    }

    public PurdueClass getPurdueClass() {
        return purdueClass;
    }

}
