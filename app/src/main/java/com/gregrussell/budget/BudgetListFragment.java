package com.gregrussell.budget;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 3/24/2017.
 */

public class BudgetListFragment extends Fragment{

    DataBaseHelperCategory myDBHelper;
    Context context;
    ViewGroup rootView;
    FloatingActionButton addCategoryButton;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        context = getActivity();
        rootView = (ViewGroup) inflater.inflate(R.layout.all_budgets_fragment_layout, container, false);

        addCategoryButton = (FloatingActionButton)rootView.findViewById(R.id.addAllBudgetsFragment);
        addCategoryButton.setVisibility(View.INVISIBLE);

        AsyncLoadList loadListTask = new AsyncLoadList();
        loadListTask.execute();

        Log.d("BudgetListFragment", "Fragment loaded");
        return rootView;
    }

    private class AsyncLoadList extends AsyncTask<Void,Void,Boolean> {


        ListViewAdapterAllBudgets adapter;
        ListView listView;
        List<BudgetListItemObj> budgetListItemList = new ArrayList<BudgetListItemObj>();

        @Override
        protected void onPreExecute(){

            View listLoadingPanel = rootView.findViewById(R.id.listLoadingPanelAllBudgetsFragment);
            listLoadingPanel.setVisibility(View.VISIBLE);
            addCategoryButton.setVisibility(View.INVISIBLE);
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            Log.d("budgetList", "do in background");
            PopulateList();
            return null;
        }



        @Override
        protected void onPostExecute(Boolean result){
            //progress bar is visible by default. Turn invisible once loading is complete
            View listLoadingPanel = rootView.findViewById(R.id.listLoadingPanelAllBudgetsFragment);
            listLoadingPanel.setVisibility(View.INVISIBLE);
            addCategoryButton.setVisibility(View.VISIBLE);

            Log.d("listView budgetlist", "listViewAdapter set");

            listView.setAdapter(adapter);

            //set listener for list item click
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick budget list", "click " + String.valueOf(position));
                    //On click change current budget
                    CurrentBudgetFragment.CURRENT_BUDGET = budgetListItemList.get(position).getBudgetID();
                    AsyncCurrentBudgetLoadHeader loadHeader = new AsyncCurrentBudgetLoadHeader();
                    loadHeader.execute();
                    AsyncCurrentBudgetLoadList loadList = new AsyncCurrentBudgetLoadList();
                    loadList.execute();
                }
            });
        }

        private void PopulateList(){

            myDBHelper = new DataBaseHelperCategory(context);
            try {
                myDBHelper.createDataBase();
            } catch (IOException ioe) {
                throw new Error("Unable to create database");
            }
            try {
                myDBHelper.openDataBase();
            } catch (SQLException sqle) {
                throw sqle;
            }

            Log.d("budgetlist lvadapter", "PopulateList method is running yay");
            budgetListItemList = myDBHelper.getAllBudgetsList();
            Log.d("budgetlist lvadapter", "getallbudgets " + budgetListItemList.size());


            //set listview adapter
            View view = rootView.findViewById(R.id.listViewFrameAllBudgetsFragment);
            listView = (ListView)view.findViewById(R.id.listViewAllBudgetsFragment);
            adapter = new ListViewAdapterAllBudgets(context,budgetListItemList);
        }
    }



    private class AsyncCurrentBudgetLoadHeader extends AsyncTask<Void,Void,String[]>{

        double diff;
        double allExp;
        double totSpent;

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup currentBudgetRootView = (ViewGroup)inflater.inflate(R.layout.activity_main,null);

        View containerLayout = currentBudgetRootView.findViewById(R.id.container);
        TextView difference = (TextView)currentBudgetRootView.findViewById(R.id.difference);
        TextView overUnder = (TextView)currentBudgetRootView.findViewById(R.id.overUnder);
        TextView budgetName = (TextView)currentBudgetRootView.findViewById(R.id.budgetName);
        TextView projectedExpenses = (TextView)currentBudgetRootView.findViewById(R.id.projectedValue);
        TextView spent = (TextView)currentBudgetRootView.findViewById(R.id.spentValue);



        @Override
        protected String[] doInBackground(Void... params) {

            return populateHeader();
        }



        @Override
        protected void onPostExecute(String[] result){

            //set text from string[] result that is returned by doInBackground

            String ovUn;



            //round because double doesn't know how to math
            double roundTotSpent = Math.round(totSpent *100.0)/100.0;
            double roundAllExp = Math.round(allExp *100.0)/100.0;

            if(roundTotSpent < roundAllExp){
                ovUn = "Under";
                containerLayout.setBackgroundColor(getResources().getColor(R.color.colorListGreen));
            }else if(roundTotSpent == roundAllExp){
                ovUn = "Even";
                containerLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }else {
                ovUn = "Over";
                containerLayout.setBackgroundColor(getResources().getColor(R.color.colorListRed));
            }




            //set text of textViews
            budgetName.setText(CurrentBudgetFragment.BUDGET_NAME);
            projectedExpenses.setText(result[0]);
            spent.setText(result[1]);
            difference.setText(result[2]);
            overUnder.setText(ovUn);




            //progress bar is visible by default. Turn invisible once loading is complete
            View headerLoadingPanel = currentBudgetRootView.findViewById(R.id.headerLoadingPanel);
            headerLoadingPanel.setVisibility(View.INVISIBLE);


        }


        private String[] populateHeader(){

            Log.d("listDataObj", "Entered PopulateHeader, current budget is " + CurrentBudgetFragment.BUDGET_NAME);
            ListDataObj listData = myDBHelper.createListData(CurrentBudgetFragment.CURRENT_BUDGET);


            //Debug logs to check that all data is in the list

            Log.d("listDataObj", "budget name: " + listData.getBudgetName());
            Log.d("listDataObj", "all expenses:" + listData.getAllExpenses());
            Log.d("listDataObj", "total spent: " + listData.getTotalSpent());
            Log.d("listDataObj", "income category check: " + listData.getCategoryList().get(0));
            Log.d("listDataObj", "projected income: " + listData.getExpenseList().get(0));
            Log.d("listDataObj", "earned income: " + listData.getSpentList().get(0));

            for(int i = 1; i < listData.getCategoryList().size(); i++){
                Log.d("listDataObj", "categories after income are: " + i + ". " + listData.getCategoryList().get(i));
            }
            for(int i = 1; i < listData.getCategoryList().size(); i++){
                Log.d("listDataObj", "estimated expenses after income are: " + i + ". " + listData.getExpenseList().get(i));
            }
            for(int i = 1; i < listData.getCategoryList().size(); i++){
                Log.d("listDataObj", "spent for each category after income are: " + i + ". " + listData.getSpentList().get(i));
            }

            //pass data from list to objects
            CurrentBudgetFragment.BUDGET_NAME = listData.getBudgetName();
            allExp = listData.getAllExpenses();
            totSpent = listData.getTotalSpent();
            Log.d("populateheader", "listdata spent is " + listData.getTotalSpent());
            diff = totSpent - allExp;






            //formatter to convert double under 1000 to currency (only for difference text view)
            DecimalFormat lowFmt = new DecimalFormat("+$#,##0.00;-$#,##0.00");

            //formatter to convert double over 1000 to currency (only for difference text view)
            DecimalFormat highFmt = new DecimalFormat("+$#,##0.0;-$#,##0.0");

            //standard currency format
            NumberFormat fmt = NumberFormat.getCurrencyInstance();

            String fmtAllExp = fmt.format(allExp);
            String fmtTotSpent = fmt.format(totSpent);
            String fmtDiff;



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





            //String Array to return
            String[] results = {fmtAllExp,fmtTotSpent,fmtDiff};
            return results;



        }

    }


    private class AsyncCurrentBudgetLoadList extends AsyncTask<Void,Void,Boolean>{


        ListViewAdapter adapter;
        ListView listView;
        ListDataObj listData;

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup currentBudgetRootView = (ViewGroup)inflater.inflate(R.layout.activity_main,null);

        @Override
        protected void onPreExecute(){

            View listLoadingPanel = currentBudgetRootView.findViewById(R.id.listLoadingPanel);
            listLoadingPanel.setVisibility(View.VISIBLE);
            addCategoryButton.setVisibility(View.INVISIBLE);
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            PopulateList();
            return null;
        }



        @Override
        protected void onPostExecute(Boolean result){
            //progress bar is visible by default. Turn invisible once loading is complete
            View listLoadingPanel = currentBudgetRootView.findViewById(R.id.listLoadingPanel);
            listLoadingPanel.setVisibility(View.INVISIBLE);
            addCategoryButton.setVisibility(View.VISIBLE);

            Log.d("listView", "listViewAdapter set");

            listView.setAdapter(adapter);

            //set listener for list item click
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick", "click " + String.valueOf(position));
                    //use position to get the category from listData
                    int category = listData.getCategoryIDList().get(position);

                    //must check if first position, income category handled differently
                    if(position == 0){
                        //income category has been selected
                        Intent intent = new Intent(context, DisplayIncome.class);
                        startActivity(intent);

                    }else{
                        //an expense category has been selected
                        Intent intent = new Intent(context, DisplayCategory.class);
                        intent.putExtra(CurrentBudgetFragment.EXTRA_MESSAGE_TO_DISPLAY_CATEGORY, category);
                        startActivity(intent);
                    }


                }
            });




        }

        private void PopulateList(){

            Log.d("listViewAdapter", "PopulateList method is running yay");
            listData = myDBHelper.createListData(CurrentBudgetFragment.CURRENT_BUDGET);


            CategoryObj c = new CategoryObj(100, "ReNTs", 0);
            Log.d("uniqueCategory",  String.valueOf(myDBHelper.checkCategoryName(c)));

            //set listview adapter
            View view = currentBudgetRootView.findViewById(R.id.listViewFrame);
            listView = (ListView)view.findViewById(R.id.listView);
            adapter = new ListViewAdapter(context,listData);
        }
    }
}
