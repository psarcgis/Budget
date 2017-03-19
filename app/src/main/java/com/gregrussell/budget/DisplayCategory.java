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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    String dateString;
    Calendar myCalendar;
    ExpenseObj expenseObj;





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
        list = (ListView)findViewById(R.id.listViewDisplayCategory);
        projected = (TextView)findViewById(R.id.projectedTextViewDisplayCategory);
        actual = (TextView)findViewById(R.id.actualDisplayCategory);
        categoryTextView = (TextView)findViewById(R.id.categoryDisplayCategory);
        ImageView backButton = (ImageView) findViewById(R.id.backButtonDisplayCategory);
        FloatingActionButton addButton = (FloatingActionButton)findViewById(R.id.addDisplayCategory);




        //set button functionality
        final ImageView editButtonProjected = (ImageView)findViewById(R.id.editProjectedDisplayCategory);
        editButtonProjected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editProjectedExpenses(context,inflater);

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

                addNewSpending(context,inflater);


            }
        });




        //load category and projected and actual expenses
        AsyncLoadCategoryName loadCategory = new AsyncLoadCategoryName();
        loadCategory.execute();

        AsyncLoadProjectedAndActual loadProjAndActual = new AsyncLoadProjectedAndActual();
        loadProjAndActual.execute();

        AsyncLoadList loadList = new AsyncLoadList();
        loadList.execute();



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
            double diff = actualExpense - projExpense;

            //currency formatter
            NumberFormat fmt = NumberFormat.getCurrencyInstance();

            //formatter to convert double under 1000 to currency (only for difference text view)
            DecimalFormat lowFmt = new DecimalFormat("+$#,##0.00;-$#,##0.00");



            //array that is returned

            String[] numbers = {fmt.format(projExpense),fmt.format(actualExpense),lowFmt.format(diff)};
            return numbers;


        }

        @Override
        protected void onPostExecute(String[] numbers) {


            projected.setText((numbers[0]));
            actual.setText(numbers[1] + " (" + numbers[2] + ")");

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



    private class AsyncEditProjectedExpenses extends AsyncTask<Double,Void,Double>{

        @Override
        protected Double doInBackground(Double... projectedAmount) {



            //get the expense object
           expenseObj = projectedExpenses();

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

    public void editProjectedExpenses(final Context context,final LayoutInflater inflater){

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
                                    editProjectedExpenses(context,inflater);
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
                Log.d("onTextChanged", "decimal is " + decimal);
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


    public void addNewSpending(final Context context,final LayoutInflater inflater){

        final View addSpendingDialog = inflater.inflate(R.layout.add_new_spending_dialog_layout,null);
        final TextView dateText = (TextView)addSpendingDialog.findViewById(R.id.dateAddNewSpendingDialog);
        final EditText spentEdit = (EditText)addSpendingDialog.findViewById(R.id.spentAddNewSpendingDialog);
        final EditText descriptionEdit = (EditText)addSpendingDialog.findViewById(R.id.descriptionAddNewSpendingDialog);

        TextView categoryText = (TextView)addSpendingDialog.findViewById(R.id.categoryAddNewSpendingDialog);
        categoryText.setText(categoryString);

        //set the date to today
        myCalendar = Calendar.getInstance();
        //format the date
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
        dateText.setText(sdf.format(myCalendar.getTime()));

        dateText.setClickable(true);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String date = dateReturn(context);

                myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        //format the date
                        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
                        dateText.setText(sdf.format(myCalendar.getTime()));

                        Log.d("addnewspending", "date is " + sdf.format(myCalendar.getTime()));
                    }
                };

                DatePickerDialog dpg = new DatePickerDialog(context, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                dpg.show();




            }
        });



        //create a dialog box to enter new projected expenses
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

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

                                    SpendingObj spendingObj = new SpendingObj(myCalendar.getTime(),
                                            expenseObj.getBudgetID(),expenseObj.getBudgetName(),
                                            expenseObj.getCategoryID(),expenseObj.getCategoryName(),
                                            editTextDouble,descriptionString);

                                    AsyncAddSpending task = new AsyncAddSpending();
                                    task.execute(spendingObj);



                                }catch (Exception e){
                                    e.printStackTrace();
                                    addNewSpending(context,inflater);
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

    private String dateReturn(final Context context){

        myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy");
                dateString = sdf.format(myCalendar.getTime());

            }





        };


        DatePickerDialog dpg = new DatePickerDialog(context, date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
        dpg.show();

        return dateString;


    }

    private class AsyncAddSpending extends AsyncTask<SpendingObj,Void,ListViewAdapterSpending>{


        @Override
        protected ListViewAdapterSpending doInBackground(SpendingObj... spendingObj) {

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

            //use myDBHelper add spendingObj to the Spending table
            myDBHelper.addSpending(spendingObj[0]);
            myDBHelper.close();

            List<SpendingObj> spendingObjList = myDBHelper.getSpendingsByCategory(MainActivity.CURRENT_BUDGET, categoryID);

            ListViewAdapterSpending adapter = null;
            if(spendingObjList != null) {
                adapter = new ListViewAdapterSpending(DisplayCategory.this, spendingObjList);
            }
            return adapter;

        }

        @Override
        protected void onPostExecute(ListViewAdapterSpending adapter){


            list.setAdapter(adapter);
            AsyncLoadProjectedAndActual task = new AsyncLoadProjectedAndActual();
            task.execute();
        }
    }

    private class AsyncLoadList extends AsyncTask<Void,Void,ListViewAdapterSpending>{



        List<SpendingObj> spendingObjList;


        @Override
        protected ListViewAdapterSpending doInBackground(Void... params) {
            return populateList();

        }

        @Override
        protected void onPostExecute(ListViewAdapterSpending adapter){



                list.setAdapter(adapter);


            AsyncLoadProjectedAndActual task = new AsyncLoadProjectedAndActual();
            task.execute();


        }

        private ListViewAdapterSpending populateList(){

            Log.d("listViewAdapter", "PopulateList method is running yay");

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

            spendingObjList = myDBHelper.getSpendingsByCategory(MainActivity.CURRENT_BUDGET,categoryID);
            myDBHelper.close();



            //set listview adapter
            View view = findViewById(R.id.listViewFrame);
            ListViewAdapterSpending adapter = null;
            if(spendingObjList != null) {
                adapter = new ListViewAdapterSpending(DisplayCategory.this, spendingObjList);
            }
            return adapter;






        }

    }



}
