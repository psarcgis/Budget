package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

//Paul is awesome
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
    FloatingActionButton addCategoryButton;
    public static String BUDGET_NAME;
    List<CategoryObj> unusedCategoryList = new ArrayList<CategoryObj>();

    @Override
    protected void onResume(){

        super.onResume();
        AsyncLoadHeader loadHeader = new AsyncLoadHeader();
        loadHeader.execute();

        AsyncLoadList loadList = new AsyncLoadList();
        loadList.execute();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);







        //initializing views
        container = findViewById(R.id.container);
        difference = (TextView)findViewById(R.id.difference);
        overUnder = (TextView)findViewById(R.id.overUnder);
        budgetName = (TextView)findViewById(R.id.budgetName);
        projectedExpenses = (TextView)findViewById(R.id.projectedValue);
        spent = (TextView)findViewById(R.id.spentValue);
        addCategoryButton = (FloatingActionButton)findViewById(R.id.addMainActivity);
        addCategoryButton.setVisibility(View.INVISIBLE);

        //setting color for header progress bar
        ProgressBar headerProgress = (ProgressBar) findViewById(R.id.headerProgress);
        headerProgress.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, getResources().getColor(R.color.colorListNeutral)));

        //setting color for list progress bar
        ProgressBar listProgress = (ProgressBar) findViewById(R.id.listProgress);
        listProgress.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, getResources().getColor(R.color.colorListNeutral)));

        //starting each task on a background thread
        AsyncLoadBudget loadBudget = new AsyncLoadBudget();
        loadBudget.execute();

        AsyncLoadHeader loadHeader = new AsyncLoadHeader();
        loadHeader.execute();

        AsyncLoadList loadList = new AsyncLoadList();
        loadList.execute();

        //onClickListener for addCategoryButton
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });


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

            //myDBHelper.addCategory("Dating", false);
            unusedCategoryList = myDBHelper.getUnusedCategories(CURRENT_BUDGET);
            for(int i =0; i < unusedCategoryList.size(); i++){
                Log.d("allCategoriesNotUsed", unusedCategoryList.get(i).getCategoryName());
            }

            Timestamp spendingTime = myDBHelper.getSpendingTimestamp();
            Timestamp earningTime = myDBHelper.getEarningTimestamp();

            if(spendingTime != null && earningTime != null) {

                if (spendingTime.after(earningTime)) {

                    //use spending
                    recentBudget = myDBHelper.getMostRecentSpending();
                    CURRENT_BUDGET = recentBudget;





                } else {

                    //use earning
                    recentBudget = myDBHelper.getMostRecentEarning();
                    CURRENT_BUDGET = recentBudget;



                }

            }else if(spendingTime == null && earningTime == null){

                recentBudget = myDBHelper.getMostRecentBudget();
                if(recentBudget == -1){
                    CreateBudget();
                }else {
                    CURRENT_BUDGET = recentBudget;




                }


            }else if(spendingTime == null) {
                Log.d("Most Recent STimestamp", "null");
                Log.d("Most Recent ETimestamp", earningTime.toString());
                //use earning

                recentBudget = myDBHelper.getMostRecentEarning();
                CURRENT_BUDGET = recentBudget;



            }
            else{
                //use spending
                recentBudget = myDBHelper.getMostRecentSpending();
                CURRENT_BUDGET = recentBudget;




                Log.d("Most Recent Timestamp", spendingTime.toString());
            }
        }

        private void CreateBudget(){


            Log.d("CreateBudget", "Entered Create Budget");
            //myDBHelper.addBudget("March 2017");
            LoadBudget();




        }



    }


    private class AsyncLoadHeader extends AsyncTask<Void,Void,String[]>{

        double diff;
        double allExp;
        double totSpent;


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
                container.setBackgroundColor(getResources().getColor(R.color.colorListGreen));
            }else if(roundTotSpent == roundAllExp){
                ovUn = "Even";
                container.setBackgroundColor(getResources().getColor(R.color.colorListNeutral));
            }else {
                ovUn = "Over";
                container.setBackgroundColor(getResources().getColor(R.color.colorListRed));
            }




            //set text of textViews
            budgetName.setText(BUDGET_NAME);
            projectedExpenses.setText(result[0]);
            spent.setText(result[1]);
            difference.setText(result[2]);
            overUnder.setText(ovUn);




            //progress bar is visible by default. Turn invisible once loading is complete
            View headerLoadingPanel = findViewById(R.id.headerLoadingPanel);
            headerLoadingPanel.setVisibility(View.INVISIBLE);


        }


        private String[] populateHeader(){

            Log.d("listDataObj", "Entered PopulateHeader, current budget is " + CURRENT_BUDGET);
            ListDataObj listData = myDBHelper.createListData(CURRENT_BUDGET);


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
            BUDGET_NAME = listData.getBudgetName();
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


    private class AsyncLoadList extends AsyncTask<Void,Void,Boolean>{


        ListViewAdapter adapter;
        ListView listView;
        ListDataObj listData;

        @Override
        protected void onPreExecute(){

            View listLoadingPanel = findViewById(R.id.listLoadingPanel);
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
            View listLoadingPanel = findViewById(R.id.listLoadingPanel);
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
                        Intent intent = new Intent(MainActivity.this, DisplayIncome.class);
                        startActivity(intent);

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
            listData = myDBHelper.createListData(CURRENT_BUDGET);


            CategoryObj c = new CategoryObj(100, "ReNTs", 0);
            Log.d("uniqueCategory",  String.valueOf(myDBHelper.checkCategoryName(c)));

            //set listview adapter
            View view = findViewById(R.id.listViewFrame);
            listView = (ListView)view.findViewById(R.id.listView);
            adapter = new ListViewAdapter(MainActivity.this,listData);
        }
    }

    private void addCategory(){

        /**Bring up dialog x
         * allow user to pick from already created category that's not being used x
         * or
         * name new category - MUST ENTER A NAME x
         * decide if category will become a default - not default by default x
         * expense $0.00 by default
         *
         */

        LayoutInflater inflater = getLayoutInflater();
        final View addCategoryDialog = inflater.inflate(R.layout.add_category_dialog_layout,null);
        final CheckBox checkBox = (CheckBox)addCategoryDialog.findViewById(R.id.checkBoxAddCategoryDialog);
        final RadioButton radioUseExisting = (RadioButton)addCategoryDialog.findViewById(R.id.radioUseExistingAddCategoryDialog);
        final RadioButton radioAddNew = (RadioButton)addCategoryDialog.findViewById(R.id.radioAddNewAddCategoryDialog);
        final Spinner categorySpinner = (Spinner)addCategoryDialog.findViewById(R.id.spinnerAddCategoryDialog);
        final EditText categoryNameEditText = (EditText)addCategoryDialog.findViewById(R.id.editTextNameAddCategoryDialog);

        //create a dialog box to add new category
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        //set the layout the dialog uses
        alertDialogBuilder.setView(addCategoryDialog);

        //set up dialog
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if(!radioAddNew.isChecked() && !radioUseExisting.isChecked()){
                                    addCategory();
                                }else
                                if(String.valueOf(categoryNameEditText.getText()).equals("")){
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Invalid Category Name")
                                            .setMessage("Must enter a name for the new category.")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addCategory();
                                                }
                                            })
                                            .show();
                                }else{
                                    if(radioAddNew.isChecked()) {
                                        CategoryObj categoryObj = new CategoryObj();
                                        int isDefault;
                                        if (checkBox.isChecked()) {
                                            isDefault = 1;
                                        } else isDefault = 0;
                                        categoryObj.setCategoryName(String.valueOf(categoryNameEditText.getText()));
                                        categoryObj.setDefaultCategory(isDefault);
                                        AsyncAddNewCategory addNewCategoryTask = new AsyncAddNewCategory();
                                        addNewCategoryTask.execute(categoryObj);
                                    }else{
                                        //AsyncAddExistingCategory
                                    }
                                }
                            }


                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        //disable spinner and existing radio button if no existing categories, check radioAddNew
        if(unusedCategoryList.size() == 0){
            radioUseExisting.setEnabled(false);
            categorySpinner.setEnabled(false);
            radioAddNew.setChecked(true);
        }

        //setting what happens on click of radio buttons, spinner, and edit text
        radioAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check radioAddNew, uncheck radioUseExisting, give focus to edit text, make
                // keyboard appear
                radioAddNew.setChecked(true);
                radioUseExisting.setChecked(false);
                categoryNameEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
            }
        });
        radioUseExisting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //uncheck radioAddNew, check radioUseExisting take focus from edit text, make
                // keyboard disappear
                radioAddNew.setChecked(false);
                radioUseExisting.setChecked(true);
                categoryNameEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(categoryNameEditText.getWindowToken(), 0);
            }
        });
        categoryNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check radioAddNew, uncheck radioUseExisting
                radioAddNew.setChecked(true);
                radioUseExisting.setChecked(false);
            }
        });
        categoryNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    //when gains focus, check radioAddNew, uncheck radioUseExisting
                    radioAddNew.setChecked(true);
                    radioUseExisting.setChecked(false);
                }
            }
        });
        categorySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //uncheck radioAddNew, check radioUseExisting, take focus from edit text, make
                // keyboard disappear
                if(event.getAction() == MotionEvent.ACTION_UP){
                    radioAddNew.setChecked(false);
                    radioUseExisting.setChecked(true);
                    categoryNameEditText.clearFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(categoryNameEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        //adapter for categorySpinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_layout_add_category_dialog){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){

                View v = super.getView(position, convertView, parent);
                if (position == getCount()){
                    ((TextView) v.findViewById(R.id.spinnerLayoutTextView)).setText("");

                    //set the hint for the spinner
                    ((TextView)v.findViewById(R.id.spinnerLayoutTextView)).setHint(getItem(getCount()));
                }
                return v;
            }

            @Override
            public int getCount(){

                //last item in adapter will be hint, which shouldn't be displayed in drop down
                return  super.getCount() -1;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //add categories in unusedCategoryList
        for(int i = 0; i < unusedCategoryList.size(); i++){
            adapter.add(unusedCategoryList.get(i).getCategoryName());
        }

        //add hint to end of adapter
        //adapter.add("Test");
        if(unusedCategoryList.size() > 0) {
            adapter.add(MainActivity.this.getResources().getString(R.string.spinner_add_category_hint));
        }else {

            //display no categories if there are none
            adapter.add("No Categories");
            adapter.add("No Categories");
        }

        //set spinner adapter
        categorySpinner.setAdapter(adapter);

        //set default selection to hint
        categorySpinner.setSelection(adapter.getCount());

        // show it
        alertDialog.show();
    }

    private class AsyncAddNewCategory extends AsyncTask<CategoryObj, Void, Boolean>{


        @Override
        protected Boolean doInBackground(CategoryObj... params) {

            CategoryObj categoryObj = params[0];
            /*add category to category table
            add category to expense table with expenses of 0.00
            reload the category list view
             */

            //if name is unique, proceed
            if(myDBHelper.checkCategoryName(categoryObj)){

                //proceed

                CategoryObj newCategoryObj = myDBHelper.addCategory(categoryObj);
                myDBHelper.addNewCategoryExpense(CURRENT_BUDGET, BUDGET_NAME, newCategoryObj);

                return true;
            }else{
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result){

            if(!result){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Invalid Category Name")
                        .setMessage("The category name entered already exists. Please enter a unique name.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                addCategory();
                            }
                        })
                        .show();
            }else {
                AsyncLoadList loadList = new AsyncLoadList();
                loadList.execute();
            }
        }


    }

    private class AsyncAddExistingCategory extends AsyncTask<CategoryObj, Void, Boolean>{


        @Override
        protected Boolean doInBackground(CategoryObj... params) {
            return null;
        }
    }




}
