package com.gregrussell.budget;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 3/15/2017.
 */

public class DisplayIncome extends Activity {

    List<SpendingObj> spendingList = new ArrayList<SpendingObj>();
    TextView projected;
    TextView actual;
    ListView list;
    DataBaseHelperCategory myDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_income);

        Log.d("DisplayIncome", "Current Budget is " + String.valueOf(MainActivity.CURRENT_BUDGET));

        //initializing views
        projected = (TextView)findViewById(R.id.projectedDisplayIncome);
        actual = (TextView)findViewById(R.id.actualDisplayIncome);


        AsyncLoadProjectedAndActual loadProjAndActual = new AsyncLoadProjectedAndActual();
        loadProjAndActual.execute();



    }



    private class AsyncLoadProjectedAndActual extends AsyncTask<Void,Void,String[]>{

        @Override
        protected String[] doInBackground(Void... params) {

            //get projected expense and actual expenses
            double projExpense = ProjectedExpenses();
            double actualExpense = ActualExpenses();

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


    private Double ProjectedExpenses(){

        //open the database
        myDBHelper = new DataBaseHelperCategory(DisplayIncome.this);

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
        double projExpense = myDBHelper.getProjectedIncome(MainActivity.CURRENT_BUDGET);
        myDBHelper.close();

        return projExpense;

    }

    private Double ActualExpenses(){

        //open the database
        myDBHelper = new DataBaseHelperCategory(DisplayIncome.this);

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
        double actualExpense = myDBHelper.getEarnedAmount(MainActivity.CURRENT_BUDGET);
        myDBHelper.close();

        return actualExpense;

    }

}
