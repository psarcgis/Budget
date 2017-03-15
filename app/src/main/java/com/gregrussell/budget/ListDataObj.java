package com.gregrussell.budget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 3/11/2017.
 */

//for creating an object that contains necessary data to populate listView

public class ListDataObj {

    String budgetName;
    double allExpenses;
    double totalSpent;
    List<Integer>categoryIDList = new ArrayList<Integer>();
    List<String>categoryList = new ArrayList<String>();
    List<Double>expensesList = new ArrayList<Double>();
    List<Double>spentList = new ArrayList<Double>();

    public ListDataObj(){

    }

    public ListDataObj(String budgetName, double allExpenses, double totalSpent,
                       List<Integer>categoryIDList, List<String> categoryList,
                       List<Double> expensesList, List<Double> spentList){

        this.budgetName = budgetName;
        this.allExpenses = allExpenses;
        this.totalSpent = totalSpent;
        this.categoryIDList = categoryIDList;
        this.categoryList = categoryList;
        this.expensesList = expensesList;
        this.spentList = spentList;

    }




    public String getBudgetName(){
        return this.budgetName;
    }
    public void setBudgetName(String budgetName){
        this.budgetName = budgetName;
    }

    public Double getAllExpenses(){
        return this.allExpenses;
    }
    public void setAllExpenses(Double allExpenses){
        this.allExpenses = allExpenses;
    }

    public Double getTotalSpent(){
        return this.totalSpent;
    }
    public void setTotalSpent(Double totalSpent){
        this.totalSpent = totalSpent;
    }

    public List<Integer> getCategoryIDList(){
        return this.categoryIDList;
    }
    public void setCategoryIDList(List<Integer> categoryIDList){
        this.categoryIDList = categoryIDList;
    }

    public List<String> getCategoryList(){
        return this.categoryList;
    }
    public void setCategoryList(List<String> categoryList){
        this.categoryList = categoryList;
    }

    public List<Double> getExpenseList(){
        return this.expensesList;
    }
    public void setExpenseList(List<Double> expensesList){
        this.expensesList = expensesList;
    }

    public List<Double> getSpentList(){
        return this.spentList;
    }
    public void setSpentList(List<Double> spentList){
        this.spentList = spentList;
    }







}
