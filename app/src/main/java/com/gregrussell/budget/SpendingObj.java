package com.gregrussell.budget;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by greg on 3/9/2017.
 */

//class that creates an object that holds each item in a row from the Spending table

public class SpendingObj {


    int _ID;
    Timestamp _Timestamp;
    Date _Date;
    int _BudgetID;
    String _BudgetName;
    int _CategoryID;
    String _CategoryName;
    double _Spent;
    String _Description;

    public SpendingObj(){

    }

    public SpendingObj(int _ID, Timestamp _Timestamp, Date _Date, int _BudgetID, String _BudgetName, int _CategoryID, String _CategoryName, double _Spent, String _Description){

        this. _ID = _ID;
        this. _Timestamp = _Timestamp;
        this._Date = _Date;
        this._BudgetID = _BudgetID;
        this._BudgetName = _BudgetName;
        this._CategoryID = _CategoryID;
        this._CategoryName = _CategoryName;
        this._Spent = _Spent;
        this._Description = _Description;
    }

    public SpendingObj(int _ID, Date _Date, int _BudgetID, String _BudgetName, int _CategoryID, String _CategoryName, double _Spent, String _Description){

        this. _ID = _ID;
        this. _Timestamp = _Timestamp;
        this._Date = _Date;
        this._BudgetID = _BudgetID;
        this._BudgetName = _BudgetName;
        this._CategoryID = _CategoryID;
        this._CategoryName = _CategoryName;
        this._Spent = _Spent;
        this._Description = _Description;
    }

    public SpendingObj(Date _Date, int _BudgetID, String _BudgetName, int _CategoryID, String _CategoryName, double _Spent, String _Description){

        this._Date = _Date;
        this._BudgetID = _BudgetID;
        this._BudgetName = _BudgetName;
        this._CategoryID = _CategoryID;
        this._CategoryName = _CategoryName;
        this._Spent = _Spent;
        this._Description = _Description;
    }


    public int getID(){
        return this._ID;
    }
    public void setID(int id){
        this._ID = id;
    }

    public Timestamp getTimestamp(){
        return this._Timestamp;
    }
    public void setTimestamp(Timestamp timestamp){
        this._Timestamp = timestamp;
    }

    public Date getDate(){
        return this._Date;
    }
    public void setDate(Date date){
        this._Date = date;
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
        return this._Spent;
    }
    public void setSpent(double spent){
        this._Spent = spent;
    }

    public String getDescription(){
        return this._Description;
    }
    public void setDescription(String description){
        this._Description = description;
    }



}
