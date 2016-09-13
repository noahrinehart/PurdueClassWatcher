package com.nrinehart.purdueclasswatcher;

import android.os.AsyncTask;
import android.util.Log;

import com.nrinehart.purdueclasswatcher.eventbus.ClassInfoResultEvent;
import com.nrinehart.purdueclasswatcher.eventbus.EventBus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by noahrinehart on 8/29/16.
 */

public class DownloadClassInfoAsyncTask extends AsyncTask<String, Void, PurdueClass> {

    @Override
    protected PurdueClass doInBackground(String... params) {
        try {
            Document doc = Jsoup.connect("https://selfservice.mypurdue.purdue.edu/prod/bwckschd.p_disp_detail_sched?term_in=201710&crn_in=" + params[0]).get();
            Element errorchecking = doc.select(".errortext").first();
            if (errorchecking != null)
                return null;

            String[] info = doc.select(".ddlabel").get(0).ownText().split(" - ");
            String name = info[0];
            String crn = info[1];
            String course = info[2];
            String section = info[3];
            String capacity = doc.select("td.dddefault").get(1).ownText();
            String actual = doc.select("td.dddefault").get(2).ownText();
            String remaining = doc.select("td.dddefault").get(3).ownText();

            PurdueClass purdueClass = new PurdueClass(name, crn, course, section, capacity, actual, remaining);
            Log.d("Async", "Class found! " + purdueClass.getCourse());
            return purdueClass;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(PurdueClass purdueClass) {
        super.onPostExecute(purdueClass);
        EventBus.getBus().post(new ClassInfoResultEvent(purdueClass));
    }
}
