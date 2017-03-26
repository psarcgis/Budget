package com.gregrussell.budget;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by greg on 3/16/2017.
 */

public class CategoryObj {


    int _ID;
    String _CategoryName;
    int _DefaultCategory; //1 is default, 0 is not default

    public CategoryObj() {

    }

    public CategoryObj(int _ID, String _CategoryName, int _DefaultCategory) {

        this._ID = _ID;
        this._CategoryName = _CategoryName;
        this._DefaultCategory = _DefaultCategory;


    }


    public int getID() {
        return this._ID;
    }

    public void setID(int id) {
        this._ID = id;
    }

    public String getCategoryName() {
        return this._CategoryName;
    }

    public void setCategoryName(String categoryName) {
        this._CategoryName = categoryName;
    }

    public int getDefaultCategory() {
        return this._DefaultCategory;
    }

    public void setDefaultCategory(int defaultCategory) {
        this._DefaultCategory = defaultCategory;
    }



}
