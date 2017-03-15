package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class MainActivity extends Activity {


    DataBaseHelperCategory myDBHelper;
    public static int CURRENT_BUDGET;
    public static final String EXTRA_MESSAGE_TO_DISPLAY_CATEGORY = "com.gregrussell.budget.EXTRA_INTENT_TO_DISPLAY_CATEGORY";
    View container;
    TextView difference;
    TextView overUnder;
    TextView budgetName;
    TextView projectedExpenses;
    TextView spent;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        //initializing views
        container = findViewById(R.id.container);
        difference = (TextView)container.findViewById(R.id.difference);
        overUnder = (TextView)container.findViewById(R.id.overUnder);
        budgetName = (TextView)container.findViewById(R.id.budgetName);
        projectedExpenses = (TextView)container.findViewById(R.id.projectedValue);
        spent = (TextView)container.findViewById(R.id.spentValue);
        View frame = (View)findViewById(R.id.listViewFrame);
        listView = frame.findViewById(R.id.listView);



        //setting color for header progress bar
        ProgressBar headerProgress = (ProgressBar) findViewById(R.id.headerProgress);
        headerProgress.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, getResources().getColor(R.color.colorPrimary)));

        //setting color for list progress bar
        ProgressBar listProgress = (ProgressBar) findViewById(R.id.listProgress);
        listProgress.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, getResources().getColor(R.color.colorPrimary)));

        //starting each task on a background thread
        AsyncLoadBudget loadBudget = new AsyncLoadBudget();
        loadBudget.execute();

        AsyncLoadHeader loadHeader = new AsyncLoadHeader();
        loadHeader.execute();

        AsyncLoadList loadList = new AsyncLoadList();
        loadList.execute();


        /*
        List<String> categories = myDBHelper.getAllCategories();

        for (int i =0; i < categories.size(); i++){
            Log.d("categoriesList", categories.get(i));
        }

        //myDBHelper.addSpending(3,"March 2017", 5, "Cellphone", 40.50, "Cellphone bill");


        //get a list of all the rows with the specified budget ID and tell the size of the list
        List<SpendingObj> list = myDBHelper.getSpendings(2);

        try{
            Log.d("list size", String.valueOf(list.size()));
        }
        catch (Exception e){
            Log.d("list size", "size is null");
        }*/





    }




    private class AsyncLoadBudget extends AsyncTask<Void,Void, Boolean>{




        @Override
        protected Boolean doInBackground(Void... params) {

            LoadBudget();
            //because you have to return something to onPostExecute
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result){


        }


        private void LoadBudget(){

            Log.d("LoadBudget", "made it to load budget");
            //compare most recent spending timestamp and earning timestamp to find most recent budget
            // to load

            int recentBudget;

            myDBHelper = new DataBaseHelperCategory(MainActivity.this);

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

            Timestamp spendingTime = myDBHelper.getSpendingTimestamp();
            Timestamp earningTime = myDBHelper.getEarningTimestamp();

            if(spendingTime != null && earningTime != null) {

                if (spendingTime.after(earningTime)) {

                    //use spending
                    recentBudget = myDBHelper.getMostRecentSpending();
                    CURRENT_BUDGET = recentBudget;
                    myDBHelper.close();




                } else {

                    //use earning
                    recentBudget = myDBHelper.getMostRecentEarning();
                    CURRENT_BUDGET = recentBudget;
                    myDBHelper.close();


                }

            }else if(spendingTime == null && earningTime == null){

                recentBudget = myDBHelper.getMostRecentBudget();
                if(recentBudget == -1){
                    CreateBudget();
                }else {
                    CURRENT_BUDGET = recentBudget;
                    myDBHelper.close();



                }


            }else if(spendingTime == null) {
                Log.d("Most Recent STimestamp", "null");
                Log.d("Most Recent ETimestamp", earningTime.toString());
                //use earning

                recentBudget = myDBHelper.getMostRecentEarning();
                CURRENT_BUDGET = recentBudget;
                myDBHelper.close();


            }
            else{
                //use spending
                recentBudget = myDBHelper.getMostRecentSpending();
                CURRENT_BUDGET = recentBudget;
                myDBHelper.close();



                Log.d("Most Recent Timestamp", spendingTime.toString());
            }
        }

        private void CreateBudget(){


            Log.d("CreateBudget", "Entered Create Budget");
            myDBHelper.addBudget("March 2017");
            LoadBudget();




        }



    }


    private class AsyncLoadHeader extends AsyncTask<Void,Void,String[]>{

        double diff;
        double allExp;


        @Override
        protected String[] doInBackground(Void... params) {

            return PopulateHeader();
        }



        @Override
        protected void onPostExecute(String[] result){

            //set text from string[] result that is returned by doInBackground

            String ovUn;

            if(diff < allExp){
                ovUn = "Under";
                container.setBackgroundColor(getResources().getColor(R.color.colorListGreen));
            }else if(diff == allExp){
                ovUn = "Even";
                container.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }else {
                ovUn = "Over";
                container.setBackgroundColor(getResources().getColor(R.color.colorListRed));
            }


            //set text of textViews
            budgetName.setText(result[0]);
            projectedExpenses.setText(result[1]);
            spent.setText(result[2]);
            difference.setText(result[3]);
            overUnder.setText(ovUn);




            //progress bar is visible by default. Turn invisible once loading is complete
            View headerLoadingPanel = findViewById(R.id.headerLoadingPanel);
            headerLoadingPanel.setVisibility(View.INVISIBLE);


        }


        private String[] PopulateHeader(){

            Log.d("listDataObj", "Entered PopulateHeader, current budget is " + CURRENT_BUDGET);


            //open the database
            myDBHelper = new DataBaseHelperCategory(MainActivity.this);

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


            ListDataObj listData = myDBHelper.createListData(CURRENT_BUDGET);
            myDBHelper.close();

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
            String budName = listData.getBudgetName();
            allExp = listData.getAllExpenses();
            double totSpent = listData.getTotalSpent();
            diff = allExp - totSpent;






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



            //String Array to return
            String[] results = {budName,fmtAllExp,fmtTotSpent,fmtDiff};
            return results;



        }

    }


    private class AsyncLoadList extends AsyncTask<Void,Void,Boolean>{


        ListViewAdapter adapter;
        ListView listView;
        ListDataObj listData;


        @Override
        protected Boolean doInBackground(Void... params) {

            PopulateList();
            return null;
        }



        @Override
        protected void onPostExecute(Boolean result){
            //progress bar is visible by default. Turn invisible once loading is complete
            View listLoadingPanel = findViewById(R.id.listLoadingPanel);
            listLoadingPanel.setVisibility(View.INVISIBLE);

            Log.d("listView", "listViewAdapter set");



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

                    }else{
                        //an expense category has been selected
                        Intent intent = new Intent(MainActivity.this, DisplayCategory.class);
                        intent.putExtra(EXTRA_MESSAGE_TO_DISPLAY_CATEGORY, category);
                        startActivity(intent);
                    }


                }
            });




        }

        private void PopulateList(){

            Log.d("listViewAdapter", "PopulateList method is running yay");

            myDBHelper = new DataBaseHelperCategory(MainActivity.this);

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

            listData = myDBHelper.createListData(CURRENT_BUDGET);
            myDBHelper.close();



            //set listview adapter
            adapter = new ListViewAdapter(MainActivity.this,listData);
            listView.setAdapter(adapter);



        }


    }




}
