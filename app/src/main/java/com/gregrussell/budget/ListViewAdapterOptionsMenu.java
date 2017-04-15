package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by greg on 3/27/2017.
 */

public class ListViewAdapterOptionsMenu extends BaseAdapter {


    String[]optionsList;
    private LayoutInflater inflater;
    Context context;

    public ListViewAdapterOptionsMenu(Context context,  String[] optionsList){

        this.context = context;
        this.optionsList = optionsList;
        inflater = ((Activity)context).getLayoutInflater();

    }


    @Override
    public int getCount() {
        return optionsList.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //set the layout to use for each row in the list
        View currentView = convertView;
        if(currentView == null){
            currentView = inflater.inflate(R.layout.options_list_item_layout,parent,false);
        }

        Log.d("ListViewAdapterOptions", "In ListViewAdapterOptionsMenu " + optionsList.length );

        //initialize textViews
        TextView optionText = (TextView)currentView.findViewById(R.id.textViewOptionsListItem);
        optionText.setText(optionsList[position]);
        return currentView;
    }
}