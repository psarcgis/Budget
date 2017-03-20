package com.gregrussell.budget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by greg on 3/15/2017.
 */

public class DisplayIncome extends Activity {

    List<EarningObj> earningList = new ArrayList<EarningObj>();
    TextView projected;
    TextView actual;
    ListView list;
    DataBaseHelperCategory myDBHelper;
    EditText projectedEditText;
    Calendar myCalendar;
    IncomeObj incomeObj;
    LayoutInflater inflater;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_income);

        Log.d("DisplayIncome", "Current Budget is " + String.valueOf(MainActivity.CURRENT_BUDGET));

        //initializing for editButton functionality
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //initializing views
        list = (ListView)findViewById(R.id.listViewDisplayIncome);
        projected = (TextView)findViewById(R.id.projectedTextViewDisplayIncome);
        actual = (TextView)findViewById(R.id.actualDisplayIncome);
        TextView categoryTextView = (TextView)findViewById(R.id.categoryDisplayIncome);
        categoryTextView.setText(getResources().getText(R.string.income));
        ImageView backButton = (ImageView) findViewById(R.id.backButtonDisplayIncome);
        FloatingActionButton addButton = (FloatingActionButton)findViewById(R.id.addDisplayIncome);

        //set button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addNewEarning();
            }
        });





        AsyncLoadProjectedAndActual loadProjAndActual = new AsyncLoadProjectedAndActual();
        loadProjAndActual.execute();
        AsyncLoadList loadList = new AsyncLoadList();
        loadList.execute();



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
        incomeObj = myDBHelper.getProjectedIncome(MainActivity.CURRENT_BUDGET);
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


    /*private class AsyncEditProjectedIncome extends AsyncTask<Double,Void,Double>{

            @Override      protected Double doInBackground(Double... projectedAmount) {



            //get the expense object
            incomeObj = projectedIncome();

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

    }*/

    /*private void editProjectedIncome(){

        final View editExpensesDialog = inflater.inflate(R.layout.edit_projected_income,null);
        projectedEditText = (EditText)editExpensesDialog.findViewById(R.id.editTextEditProjectedIncome);

        //create a dialog box to enter new projected expenses
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DisplayIncome.this);

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
                                    editProjectedIncome();
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

        //TextWatcher to prevent user from inputting more than 2 digits after the decimal
        TextWatcher textWatcher = new TextWatcher() {
            int decimal;
            String afterDecimal;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                Log.d("onTextChanged", afterDecimal + "decimal is " + String.valueOf(decimal));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //if there is a decimal, get string of digits following decimal
                decimal = s.toString().indexOf(".");
                if(decimal >= 0) {
                    afterDecimal = s.toString().substring(decimal + 1);
                }
                Log.d("onTextChanged", afterDecimal + "decimal is " + String.valueOf(decimal));
            }

            @Override
            public void afterTextChanged(Editable s) {

                //prevent more than two digits being added after decimal
                if(afterDecimal != null && afterDecimal.length() > 2){
                    s = new SpannableStringBuilder(s.toString().substring(0,decimal+3));
                    projectedEditText.setText(s.toString());
                }
                Log.d("afterTextChanged", s.toString());
            }
        };

        projectedEditText.addTextChangedListener(textWatcher);

        // show it
        alertDialog.show();
    }*/

    public void addNewEarning(){

        final View addSpendingDialog = inflater.inflate(R.layout.add_new_spending_dialog_layout,null);
        final TextView dateText = (TextView)addSpendingDialog.findViewById(R.id.dateAddNewSpendingDialog);
        final EditText spentEdit = (EditText)addSpendingDialog.findViewById(R.id.spentAddNewSpendingDialog);
        final EditText descriptionEdit = (EditText)addSpendingDialog.findViewById(R.id.descriptionAddNewSpendingDialog);
        TextView categoryText = (TextView)addSpendingDialog.findViewById(R.id.categoryAddNewSpendingDialog);
        categoryText.setText(DisplayIncome.this.getResources().getText(R.string.income));
        TextView spentEarnedText = (TextView)addSpendingDialog.findViewById(R.id.spentEarnedAddNewSpendingDialog);
        spentEarnedText.setText(DisplayIncome.this.getResources().getText(R.string.earned));

        //set the date to today
        myCalendar = Calendar.getInstance();

        //format the date
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
        dateText.setText(sdf.format(myCalendar.getTime()));
        dateText.setClickable(true);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        //format the date
                        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
                        dateText.setText(sdf.format(myCalendar.getTime()));

                        Log.d("addnewearning", "date is " + sdf.format(myCalendar.getTime()));
                    }
                };
                DatePickerDialog dpg = new DatePickerDialog(DisplayIncome.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                dpg.show();
            }
        });
        //create a dialog box to enter new projected expenses
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DisplayIncome.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(addSpendingDialog);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String editTextString = String.valueOf(spentEdit.getText());
                                double editTextDouble;

                                //checks if input is a double, and updates projected expenses if it is
                                try{
                                    String descriptionString = "";
                                    descriptionString = String.valueOf(descriptionEdit.getText());
                                    editTextDouble = Double.parseDouble(editTextString);

                                    //currency formatter
                                    NumberFormat fmt = NumberFormat.getCurrencyInstance();
                                    EarningObj earningObj = new EarningObj(myCalendar.getTime(),
                                            incomeObj.getBudgetID(),incomeObj.getBudgetName(),
                                            editTextDouble,descriptionString);
                                    AsyncAddEarning task = new AsyncAddEarning();
                                    task.execute(earningObj);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    addNewEarning();
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

        //TextWatcher to prevent user from inputting more than 2 digits after the decimal
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
                if(decimal >= 0) {
                    afterDecimal = s.toString().substring(decimal + 1);
                }
                Log.d("onTextChanged", afterDecimal + "decimal is " + String.valueOf(decimal));
            }

            @Override
            public void afterTextChanged(Editable s) {

                if(afterDecimal != null && afterDecimal.length() > 2){
                    Editable editable = new SpannableStringBuilder(s.toString().substring(0,decimal+3));
                    s = editable;
                    spentEdit.setText(s.toString());
                    //projected.setText(s.toString());
                }
                Log.d("afterTextChanged", s.toString());
            }
        };
        spentEdit.addTextChangedListener(textWatcher);
        // show it
        alertDialog.show();
    }

    private class AsyncAddEarning extends AsyncTask<EarningObj,Void,ListViewAdapterEarning>{


        List<EarningObj> earningObjList;

        @Override
        protected ListViewAdapterEarning doInBackground(EarningObj... earningObj) {

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

            //use myDBHelper add earningObj to the Earning table
            myDBHelper.addEarning(earningObj[0]);
            earningObjList = myDBHelper.getEarningsList(MainActivity.CURRENT_BUDGET);
            myDBHelper.close();
            ListViewAdapterEarning adapter = null;
            if(earningObjList != null) {
                adapter = new ListViewAdapterEarning(DisplayIncome.this, earningObjList);
            }
            return adapter;
        }

        @Override
        protected void onPostExecute(ListViewAdapterEarning adapter){

            list.setAdapter(adapter);
            //set listener for list item click
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick", "click " + String.valueOf(position));
                    //use position to get the earningObj that's to be loaded
                    clickedEarning(earningObjList.get(position));
                }
            });
            AsyncLoadProjectedAndActual task = new AsyncLoadProjectedAndActual();
            task.execute();
        }
    }


    private class AsyncLoadList extends AsyncTask<Void,Void,ListViewAdapterEarning>{


        List<EarningObj> earningObjList;


        @Override
        protected ListViewAdapterEarning doInBackground(Void... params) {
            return populateList();
        }

        @Override
        protected void onPostExecute(ListViewAdapterEarning adapter){
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick", "click " + String.valueOf(position));
                    //use position to get the earningObj that's to be loaded
                    clickedEarning(earningObjList.get(position));
                }
            });
            AsyncLoadProjectedAndActual task = new AsyncLoadProjectedAndActual();
            task.execute();
        }

        private ListViewAdapterEarning populateList(){


            Log.d("listViewAdapter", "PopulateList method is running yay");

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
            earningObjList = myDBHelper.getEarningsList(MainActivity.CURRENT_BUDGET);
            myDBHelper.close();

            //set listview adapter
            View view = findViewById(R.id.listViewFrame);
            ListViewAdapterEarning adapter = null;
            if(earningObjList != null) {
                adapter = new ListViewAdapterEarning(DisplayIncome.this, earningObjList);
            }
            return adapter;
        }

    }

    private void clickedEarning(final EarningObj earningObjOld){


        final View addSpendingDialog = inflater.inflate(R.layout.add_new_spending_dialog_layout,null);
        final TextView dateText = (TextView)addSpendingDialog.findViewById(R.id.dateAddNewSpendingDialog);
        final EditText spentEdit = (EditText)addSpendingDialog.findViewById(R.id.spentAddNewSpendingDialog);
        final EditText descriptionEdit = (EditText)addSpendingDialog.findViewById(R.id.descriptionAddNewSpendingDialog);
        TextView categoryText = (TextView)addSpendingDialog.findViewById(R.id.categoryAddNewSpendingDialog);
        TextView spentEarnedText = (TextView)addSpendingDialog.findViewById(R.id.spentEarnedAddNewSpendingDialog);


        //set text
        categoryText.setText(DisplayIncome.this.getResources().getText(R.string.income));
        spentEarnedText.setText(DisplayIncome.this.getResources().getText(R.string.earned));

        //formatter to convert double to 2 decimal places
        DecimalFormat fmt = new DecimalFormat("##0.00;-##0.00");
        spentEdit.setText(fmt.format(earningObjOld.getEarned()));
        descriptionEdit.setText(earningObjOld.getDescription());


        //format the date
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
        dateText.setText(sdf.format(earningObjOld.getDate().getTime()));
        dateText.setClickable(true);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myCalendar = Calendar.getInstance();
                myCalendar.setTime(earningObjOld.getDate());
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        //format the date
                        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
                        dateText.setText(sdf.format(myCalendar.getTime()));

                        Log.d("addnewearning", "date is " + sdf.format(myCalendar.getTime()));
                    }
                };
                DatePickerDialog dpg = new DatePickerDialog(DisplayIncome.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                dpg.show();
            }
        });
        //create a dialog box to enter new projected expenses
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DisplayIncome.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(addSpendingDialog);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String editTextString = String.valueOf(spentEdit.getText());
                                double editTextDouble;

                                //checks if input is a double, and updates projected expenses if it is
                                try{
                                    String descriptionString = "";
                                    descriptionString = String.valueOf(descriptionEdit.getText());
                                    editTextDouble = Double.parseDouble(editTextString);
                                    EarningObj earningObj;
                                    try {
                                        earningObj = new EarningObj(earningObjOld.getID(),
                                                myCalendar.getTime(), earningObjOld.getBudgetID(),
                                                earningObjOld.getBudgetName(), editTextDouble,
                                                descriptionString);
                                    }catch (Exception e){

                                        earningObj = new EarningObj(earningObjOld.getID(),
                                                earningObjOld.getDate(), earningObjOld.getBudgetID(),
                                                earningObjOld.getBudgetName(), editTextDouble,
                                                descriptionString);
                                    }
                                    AsyncEditEarning task = new AsyncEditEarning();
                                    task.execute(earningObj);
                                }catch (Exception e){
                                    e.printStackTrace();
                                    clickedEarning(earningObjOld);
                                }

                            }


                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        })
                .setNeutralButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new AlertDialog.Builder(DisplayIncome.this)
                                        .setCancelable(false)
                                        .setTitle("Delete Entry?")
                                        .setMessage("Are you sure you want to delete this entry?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                AsyncDeleteEarning task = new AsyncDeleteEarning();
                                                task.execute(earningObjOld);
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                return;
                                            }
                                        })
                                        .show();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        //TextWatcher to prevent user from inputting more than 2 digits after the decimal
        TextWatcher textWatcher = new TextWatcher() {
            int decimal;
            String afterDecimal;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                Log.d("onTextChanged", afterDecimal + "decimal is " + String.valueOf(decimal));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //if there is a decimal, get string of digits following decimal
                decimal = s.toString().indexOf(".");
                if(decimal >= 0) {
                    afterDecimal = s.toString().substring(decimal + 1);
                }
                Log.d("onTextChanged", afterDecimal + "decimal is " + String.valueOf(decimal));
            }

            @Override
            public void afterTextChanged(Editable s) {

                //prevent more than two digits being added after decimal
                if(afterDecimal != null && afterDecimal.length() > 2){
                    Editable editable = new SpannableStringBuilder(s.toString().substring(0,decimal+3));
                    s = editable;
                    spentEdit.setText(s.toString());
                }
                Log.d("afterTextChanged", s.toString());
            }
        };
        spentEdit.addTextChangedListener(textWatcher);
        // show it
        alertDialog.show();
    }


    //class to edit an entry
    private class AsyncEditEarning extends AsyncTask<EarningObj,Void,ListViewAdapterEarning>{


        List<EarningObj> earningObjList;

        @Override
        protected ListViewAdapterEarning doInBackground(EarningObj... earningObj) {

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

            //use myDBHelper update given earningObj
            myDBHelper.updateEarning(earningObj[0]);
            earningObjList = myDBHelper.getEarningsList(MainActivity.CURRENT_BUDGET);
            myDBHelper.close();
            ListViewAdapterEarning adapter = null;
            if(earningObjList != null) {
                adapter = new ListViewAdapterEarning(DisplayIncome.this, earningObjList);
            }
            return adapter;
        }

        @Override
        protected void onPostExecute(ListViewAdapterEarning adapter){

            list.setAdapter(adapter);
            //set listener for list item click
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick", "click " + String.valueOf(position));
                    //use position to get the earningObj that's to be loaded
                    clickedEarning(earningObjList.get(position));
                }
            });
            AsyncLoadProjectedAndActual task = new AsyncLoadProjectedAndActual();
            task.execute();
        }
    }

    //class to delete a entry
    private class AsyncDeleteEarning extends AsyncTask<EarningObj,Void,ListViewAdapterEarning>{


        List<EarningObj> earningObjList;

        @Override
        protected ListViewAdapterEarning doInBackground(EarningObj... earningObj) {

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

            //use myDBHelper to delete given earningObj
            myDBHelper.deleteEarning(earningObj[0]);
            earningObjList = myDBHelper.getEarningsList(MainActivity.CURRENT_BUDGET);
            myDBHelper.close();
            ListViewAdapterEarning adapter = null;
            if(earningObjList != null) {
                adapter = new ListViewAdapterEarning(DisplayIncome.this, earningObjList);
            }
            return adapter;
        }

        @Override
        protected void onPostExecute(ListViewAdapterEarning adapter){

            list.setAdapter(adapter);
            //set listener for list item click
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick", "click " + String.valueOf(position));
                    //use position to get the earningObj that's to be loaded
                    clickedEarning(earningObjList.get(position));
                }
            });
            AsyncLoadProjectedAndActual task = new AsyncLoadProjectedAndActual();
            task.execute();
        }
    }

}
