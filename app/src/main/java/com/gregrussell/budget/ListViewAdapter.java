package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 3/11/2017.
 */

public class ListViewAdapter extends BaseAdapter{

    ListDataObj ldo = new ListDataObj();

    private LayoutInflater inflater;
    Context context;


    public ListViewAdapter(Context context, ListDataObj ldo){

        Log.d("listViewAdapter", "List view adapter class has run");

        this.context = context;

        this.ldo = ldo;
        inflater = ((Activity)context).getLayoutInflater();


    }


    @Override
    public int getCount() {
        return ldo.getCategoryList().size();
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


        Log.d("listViewAdapter", "List view adapter getView method");
        View currentView = convertView;
        if(currentView == null){
            currentView = inflater.inflate(R.layout.list_item_layout,parent,false);
        }






        TextView difference = (TextView)currentView.findViewById(R.id.difference);
        TextView overUnder = (TextView)currentView.findViewById(R.id.overUnder);
        TextView categoryName = (TextView)currentView.findViewById(R.id.categoryName);
        TextView expenses = (TextView)currentView.findViewById(R.id.projectedValue);
        TextView spent = (TextView)currentView.findViewById(R.id.spentValue);
        TextView spentOrEarned = (TextView)currentView.findViewById(R.id.spent);

        //make spent text view read "earned" for income field, otherwise read "spent"
        if(position==0){
            spentOrEarned.setText("Earned - ");
        }else spentOrEarned.setText("Spent - ");


        List<String> categoryList = ldo.getCategoryList();
        List<Double> expenseList = ldo.getExpenseList();
        List<Double> spentList = ldo.getSpentList();


        //pass data from list to objects
        String catName = categoryList.get(position);
        double exp = expenseList.get(position);
        double totSpent = spentList.get(position);
        double diff = exp - totSpent;

        //formatter to convert double under 1000 to currency (only for difference text view)
        DecimalFormat lowFmt = new DecimalFormat("+$#,##0.00;-$#,##0.00");

        //formatter to convert double over 1000 to currency (only for difference text view)
        DecimalFormat highFmt = new DecimalFormat("+$#,##0.0;-$#,##0.0");

        //standard currency format
        NumberFormat fmt = NumberFormat.getCurrencyInstance();

        String fmtExp = fmt.format(exp);
        String fmtTotSpent = fmt.format(totSpent);
        String fmtDiff;
        String ovUn;

        //create shortened format of difference
        if(diff < 1000){
            fmtDiff = lowFmt.format(diff);
        }else if(diff >= 1000 && diff < 1000000 ){
            diff = diff / 1000;
            fmtDiff = highFmt.format(diff) + "K";
        }else if (diff >= 1000000 && diff < 1000000000){
            diff = diff / 1000000;
            fmtDiff = highFmt.format(diff) + "M";
        }else{
            diff = diff / 1000000000;
            fmtDiff = highFmt.format(diff) + "B";
        }


        if(diff < exp){
            ovUn = "Under";
            if(position == 0){
                difference.setTextColor(context.getResources().getColor(R.color.colorListRed));
                overUnder.setTextColor(context.getResources().getColor(R.color.colorListRed));
            }else {
                difference.setTextColor(context.getResources().getColor(R.color.colorListGreen));
                overUnder.setTextColor(context.getResources().getColor(R.color.colorListGreen));
            }

        }else if(diff == exp){
            ovUn = "Even";
            difference.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            overUnder.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }else {
            ovUn = "Over";
            if(position == 0){
                difference.setTextColor(context.getResources().getColor(R.color.colorListGreen));
                overUnder.setTextColor(context.getResources().getColor(R.color.colorListGreen));
            }else {
                difference.setTextColor(context.getResources().getColor(R.color.colorListRed));
                overUnder.setTextColor(context.getResources().getColor(R.color.colorListRed));
            }
        }

        categoryName.setText(catName);
        expenses.setText(fmtExp);
        spent.setText(fmtTotSpent);
        difference.setText(fmtDiff);
        overUnder.setText(ovUn);








        return currentView;


    }
}