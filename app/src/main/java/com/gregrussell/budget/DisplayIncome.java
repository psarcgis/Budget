package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    EditText projectedEditText;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_income);

        Log.d("DisplayIncome", "Current Budget is " + String.valueOf(MainActivity.CURRENT_BUDGET));

        //initializing for editButton functionality
        final Context context = this;
        final LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        //initializing views
        projected = (TextView)findViewById(R.id.projectedTextViewDisplayIncome);
        actual = (TextView)findViewById(R.id.actualDisplayIncome);
        ImageView backButton = (ImageView) findViewById(R.id.backButtonDisplayIncome);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //set button functionality
        final ImageView editButtonProjected = (ImageView)findViewById(R.id.editProjectedDisplayIncome);
        editButtonProjected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final View editExpensesDialog = inflater.inflate(R.layout.edit_projected_income,null);
                projectedEditText = (EditText)editExpensesDialog.findViewById(R.id.editTextEditProjectedIncome);




                //create a dialog box to enter new projected expenses

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(editExpensesDialog);



                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        String editTextString = String.valueOf(projectedEditText.getText());
                                        double editTextDouble;

                                        //checks if input is a double, and updates projected expenses if it is
                                        try{
                                            editTextDouble = Double.parseDouble(editTextString);
                                            //currency formatter
                                            NumberFormat fmt = NumberFormat.getCurrencyInstance();
                                            projected.setText(fmt.format(editTextDouble));


                                            AsyncEditProjectedIncome task = new AsyncEditProjectedIncome();
                                            //call task to update table with user inputed value
                                            task.execute(editTextDouble);

                                        }catch (Exception e){
                                            e.printStackTrace();
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
                AlertDialog alertDialog = alertDialogBuilder.create();

                TextWatcher textWatcher = new TextWatcher() {
                    int decimal;
                    String afterDecimal;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        Log.d("onTextChanged", afterDecimal + "decimal is " + String.valueOf(decimal));
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        decimal = s.toString().indexOf(".");
                        if(decimal > 1 ) {
                            afterDecimal = s.toString().substring(decimal + 1);
                        }
                        Log.d("onTextChanged", afterDecimal + "decimal is " + String.valueOf(decimal));



                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        if(afterDecimal != null && afterDecimal.length() > 2){
                            Editable editable = new SpannableStringBuilder(s.toString().substring(0,decimal+3));
                            s = editable;
                            projectedEditText.setText(s.toString());
                            //projected.setText(s.toString());
                        }
                        Log.d("afterTextChanged", s.toString());

                    }
                };

                projectedEditText.addTextChangedListener(textWatcher);

                // show it
                alertDialog.show();

            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        AsyncLoadProjectedAndActual loadProjAndActual = new AsyncLoadProjectedAndActual();
        loadProjAndActual.execute();



    }



    private class AsyncLoadProjectedAndActual extends AsyncTask<Void,Void,String[]>{

        @Override
        protected String[] doInBackground(Void... params) {

            //get projected expense and actual expenses
            double projExpense = projectedIncome().getIncome();
            double actualExpense = actualIncome();

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


    private IncomeObj projectedIncome(){

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

        //use myDBHelper to get projected income and return value
        IncomeObj incomeObj = myDBHelper.getProjectedIncome(MainActivity.CURRENT_BUDGET);
        myDBHelper.close();

        return incomeObj;

    }

    private Double actualIncome(){

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


    private class AsyncEditProjectedIncome extends AsyncTask<Double,Void,Double>{

        @Override
        protected Double doInBackground(Double... projectedAmount) {



            //get the expense object
            IncomeObj incomeObj = projectedIncome();

            //update the expense object's Spent param with projectedAmountString
            incomeObj.setIncome(projectedAmount[0]);

            Log.d("displaycategory", "projectedamountstring is " + projectedAmount[0] +
                    "expenseObj cat is "  + " expenseobj cat id is " +
                    " expenseobj id is " +incomeObj.getID());

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

            int i = myDBHelper.updateIncome(incomeObj);
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
