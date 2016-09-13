package com.nrinehart.purdueclasswatcher;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nrinehart.purdueclasswatcher.eventbus.EventBus;
import com.nrinehart.purdueclasswatcher.eventbus.RemoveClassResult;

import io.realm.Realm;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Created by noahrinehart on 8/29/16.
 */

public class PurdueClassRealmAdapter
        extends RealmBasedRecyclerViewAdapter<PurdueClass, PurdueClassRealmAdapter.ViewHolder> {

    public class ViewHolder extends RealmViewHolder {

        TextView nameTextView;
        TextView crnTextView;
        TextView courseTextView;
        TextView sectionTextView;
        TextView availabilityTextView;
        View v;
        PurdueClassRealmAdapter adapter;

        ViewHolder(RelativeLayout container, PurdueClassRealmAdapter adapter) {
            super(container);
            this.nameTextView = (TextView) container.findViewById(R.id.row_class_name);
            this.crnTextView = (TextView) container.findViewById(R.id.row_crn);
            this.courseTextView = (TextView) container.findViewById(R.id.row_course);
            this.sectionTextView = (TextView) container.findViewById(R.id.row_section);
            this.availabilityTextView = (TextView) container.findViewById(R.id.row_availability);
            this.v = container;
            this.adapter = adapter;
        }
    }

    public PurdueClassRealmAdapter(
            Context context,
            RealmResults<PurdueClass> realmResults,
            boolean automaticUpdate,
            boolean animateResults) {
        super(context, realmResults, automaticUpdate, animateResults);
    }

    @Override
    public PurdueClassRealmAdapter.ViewHolder onCreateRealmViewHolder(ViewGroup viewGroup, int viewType) {
        View v = inflater.inflate(R.layout.row_class, viewGroup, false);
        return new ViewHolder((RelativeLayout) v, this);
    }

    @Override
    public void onBindRealmViewHolder(final ViewHolder purdueClassRealmViewHolder, int i) {
        final PurdueClass purdueClass = realmResults.get(i);
        purdueClassRealmViewHolder.nameTextView.setText(purdueClass.getName());
        purdueClassRealmViewHolder.crnTextView.setText(purdueClass.getCrn());
        purdueClassRealmViewHolder.courseTextView.setText(purdueClass.getCourse());
        purdueClassRealmViewHolder.sectionTextView.setText(purdueClass.getSection());
        purdueClassRealmViewHolder.availabilityTextView.setText(purdueClass.getActual() + "/" + purdueClass.getCapactiy());
        if (purdueClass.getRemaining().equals("0")) {
            purdueClassRealmViewHolder.availabilityTextView.setTextColor(Color.RED);
        } else {
            purdueClassRealmViewHolder.availabilityTextView.setTextColor(Color.GREEN);
        }
        purdueClassRealmViewHolder.v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(purdueClassRealmViewHolder.v.getContext())
                        .setTitle("Delete Class?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EventBus.getBus().post(new RemoveClassResult(purdueClassRealmViewHolder.getAdapterPosition()));
                            }
                        })
                        .show();
                return true;
            }
        });
    }
}
