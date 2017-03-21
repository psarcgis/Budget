package com.gregrussell.budget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 2/16/2017.
 */

public class DataBaseHelperCategory extends SQLiteOpenHelper{



    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    //file path of DB
    private static String DB_PATH = "/data/data/com.gregrussell.budget/databases/";

    private static String DB_NAME = "category.db";


    private SQLiteDatabase myDataBase;

    private final Context myContext;



    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String VENDOR_ID = "Categories";

    //Default Budget Categories
    private static final String income = "Income";
    private static final String[] categories = {"Rent", "Utilities", "Grocery", "Transportation", "Insurance", "Cell Phone", "Savings", "Restaurants", "Other"};


    public DataBaseHelperCategory(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;


        Log.d("filePath", DB_PATH);
    }




    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){

            Log.d("CheckDataBase", "Database already exists...");
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.

            Log.d("CheckDataBase", "Database doesn't exist, creating...");
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL(CATEGORIES_CREATE_ENTRIES);
            db.execSQL(BUDGETS_CREATE_ENTRIES);
            db.execSQL(EXPENSES_CREATE_ENTRIES);
            db.execSQL(INCOME_CREATE_ENTRIES);
            db.execSQL(SPENDING_CREATE_ENTRIES);
            db.execSQL(EARNING_CREATE_ENTRIES);
            addDefaultCategories();






        }

    }


    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }




    @Override
    public void onCreate(SQLiteDatabase db) {



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    private void addDefaultCategories(){
        String no = null;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        //income is a special category that will always be used. Its category column has '2' to show it's special


        for(int i= 0; i < categories.length; i++) {
            values.put(Categories.CATEGORIES_CATEGORY, categories[i]);
            //adding 1 to Default column to denote as default (0 if not default)
            values.put(Categories.CATEGORIES_DEFAULT, 1);
            db.insert(Categories.CATEGORIES_TABLE_NAME, null, values);
        }



        // Inserting Row


        //Log.d("row inserted " , "new row id is " + Long.toString(db.insert(Categories.TABLE_NAME, null, values)));
        db.close(); // Closing database connection
    }


    public void addCategory(String category, Boolean isDefault) {
        String no = null;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Categories.CATEGORIES_CATEGORY, category);
        if (isDefault){
            values.put(Categories.CATEGORIES_DEFAULT, 1);
        }
        else {
            values.put(Categories.CATEGORIES_DEFAULT, 0);
        }


        // Inserting Row
        db.insert(Categories.CATEGORIES_TABLE_NAME, null, values);

        //Log.d("row inserted " , "new row id is " + Long.toString(db.insert(Categories.TABLE_NAME, null, values)));
        db.close(); // Closing database connection
    }


    public Timestamp getSpendingTimestamp(){
        SQLiteDatabase db = this.getReadableDatabase();

        Timestamp time;

        String selectQuery = "SELECT  * FROM " + Spending.SPENDING_TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        try {
            Log.d("Most Recent", cursor.getString(Constants.SPENDING_TIMESTAMP_POSITION));
            return Timestamp.valueOf(cursor.getString(Constants.SPENDING_TIMESTAMP_POSITION));
        }
        catch (Exception e){

            return null;
        }
    }

    public Timestamp getEarningTimestamp(){
        SQLiteDatabase db = this.getReadableDatabase();

        Timestamp time;

        String selectQuery = "SELECT  * FROM " + Earning.EARNING_TABLE_NAME;

        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        try {
            Log.d("Most Recent", cursor.getString(Constants.EARNING_TIMESTAMP_POSITION));
            return Timestamp.valueOf(cursor.getString(Constants.EARNING_TIMESTAMP_POSITION));
        }
        catch (Exception e){

            return null;
        }



    }

    public List<String> getAllCategories() {
        List<String> categoriesList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + Categories.CATEGORIES_TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                //1 is the column for category name
                String category = cursor.getString(1);






                // Adding contact to list
                categoriesList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return categoriesList;
    }

    public void addSpending(SpendingObj spendingObj) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Spending.SPENDING_DATE, spendingObj.getDate().getTime());
        values.put(Spending.SPENDING_BUDGET_ID, spendingObj.getBudgetID());
        values.put(Spending.SPENDING_BUDGET_NAME, spendingObj.getBudgetName());
        values.put(Spending.SPENDING_CATEGORY_ID, spendingObj.getCategoryID());
        values.put(Spending.SPENDING_CATEGORY_NAME, spendingObj.getCategoryName());
        values.put(Spending.SPENDING_SPENT, spendingObj.getSpent());
        values.put(Spending.SPENDING_EXPENSE_DESCRIPTION, spendingObj.getDescription());


        // Inserting Row
        db.insert(Spending.SPENDING_TABLE_NAME, null, values);

        //Log.d("row inserted " , "new row id is " + Long.toString(db.insert(Categories.TABLE_NAME, null, values)));
        db.close(); // Closing database connection
    }

    public void addEarning(EarningObj earningObj) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Earning.EARNING_DATE, earningObj.getDate().getTime());
        values.put(Earning.EARNING_BUDGET_ID, earningObj.getBudgetID());
        values.put(Earning.EARNING_BUDGET_NAME, earningObj.getBudgetName());
        values.put(Earning.EARNING_EARNED, earningObj.getEarned());
        values.put(Earning.EARNING_INCOME_DESCRIPTION, earningObj.getDescription());


        // Inserting Row
        db.insert(Earning.EARNING_TABLE_NAME, null, values);

        //Log.d("row inserted " , "new row id is " + Long.toString(db.insert(Categories.TABLE_NAME, null, values)));
        db.close(); // Closing database connection
    }




    public void addBudget(String budgetName) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Budgets.BUDGETS_NAME, budgetName);


        // Inserting Row
        db.insert(Budgets.BUDGETS_TABLE_NAME, null, values);

        //because a new budget has been created, the expenses table must be populated for this budget
        int mostRecentBudgetID = getMostRecentBudget();
        addDefaultIncome(mostRecentBudgetID,budgetName);
        addDefaultExpenses(mostRecentBudgetID,budgetName);


        db.close(); // Closing database connection
    }

    private void addDefaultIncome(int budgetID, String budgetName){
        //populate the income table for newly created budget
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        //adding income category to expenses table for new budget
        values.put(Income.INCOME_BUDGET_ID,budgetID);
        values.put(Income.INCOME_BUDGET_NAME,budgetName);
        values.put(Income.INCOME_ESTIMATE,0.00);
        db.insert(Income.INCOME_TABLE_NAME,null,values);
    }

    private void addDefaultExpenses(int budgetID, String budgetName){
        //populate the expenses table for newly created budget

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();



        //loop to add default categories

        String selectQuery = "SELECT * FROM " + Categories.CATEGORIES_TABLE_NAME + " WHERE " +
                Categories.CATEGORIES_DEFAULT  + " = " + 1;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                values.put(Expenses.EXPENSES_BUDGET_ID,budgetID);
                values.put(Expenses.EXPENSES_BUDGET_NAME,budgetName);
                values.put(Expenses.EXPENSES_CATEGORY_ID,cursor.getInt(Constants.CATEGORIES_ID_POSITION));
                values.put(Expenses.EXPENSES_CATEGORY_NAME,cursor.getString(Constants.CATEGORIES_NAME_POSITION));
                values.put(Expenses.EXPENSES_ESTIMATE,0.00);
                db.insert(Expenses.EXPENSES_TABLE_NAME,null,values);

            }while (cursor.moveToNext());


        }







    }




    public int getMostRecentSpending(){

        String selectQuery = "SELECT  * FROM " + Spending.SPENDING_TABLE_NAME +
                " ORDER BY " + Spending.SPENDING_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            //if there are no entries, cursor will come back null
            try {
                return cursor.getInt(Constants.SPENDING_BUDGET_ID_POSITION);
            } catch (Exception e) {

                return -1;
            }
        }
        else return -1;

    }

    public int getMostRecentEarning(){

        String selectQuery = "SELECT  * FROM " + Earning.EARNING_TABLE_NAME +
                " ORDER BY " + Earning.EARNING_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            //if there are no entries, cursor will come back null
            try {
                return cursor.getInt(Constants.EARNING_BUDGET_ID_POSITION);
            } catch (Exception e) {

                return -1;
            }
        }
        else return -1;

    }




    public int getMostRecentBudget(){

        Log.d("MostRecentBudget", "getMostRecentBudget method called");

        String selectQuery = "SELECT  * FROM " + Budgets.BUDGETS_TABLE_NAME +
                " ORDER BY " + Budgets._ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        //if there are no entries, cursor will come back null
        try {
            Log.d("MostRecentBudget", "most recent budgetID is " + cursor.getInt(Constants.BUDGETS_ID_POSITION));
            return cursor.getInt(Constants.BUDGETS_ID_POSITION);
        }
        catch (Exception e){

            return -1;
        }

    }






    public List<SpendingObj> getSpendingsByCategory(int budgetID, int categoryID){

    List<SpendingObj> spendingList = new ArrayList<SpendingObj>();

        int i = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Spending.SPENDING_TABLE_NAME + " WHERE " +
        Spending.SPENDING_BUDGET_ID  + " = " + budgetID + " AND " + Spending.SPENDING_CATEGORY_ID + " = " + categoryID + " ORDER BY " + Spending.SPENDING_DATE + " DESC", null);

        if(cursor.moveToFirst()){
            do{
                i++;
                SpendingObj spendObj = new SpendingObj();
                spendObj.setID(cursor.getInt(Constants.SPENDING_ID_POSITION));
                spendObj.setTimestamp(Timestamp.valueOf(cursor.getString(Constants.SPENDING_TIMESTAMP_POSITION)));
                //create a date object and convert numerical date in db to date object
                Date date = new Date(cursor.getLong(Constants.SPENDING_DATE_POSITION));
                spendObj.setDate(date);
                spendObj.setBudgetID(cursor.getInt((Constants.SPENDING_BUDGET_ID_POSITION)));
                spendObj.setBudgetName(cursor.getString(Constants.SPENDING_BUDGET_NAME_POSITION));
                spendObj.setCategoryID(cursor.getInt(Constants.SPENDING_CATEGORY_ID_POSITION));
                spendObj.setCategoryName(cursor.getString(Constants.SPENDING_CATEGORY_NAME_POSITION));
                spendObj.setSpent(cursor.getDouble(Constants.SPENDING_SPENT_POSITION));
                spendObj.setDescription(cursor.getString(Constants.SPENDING_DESCRIPTION_POSITION));

                spendingList.add(spendObj);

            }while (cursor.moveToNext());


        }
        //Log.d("list size dbh", Integer.toString(spendingList.size()));
        Log.d("list size dbh", Integer.toString(i));

        if(spendingList.size() > 0){
            return spendingList;
        }
        else {

            spendingList = null;
            return spendingList;
        }

    }


    public ListDataObj createListData(int budgetID){

        ListDataObj ldo = new ListDataObj();

        SQLiteDatabase db = this.getReadableDatabase();

        //get budget name

        Cursor cursor = db.rawQuery("SELECT * FROM " + Budgets.BUDGETS_TABLE_NAME + " WHERE " +
                Budgets._ID  + " = " + budgetID,null);
        cursor.moveToFirst();

        String budgetName = cursor.getString(Constants.BUDGETS_NAME_POSITION);


        //get sum of projected expenses
        double allExpenses;
        cursor = db.rawQuery("SELECT SUM(" + Expenses.EXPENSES_ESTIMATE + ") FROM " +
                Expenses.EXPENSES_TABLE_NAME + " WHERE " + Expenses.EXPENSES_BUDGET_ID + " = " +
                budgetID,null);
        if(cursor.moveToFirst()) {
            allExpenses = cursor.getDouble(0);
        }
        else allExpenses = 0;

        //get sum of all money spent
        double spent;
        cursor = db.rawQuery("SELECT SUM(" + Spending.SPENDING_SPENT + ") FROM " +
                Spending.SPENDING_TABLE_NAME + " WHERE " + Spending.SPENDING_BUDGET_ID + " = " +
                budgetID,null);
        if(cursor.moveToFirst()){
            spent = cursor.getDouble(0);
            Log.d("createlist", String.valueOf(cursor.getFloat(0)));
        }


        else spent = 0;


        //get categories and projected expenses for each category
        List<String> categoryList = new ArrayList<String >(); //list to be added to ldo
        List<Double> expensesList = new ArrayList<Double>(); //list to be added to ldo
        List<Integer> categoryIDList = new ArrayList<Integer>(); //list to keep position for spent list
        categoryList.add(income);
        categoryIDList.add(0);


        expensesList.add(getAllExpenses(budgetID));
        updateIncome(budgetID,getAllExpenses(budgetID));




        cursor = db.rawQuery("SELECT * FROM " + Expenses.EXPENSES_TABLE_NAME + " WHERE " +
        Expenses.EXPENSES_BUDGET_ID + " = " + budgetID + " ORDER BY " + Expenses.EXPENSES_ESTIMATE +
        " DESC",null);

        if(cursor.moveToFirst()){
            do{
                categoryList.add(cursor.getString(Constants.EXPENSES_CATEGORY_NAME_POSITION));
                expensesList.add(cursor.getDouble(Constants.EXPENSES_ESTIMATED_EXPENSE_POSITION));
                categoryIDList.add(cursor.getInt(Constants.EXPENSES_CATEGORY_ID_POSITION));
            } while (cursor.moveToNext());
        }


        //get spending for each category
        List<Double> spentList = getSpentList(budgetID, categoryIDList);

        //add values to list data object
        ldo.setBudgetName(budgetName);
        ldo.setAllExpenses(allExpenses);
        ldo.setTotalSpent(spent);
        ldo.setCategoryIDList(categoryIDList);
        ldo.setCategoryList(categoryList);
        ldo.setExpenseList(expensesList);
        ldo.setSpentList(spentList);

        return ldo;
    }


    private List<Double> getSpentList(int budgetID, List<Integer> categoryIDList){

        //returns a list of total money spent in each category ordered by categoryIDList


        List<Double> spentList = new ArrayList<Double>();
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor = db.rawQuery("SELECT SUM (" + Earning.EARNING_EARNED + ") FROM " +
                Earning.EARNING_TABLE_NAME + " WHERE " + Earning.EARNING_BUDGET_ID + " = " +
                budgetID,null);

        if(cursor.moveToFirst()){
            spentList.add(cursor.getDouble(0));

        }else spentList.add(0.0);




        for(int i = 1; i < categoryIDList.size(); i++){

            cursor = db.rawQuery("SELECT SUM (" + Spending.SPENDING_SPENT + ") FROM " +
                    Spending.SPENDING_TABLE_NAME + " WHERE " + Spending.SPENDING_BUDGET_ID + " = " +
                    budgetID + " AND " + Spending.SPENDING_CATEGORY_ID + " = " +
                    categoryIDList.get(i),null);

            if(cursor.moveToFirst()){
                spentList.add(cursor.getDouble(0));

            }else spentList.add(0.0);

        }

        return spentList;


    }

    //method to return the projected expenses for a category in a budget
    public ExpenseObj getProjectedExpenseForCategory(int categoryID, int budgetID){


        ExpenseObj projectedExpense = new ExpenseObj();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Expenses.EXPENSES_TABLE_NAME + " WHERE " +
        Expenses.EXPENSES_BUDGET_ID + " = " + budgetID + " AND " + Expenses.EXPENSES_CATEGORY_ID +
        " = " + categoryID, null);

        if(cursor.moveToFirst()){
            projectedExpense.setID(cursor.getInt(Constants.EXPENSES_ID_POSITION));
            projectedExpense.setBudgetID(cursor.getInt(Constants.EXPENSES_BUDGET_ID_POSITION));
            projectedExpense.setBudgetName(cursor.getString(Constants.EXPENSES_BUDGET_NAME_POSITION));
            projectedExpense.setCategoryID(cursor.getInt(Constants.EXPENSES_CATEGORY_ID_POSITION));
            projectedExpense.setCategoryName(cursor.getString(Constants.EXPENSES_CATEGORY_NAME_POSITION));
            projectedExpense.setSpent(cursor.getDouble(Constants.EXPENSES_ESTIMATED_EXPENSE_POSITION));


        }else projectedExpense = null;

        return projectedExpense;
    }

    //method to return the total amount spent for a given category in a budget
    public Double getSpentAmountForCategory(int categoryID, int budgetID){


        double spentAmount;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM (" + Spending.SPENDING_SPENT + ") FROM " +
        Spending.SPENDING_TABLE_NAME + " WHERE " + Spending.SPENDING_BUDGET_ID + " = " + budgetID +
        " AND " + Spending.SPENDING_CATEGORY_ID + " = " + categoryID,null);

        if(cursor.moveToFirst()){
            spentAmount = cursor.getDouble(0);
        }else spentAmount = 0.0;

        return spentAmount;

    }

    //returns category name when given categoryID
    public String getCategory(int categoryID){

        String category;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Categories.CATEGORIES_TABLE_NAME + " WHERE " +
        Categories._ID + " = " + categoryID, null);

        if(cursor.moveToFirst()){
            category = cursor.getString(Constants.CATEGORIES_NAME_POSITION);
        }else category = "";
        return category;
    }



    //method to return the projected income for a given budget
    public IncomeObj getProjectedIncome(int budgetID){


        IncomeObj projectedIncome = new IncomeObj();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Income.INCOME_TABLE_NAME + " WHERE " +
                Income.INCOME_BUDGET_ID + " = " + budgetID, null);

        if(cursor.moveToFirst()){
            projectedIncome.setID(cursor.getInt(Constants.INCOME_ID_POSITION));
            projectedIncome.setBudgetID(cursor.getInt(Constants.INCOME_BUDGET_ID_POSITION));
            projectedIncome.setBudgetName(cursor.getString(Constants.INCOME_BUDGET_NAME_POSITION));
            projectedIncome.setIncome(cursor.getDouble(Constants.INCOME_ESTIMATE_POSITION));
        }else projectedIncome = null;


        return projectedIncome;
    }

    //method to return the total amount earned for a given budget
    public Double getEarnedAmount(int budgetID){


        double spentAmount;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM (" + Earning.EARNING_EARNED + ") FROM " +
                Earning.EARNING_TABLE_NAME + " WHERE " + Earning.EARNING_BUDGET_ID + " = " + budgetID,null);

        if(cursor.moveToFirst()){
            spentAmount = cursor.getDouble(0);
        }else spentAmount = 0.0;

        return spentAmount;

    }

    //accepts budgetID and returns a list of all earnings for the budget
    public List<EarningObj> getEarningsList(int budgetID){

        List<EarningObj> earningObjList = new ArrayList<EarningObj>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Earning.EARNING_TABLE_NAME + " WHERE " +
        Earning.EARNING_BUDGET_ID + " = " + budgetID + " ORDER BY " + Earning.EARNING_DATE + " DESC", null);

        if(cursor.moveToFirst()){
            do {

                EarningObj earned = new EarningObj();
                earned.setID(cursor.getInt(Constants.EARNING_ID_POSITION));
                earned.setTimestamp(Timestamp.valueOf(cursor.getString(Constants.EARNING_TIMESTAMP_POSITION)));
                Date date = new Date(cursor.getLong(Constants.EARNING_DATE_POSITION));
                earned.setDate(date);
                earned.setBudgetID(cursor.getInt(Constants.EARNING_BUDGET_ID_POSITION));
                earned.setBudgetName(cursor.getString(Constants.EARNING_BUDGET_NAME_POSITION));
                earned.setEarned(cursor.getDouble(Constants.EARNING_EARNED_POSITION));
                earned.setDescription(cursor.getString(Constants.EARNING_DESCRIPTION_POSITION));

                earningObjList.add(earned);
            }while (cursor.moveToNext());

        }

        return earningObjList;

    }


    //accepts a list of category IDs and returns a list of category objects
    public List<CategoryObj> getCategories(List<Integer> categoryIDList) {

        List<CategoryObj> categoryObjList = new ArrayList<CategoryObj>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;

        for (int i = 0; i < categoryIDList.size(); i++) {

            cursor = db.rawQuery("SELECT * FROM " + Categories.CATEGORIES_TABLE_NAME + " WHERE " +
                    Categories._ID + " = " + categoryIDList.get(i), null);
            if (cursor.moveToFirst()) {
                CategoryObj category = new CategoryObj();
                category.setID(cursor.getInt(Constants.CATEGORIES_ID_POSITION));
                category.setCategoryName(cursor.getString(Constants.CATEGORIES_NAME_POSITION));
                category.setDefaultCategory(cursor.getInt(Constants.CATEGORIES_DEFAULT_POSITION));

                categoryObjList.add(category);
            }
        }

        return categoryObjList;
    }

    //accepts the expense object and updates the table with the new amount spent
    public int updateExpense(ExpenseObj expenseObj){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values that are to be updated
        values.put(Expenses.EXPENSES_ESTIMATE, expenseObj.getSpent());

        //updating row
        return db.update(Expenses.EXPENSES_TABLE_NAME, values, Expenses._ID + " = " + expenseObj.getID(), null);


    }

    //update the estimated income
    public int updateIncome(int budgetID, double estimate){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values that are to be updated
        values.put(Income.INCOME_ESTIMATE, estimate);

        //updating row
        return db.update(Income.INCOME_TABLE_NAME, values, Income._ID + " = " + budgetID, null);


    }

    public int updateSpending(SpendingObj spendingObj){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values that are to be updated
        values.put(Spending.SPENDING_DATE, spendingObj.getDate().getTime());
        values.put(Spending.SPENDING_SPENT, spendingObj.getSpent());
        values.put(Spending.SPENDING_EXPENSE_DESCRIPTION, spendingObj.getDescription());

        //updating row
        return db.update(Spending.SPENDING_TABLE_NAME, values, Spending._ID + " = " + spendingObj.getID(), null);


    }

    public int updateEarning(EarningObj earningObj){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values that are to be updated
        values.put(Earning.EARNING_DATE, earningObj.getDate().getTime());
        values.put(Earning.EARNING_EARNED, earningObj.getEarned());
        values.put(Earning.EARNING_INCOME_DESCRIPTION, earningObj.getDescription());

        //updating row
        return db.update(Earning.EARNING_TABLE_NAME, values, Earning._ID + " = " + earningObj.getID(), null);


    }


    public int deleteSpending(SpendingObj spendingObj){

        SQLiteDatabase db = this.getWritableDatabase();


        //updating row
        return db.delete(Spending.SPENDING_TABLE_NAME, Spending._ID + " = " + spendingObj.getID(), null);


    }

    public int deleteEarning(EarningObj earningObj){

        SQLiteDatabase db = this.getWritableDatabase();


        //updating row
        return db.delete(Earning.EARNING_TABLE_NAME, Earning._ID + " = " + earningObj.getID(), null);


    }

    public double getAllExpenses(int budgetID){

        double allExpenses;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + Expenses.EXPENSES_ESTIMATE + ") FROM " +
                Expenses.EXPENSES_TABLE_NAME + " WHERE " + Expenses.EXPENSES_BUDGET_ID + " = " +
                budgetID,null);
        if(cursor.moveToFirst()) {
            allExpenses = cursor.getDouble(0);
        }
        else allExpenses = 0;

        return allExpenses;

    }





























    public static class Categories implements BaseColumns {
        public static final String CATEGORIES_TABLE_NAME = "Categories";
        public static final String CATEGORIES_CATEGORY = "Categories";
        public static final String CATEGORIES_DEFAULT = "IsDefault";
    }

    public static class Budgets implements BaseColumns {

        public static final String BUDGETS_TABLE_NAME = "Budgets";
        public static final String BUDGETS_NAME = "BudgetName";
        public static final String BUDGETS_TIMESTAMP = "Timestamp";

    }

    public static class Expenses implements BaseColumns {

        public static final String EXPENSES_TABLE_NAME = "Expenses";
        public static final String EXPENSES_BUDGET_ID = "BudgetID";
        public static final String EXPENSES_BUDGET_NAME = "BudgetName";
        public static final String EXPENSES_CATEGORY_ID = "CategoryID";
        public static final String EXPENSES_CATEGORY_NAME = "CategoryName";
        public static final String EXPENSES_ESTIMATE = "EstimatedExpense";

    }

    public static class Income implements BaseColumns{

        public static final String INCOME_TABLE_NAME = "Income";
        public static final String INCOME_BUDGET_ID = "BudgetID";
        public static final String INCOME_BUDGET_NAME = "BudgetName";
        public static final String INCOME_ESTIMATE = "EstimatedIncome";

    }

    public static class Spending implements BaseColumns{

        public static final String SPENDING_TABLE_NAME = "Spending";
        //budget id will be primary key from associated budget in Budgets table
        public static final String SPENDING_BUDGET_ID = "BudgetID";
        public static final String SPENDING_BUDGET_NAME = "BudgetName";
        public static final String SPENDING_TIMESTAMP = "Timestamp";
        public static final String SPENDING_DATE = "Date";
        public static final String SPENDING_CATEGORY_ID = "CategoryID";
        public static final String SPENDING_CATEGORY_NAME = "CategoryName";
        public static final String SPENDING_SPENT = "Spent";
        public static final String SPENDING_EXPENSE_DESCRIPTION = "Description";
    }

    public static class Earning implements BaseColumns{

        public static final String EARNING_TABLE_NAME = "Earning";
        //budget id will be primary key from associated budget in Budgets table
        public static final String EARNING_BUDGET_ID = "BudgetID";
        public static final String EARNING_BUDGET_NAME = "BudgetName";
        public static final String EARNING_TIMESTAMP = "Timestamp";
        public static final String EARNING_DATE = "Date";
        public static final String EARNING_EARNED = "Earned";
        public static final String EARNING_INCOME_DESCRIPTION = "Description";


    }



    private static final String CATEGORIES_CREATE_ENTRIES = "CREATE TABLE " + Categories.CATEGORIES_TABLE_NAME + " (" +
            Categories._ID + " INTEGER PRIMARY KEY," +
            Categories.CATEGORIES_CATEGORY + " TEXT," +
            Categories.CATEGORIES_DEFAULT + " INT)";

    private static final String BUDGETS_CREATE_ENTRIES = "CREATE TABLE " + Budgets.BUDGETS_TABLE_NAME + " (" +
            Budgets._ID + " INTEGER PRIMARY KEY," +
            Budgets.BUDGETS_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
            Budgets.BUDGETS_NAME + " TEXT)";

    private static final String EXPENSES_CREATE_ENTRIES = "CREATE TABLE " + Expenses.EXPENSES_TABLE_NAME +
            " (" + Expenses._ID + " INTEGER PRIMARY KEY," +
            Expenses.EXPENSES_BUDGET_ID + " INT," +
            Expenses.EXPENSES_BUDGET_NAME + " TEXT," +
            Expenses.EXPENSES_CATEGORY_ID + " INT," +
            Expenses.EXPENSES_CATEGORY_NAME + " TEXT," +
            Expenses.EXPENSES_ESTIMATE + " NUM)";

    private static final String INCOME_CREATE_ENTRIES = "CREATE TABLE " + Income.INCOME_TABLE_NAME +
            " (" + Income._ID + " INTEGER PRIMARY KEY," +
            Income.INCOME_BUDGET_ID + " INT," +
            Income.INCOME_BUDGET_NAME + " TEXT," +
            Income.INCOME_ESTIMATE + " NUM)";

    private static final String SPENDING_CREATE_ENTRIES = "CREATE TABLE " +
            Spending.SPENDING_TABLE_NAME + " (" +
            Spending._ID + " INTEGER PRIMARY KEY," +
            Spending.SPENDING_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
            Spending.SPENDING_DATE + " INT," +
            Spending.SPENDING_BUDGET_ID + " INT," +
            Spending.SPENDING_BUDGET_NAME + " TEXT," +
            Spending.SPENDING_CATEGORY_ID + " INT," +
            Spending.SPENDING_CATEGORY_NAME + " TEXT," +
            Spending.SPENDING_SPENT + " NUM," +
            Spending.SPENDING_EXPENSE_DESCRIPTION + " TEXT)";

    private static final String EARNING_CREATE_ENTRIES = "CREATE TABLE " +
            Earning.EARNING_TABLE_NAME + " (" +
            Earning._ID + " INTEGER PRIMARY KEY," +
            Earning.EARNING_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
            Earning.EARNING_DATE + " INT," +
            Earning.EARNING_BUDGET_ID + " INT," +
            Earning.EARNING_BUDGET_NAME + " TEXT," +
            Earning.EARNING_EARNED + " NUM," +
            Earning.EARNING_INCOME_DESCRIPTION + " TEXT)";

}


