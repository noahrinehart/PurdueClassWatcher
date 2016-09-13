package com.nrinehart.purdueclasswatcher.eventbus;

import com.nrinehart.purdueclasswatcher.PurdueClass;

/**
 * Created by noahrinehart on 8/29/16.
 */

public class RemoveClassResult {

    private int position;

    public RemoveClassResult(int position) {
        this.position = position;
    }

    public int getPurdueClassPosition() {
        return position;
    }
}
