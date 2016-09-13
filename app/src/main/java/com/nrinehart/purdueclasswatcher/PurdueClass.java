package com.nrinehart.purdueclasswatcher;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahrinehart on 8/29/16.
 */

public class PurdueClass extends RealmObject {

    String name;
    @PrimaryKey
    String crn;
    String course;
    String section;
    String capactiy;
    String actual;
    String remaining;

    public PurdueClass() {
    }

    public PurdueClass(String name, String crn, String course, String section, String capactiy, String actual, String remaining) {
        this.name = name;
        this.crn = crn;
        this.course = course;
        this.section = section;
        this.capactiy = capactiy;
        this.actual = actual;
        this.remaining = remaining;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getCapactiy() {
        return capactiy;
    }

    public void setCapactiy(String capactiy) {
        this.capactiy = capactiy;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getRemaining() {
        return remaining;
    }

    public void setRemaining(String remaining) {
        this.remaining = remaining;
    }
}
