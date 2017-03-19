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
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
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
    EditText projectedEditText;
    String categoryString;
    TextView categoryEditProjectedExpenses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_category);

        //initializing for editButton functionality
        final Context context = this;
        final LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        //get the category ID from the extra on the intent
        Intent intent = getIntent();
        categoryID = intent.getIntExtra(MainActivity.EXTRA_MESSAGE_TO_DISPLAY_CATEGORY,-1);

        //initializing views
        projected = (TextView)findViewById(R.id.projectedTextViewDisplayCategory);
        actual = (TextView)findViewById(R.id.actualDisplayCategory);
        categoryTextView = (TextView)findViewById(R.id.categoryDisplayCategory);
        ImageView backButton = (ImageView) findViewById(R.id.backButtonDisplayCategory);






        //set button functionality
        final ImageView editButtonProjected = (ImageView)findViewById(R.id.editProjectedDisplayCategory);
        editButtonProjected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final View editExpensesDialog = inflater.inflate(R.layout.edit_projected_expenses,null);
                projectedEditText = (EditText)editExpensesDialog.findViewById(R.id.editTextEditProjectedExpenses);
                categoryEditProjectedExpenses = (TextView)editExpensesDialog.findViewById(R.id.categoryEditProjectedExpenses);
                categoryEditProjectedExpenses.setText(categoryString);



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


                                            AsyncEditProjectedExpenses task = new AsyncEditProjectedExpenses();
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
            categoryString = category;
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


            //array that is returned

            String[] numbers = {fmt.format(projExpense),fmt.format(actualExpense)};
            return numbers;


        }

        @Override
        protected void onPostExecute(String[] numbers) {


            projected.setText((numbers[0]));
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
        ExpenseObj expenseObj = myDBHelper.getProjectedExpenseForCategory(categoryID, MainActivity.CURRENT_BUDGET);
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



    private class AsyncEditProjectedExpenses extends AsyncTask<Double,Void,Double>{

        @Override
        protected Double doInBackground(Double... projectedAmount) {



            //get the expense object
           ExpenseObj expenseObj = projectedExpenses();

            //update the expense object's Spent param with projectedAmountString
            expenseObj.setSpent(projectedAmount[0]);

            Log.d("displaycategory", "projectedamountstring is " + projectedAmount[0] +
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
