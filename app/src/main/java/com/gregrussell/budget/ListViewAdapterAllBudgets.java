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
 * Created by greg on 3/25/2017.
 */

public class ListViewAdapterAllBudgets extends BaseAdapter {

    List<BudgetListItemObj> budgetListItemList = new ArrayList<BudgetListItemObj>();

    private LayoutInflater inflater;
    Context context;


    public ListViewAdapterAllBudgets(Context context, List<BudgetListItemObj> budgetListItemList){

        Log.d("listViewAdapterAll", "List view adapter class has run " + budgetListItemList.size() );

        this.context = context;

        this.budgetListItemList = budgetListItemList;
        inflater = ((Activity)context).getLayoutInflater();



    }


    @Override
    public int getCount() {
        return budgetListItemList.size();
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


        Log.d("listViewAdapterAll", "List view adapter getView method");
        View currentView = convertView;
        if(currentView == null){
            currentView = inflater.inflate(R.layout.list_item_layout,parent,false);
        }
        TextView difference = (TextView)currentView.findViewById(R.id.differenceItemLayout);
        TextView overUnder = (TextView)currentView.findViewById(R.id.overUnderItemLayout);
        TextView budgetName = (TextView)currentView.findViewById(R.id.categoryNameItemLayout);
        TextView expenses = (TextView)currentView.findViewById(R.id.projectedValueItemLayout);
        TextView spent = (TextView)currentView.findViewById(R.id.spentValueItemLayout);
        TextView spentOrEarned = (TextView)currentView.findViewById(R.id.spentItemLayout);
        TextView projected = (TextView)currentView.findViewById(R.id.projectedItemLayout);

        //make spent text view read "earned" for income field, otherwise read "spent"
        if(position==0){
            spentOrEarned.setText(context.getResources().getText(R.string.earned));
            projected.setText(context.getResources().getText(R.string.projectedExpenses));
        }else{
            spentOrEarned.setText(context.getResources().getText(R.string.spent));
            projected.setText(context.getResources().getText(R.string.projected));
        }





        //pass data from list to objects
        String budName = budgetListItemList.get(position).getBudgetName();
        double exp = budgetListItemList.get(position).getExpenses();
        double totSpent = budgetListItemList.get(position).getSpent();
        double diff;
        if(position != 0) {
            diff = exp - totSpent;
        }else diff = totSpent - exp;

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
        if(Math.abs(diff) < 1000){
            fmtDiff = lowFmt.format(diff);
        }else if(Math.abs(diff) >= 1000 && Math.abs(diff) < 1000000 ){
            diff = diff / 1000;
            fmtDiff = highFmt.format(diff) + "K";
        }else if (Math.abs(diff) >= 1000000 && Math.abs(diff) < 1000000000){
            diff = diff / 1000000;
            fmtDiff = highFmt.format(diff) + "M";
        }else{
            diff = diff / 1000000000;
            fmtDiff = highFmt.format(diff) + "B";
        }

        Log.d("listviewadapter","diff is " + diff + "exp is " + exp);
        if(totSpent < exp){
            ovUn = "Under";

            difference.setTextColor(context.getResources().getColor(R.color.colorListGreen));
            overUnder.setTextColor(context.getResources().getColor(R.color.colorListGreen));


        }else if(totSpent == exp){
            ovUn = "Even";
            difference.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            overUnder.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }else {
            ovUn = "Over";

            difference.setTextColor(context.getResources().getColor(R.color.colorListRed));
            overUnder.setTextColor(context.getResources().getColor(R.color.colorListRed));

        }

        Log.d("listviewadapter", "over under is " + ovUn);

        budgetName.setText(budName);
        expenses.setText(fmtExp);
        spent.setText(fmtTotSpent);
        difference.setText(fmtDiff);
        overUnder.setText(ovUn);








        return currentView;


    }
}