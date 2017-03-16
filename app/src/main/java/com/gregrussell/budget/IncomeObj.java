package com.gregrussell.budget;

/**
 * Created by greg on 3/16/2017.
 */

public class IncomeObj {


    int _ID;
    int _BudgetID;
    String _BudgetName;
    double _Income;


    public IncomeObj(){

    }

    public IncomeObj(int _ID, int _BudgetID, String _BudgetName, double _Income){

        this. _ID = _ID;
        this._BudgetID = _BudgetID;
        this._BudgetName = _BudgetName;
        this._Income = _Income;

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


    public double getSpent(){
        return this._Income;
    }
    public void setSpent(double income){
        this._Income = income;
    }


}