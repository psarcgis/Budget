package com.gregrussell.budget;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 3/14/2017.
 */

public class DisplayCategory extends Activity{

    List<SpendingObj> spendingList = new ArrayList<SpendingObj>();
    TextView projected;
    TextView actual;
    ListView list;
    TextView categoryTextView;
    DataBaseHelperCategory myDBHelper;
    int categoryID;
    private static ExpenseObj expenseObj = new ExpenseObj();
    ImageView editButtonProjected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_category);

        //get the category ID from the extra on the intent
        Intent intent = getIntent();
        categoryID = intent.getIntExtra(MainActivity.EXTRA_MESSAGE_TO_DISPLAY_CATEGORY,-1);

        //initializing views
        projected = (TextView)findViewById(R.id.projectedDisplayCategory);
        actual = (TextView)findViewById(R.id.actualDisplayCategory);
        categoryTextView = (TextView)findViewById(R.id.categoryDisplayCategory);
        ImageView backButton = (ImageView) findViewById(R.id.backButtonDisplayCategory);

        //set button functionality
        ImageView editButtonProjected = (ImageView)findViewById(R.id.editProjectedDisplayCategory);
        editButtonProjected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("displaycategory", "clicked edit; category id is " + categoryID + "; budget id is " + MainActivity.CURRENT_BUDGET);
                AsyncEditProjectedExpenses task = new AsyncEditProjectedExpenses();
                task.execute("19500.59");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //load category and projected and actual expenses
        AsyncLoadCategoryName loadCategory = new AsyncLoadCategoryName();
        loadCategory.execute();

        AsyncLoadProjectedAndActual loadProjAndActual = new AsyncLoadProjectedAndActual();
        loadProjAndActual.execute();



    }

    private class AsyncLoadCategoryName extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params){

            return getCategory();

        }

        @Override
        protected void onPostExecute(String category){
            categoryTextView.setText(category);
        }
    }

    private String getCategory(){

        myDBHelper = new DataBaseHelperCategory(DisplayCategory.this);

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

        //use myDBHelper to get projected expense and return value
        String category = myDBHelper.getCategory(categoryID);
        myDBHelper.close();

        return category;
    }





    private class AsyncLoadProjectedAndActual extends AsyncTask<Void,Void,String[]>{

        @Override
        protected String[] doInBackground(Void... params) {

            //get projected expense and actual expenses
            double projExpense = projectedExpenses().getSpent();
            double actualExpense = actualExpenses();


            //currency formatter
            NumberFormat fmt = NumberFormat.getCurrencyInstance();

            //String array that is returned
            String[] numbers = {fmt.format(projExpense),fmt.format(actualExpense)};
            return numbers;


        }

        @Override
        protected void onPostExecute(String[] numbers) {


            projected.setText(numbers[0]);
            actual.setText(numbers[1]);

        }
    }


    private ExpenseObj projectedExpenses(){

        //open the database
        myDBHelper = new DataBaseHelperCategory(DisplayCategory.this);

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

        //use myDBHelper to get projected expense object and return
        expenseObj = myDBHelper.getProjectedExpenseForCategory(categoryID, MainActivity.CURRENT_BUDGET);
        myDBHelper.close();


        return expenseObj;

    }

    private Double actualExpenses(){

        //open the database
        myDBHelper = new DataBaseHelperCategory(DisplayCategory.this);

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

        //use myDBHelper to get projected expense and return value
        double actualExpense = myDBHelper.getSpentAmountForCategory(categoryID, MainActivity.CURRENT_BUDGET);
        myDBHelper.close();

        return actualExpense;

    }



    private class AsyncEditProjectedExpenses extends AsyncTask<String,Void,Double>{

        @Override
        protected Double doInBackground(String... projectedAmountString) {



            //get the expense object
           ExpenseObj expenseObj = projectedExpenses();

            //update the expense object's Spent param with projectedAmountString
            expenseObj.setSpent(Double.parseDouble(projectedAmountString[0]));

            Log.d("displaycategory", "projectedamountstring is " + projectedAmountString[0] +
                    "expenseObj cat is " + expenseObj.getCategoryName() + " expenseobj cat id is " +
                    expenseObj.getCategoryID() + " expenseobj id is " + expenseObj.getID());

            //open the database
            myDBHelper = new DataBaseHelperCategory(DisplayCategory.this);

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

            int i = myDBHelper.updateExpense(expenseObj);
            Log.d("displaycategory", "updated row is " + i);
            myDBHelper.close();

            return null;


        }

        @Override
        protected void onPostExecute(Double newProjectedExpenses) {

            AsyncLoadProjectedAndActual task = new AsyncLoadProjectedAndActual();
            task.execute();



        }

    }

}
