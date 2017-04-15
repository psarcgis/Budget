package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by greg on 3/18/2017.
 */

public class ListViewAdapterSpending extends BaseAdapter {

    List<SpendingObj> spendingObjList = new ArrayList<SpendingObj>();
    private LayoutInflater inflater;
    Context context;
    Date previousDate;

    public ListViewAdapterSpending(Context context, List<SpendingObj> spendingObjList){

        this.context = context;
        this.spendingObjList = spendingObjList;
        inflater = ((Activity)context).getLayoutInflater();

    }


    @Override
    public int getCount() {
        return spendingObjList.size();
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
            currentView = inflater.inflate(R.layout.spending_list_item_layout,parent,false);
        }

        Log.d("ListViewAdapterSpending", "In ListViewAdapterSpending");

        //initialize textViews
        TextView dateTextView = (TextView)currentView.findViewById(R.id.dateSpendingListItemLayout);
        TextView spentTextView = (TextView)currentView.findViewById(R.id.spentSpendingListItemLayout);
        TextView descriptionTextView = (TextView)currentView.findViewById(R.id.descriptionSpendingListItemLayout);

        //date formatter
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        //currency formatter
        NumberFormat fmt = NumberFormat.getCurrencyInstance();


        double spent = spendingObjList.get(position).getSpent();
        String description = spendingObjList.get(position).getDescription();

        Date currentDate = spendingObjList.get(position).getDate();

        if(previousDate != null && dateFormat.format(currentDate).equals(dateFormat.format(previousDate))&& position != 0){
            dateTextView.setVisibility(View.GONE);
        }else{
            previousDate = currentDate;
            dateTextView.setText(dateFormat.format(currentDate));
        }

        spentTextView.setText(fmt.format(spent));
        descriptionTextView.setText(description);


        return currentView;













    }



}
