package com.gregrussell.budget;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by greg on 3/16/2017.
 */

public class ExpenseObj {


    int _ID;
    int _BudgetID;
    String _BudgetName;
    int _CategoryID;
    String _CategoryName;
    double _Expenses;


    public ExpenseObj(){

    }

    public ExpenseObj(int _ID, int _BudgetID, String _BudgetName, int _CategoryID, String _CategoryName, double _Expenses){

        this. _ID = _ID;
        this._BudgetID = _BudgetID;
        this._BudgetName = _BudgetName;
        this._CategoryID = _CategoryID;
        this._CategoryName = _CategoryName;
        this._Expenses = _Expenses;

    }


    public int getID(){
        return this._ID;
    }
    public void setID(int id){
        this._ID = id;
    }


    public int getBudgetID(){
        return this._BudgetID;
    }
    public void setBudgetID(int id){
        this._BudgetID = id;
    }

    public String getBudgetName(){
        return this._BudgetName;
    }
    public void setBudgetName(String name){
        this._BudgetName = name;
    }

    public int getCategoryID(){
        return this._CategoryID;
    }
    public void setCategoryID(int id){
        this._CategoryID = id;
    }

    public String getCategoryName(){
        return this._CategoryName;
    }
    public void setCategoryName(String name){
        this._CategoryName = name;
    }

    public double getSpent(){
        return this._Expenses;
    }
    public void setSpent(double expenses){
        this._Expenses = expenses;
    }


}