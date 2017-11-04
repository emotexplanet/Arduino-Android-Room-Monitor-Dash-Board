package com.startinnovationhub.emmanuel.roommonitordashboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Emmanuel on 22/09/2017.
 */

public class DetailListAdapter extends ArrayAdapter<Records> implements Filterable {
    private Context context;
    private ArrayList<Records> recordsListOriginal = null;
    private ArrayList<Records> recordsFiltered = null;
    private HymnFilter HFilter;

    public DetailListAdapter(Context context, int resource, ArrayList<Records> recordsList) {
        super(context, resource, recordsList);
        this.context = context;
        this.recordsListOriginal = recordsList;
        this.recordsFiltered = recordsList;
    }


    @Override
    public int getCount() {
        return recordsFiltered.size();
    }

    @Override
    public Records getItem(int position) {
        return recordsFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder;
        if (view == null) {

            view = layoutInflater.inflate(R.layout.detail_layout, null);
            viewHolder = new ViewHolder();

            viewHolder.temperature = (TextView) view.findViewById(R.id.temp_data);
            viewHolder.humidity = (TextView) view.findViewById(R.id.hum_data);
            viewHolder.polutionLevel = (TextView) view.findViewById(R.id.polu_data);
            viewHolder.status = (TextView) view.findViewById(R.id.status_data);
            viewHolder.dateTime = (TextView) view.findViewById(R.id.date);

            //Bind Records efficiently with the holder
            view.setTag(viewHolder);
        } else {
            //get the vViewHolder
            viewHolder = (ViewHolder) view.getTag();


        }
        viewHolder.dateTime.setText(recordsFiltered.get(position).getDate());
        viewHolder.temperature.setText(recordsFiltered.get(position).getTemperature());
        viewHolder.humidity.setText(recordsFiltered.get(position).getHumidity());
        viewHolder.polutionLevel.setText(recordsFiltered.get(position).getPolutionLevel());
        viewHolder.status.setText(recordsFiltered.get(position).getStatus());


        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if (HFilter == null) {
            HFilter = new HymnFilter();
        }
        return HFilter;
    }


    private class HymnFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                results.values = recordsListOriginal;
                results.count = recordsListOriginal.size();
            } else {
                ArrayList<Records> recordsList = new ArrayList<Records>();
                for (int i = 0; i < recordsListOriginal.size(); i++) {
                    /*if (recordsListOriginal.get(i).getHymnNumber().contains(filterString) ||
                            hymnListOriginal.get(i).getSong().toLowerCase().contains(filterString)) {

                        Hymns hymn = new Hymns(i, hymnListOriginal.get(i).getHymnNumber(),
                                hymnListOriginal.get(i).getSongTitle(), hymnListOriginal.get(i).getSong());
                        HymnList.add(hymn);
                    }*/

                }
                results.values = recordsList;
                results.count = recordsList.size();
            }


            return results;
        }

        @SuppressWarnings("uncheck")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            recordsFiltered = (ArrayList<Records>) results.values;
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        private TextView dateTime;
        private TextView temperature;
        private TextView humidity;
        private TextView polutionLevel;
        private TextView status;
    }

}
