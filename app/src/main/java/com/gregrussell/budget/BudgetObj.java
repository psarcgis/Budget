package com.gregrussell.budget;

import java.sql.Timestamp;

/**
 * Created by greg on 3/16/2017.
 */

public class BudgetObj {


    int _ID;
    Timestamp _Timestamp;
    String _BudgetName;

    public BudgetObj() {

    }

    public BudgetObj(int _ID, Timestamp _Timestamp, String _BudgetName) {

        this._ID = _ID;
        this._Timestamp = _Timestamp;
        this._BudgetName = _BudgetName;



    }


    public int getID() {
        return this._ID;
    }

    public void setID(int id) {
        this._ID = id;
    }

    public Timestamp getTimestamp() {
        return this._Timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this._Timestamp = timestamp;
    }

    public String getBudgetName() {
        return this._BudgetName;
    }

    public void setBudgetName(String name) {
        this._BudgetName = name;
    }



}