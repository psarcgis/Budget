package com.gregrussell.budget;

import android.app.Fragment;
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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by greg on 3/24/2017.
 */

public class BudgetListFragment extends Fragment{

    DataBaseHelperCategory myDBHelper;
    Context context;
    ViewGroup rootView;
    FloatingActionButton addBudgetButton;
    public static ListViewAdapterAllBudgets adapter;
    public static ListView budgetListView;
    public static List<BudgetListItemObj> budgetListItemList = new ArrayList<BudgetListItemObj>();
    int longClickPos;

    @Override
    public void onResume(){
        super.onResume();

        Log.d("BudgetListFragment", "on resume " + SwipeViews.swipePosition);
        if(SwipeViews.swipePosition == 1) {
            SwipeViews.fragTitle.setText(getResources().getText(R.string.all_budgets));
        }
        AsyncLoadList loadListTask = new AsyncLoadList();
        loadListTask.execute();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        context = getActivity();
        rootView = (ViewGroup) inflater.inflate(R.layout.all_budgets_fragment_layout, container, false);

        addBudgetButton = (FloatingActionButton)rootView.findViewById(R.id.addAllBudgetsFragment);
        addBudgetButton.setVisibility(View.INVISIBLE);
        addBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBudget();
            }
        });


        AsyncLoadList loadListTask = new AsyncLoadList();
        loadListTask.execute();

        Log.d("BudgetListFragment", "Fragment loaded");
        return rootView;
    }

    private class AsyncLoadList extends AsyncTask<Void,Void,Boolean> {






        @Override
        protected void onPreExecute(){

            View listLoadingPanel = rootView.findViewById(R.id.listLoadingPanelAllBudgetsFragment);
            listLoadingPanel.setVisibility(View.VISIBLE);
            addBudgetButton.setVisibility(View.INVISIBLE);
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            Log.d("budgetList", "do in background");
            PopulateList();
            return null;
        }



        @Override
        protected void onPostExecute(Boolean result){
            //progress bar is visible by default. Turn invisible once loading is complete
            View listLoadingPanel = rootView.findViewById(R.id.listLoadingPanelAllBudgetsFragment);
            listLoadingPanel.setVisibility(View.INVISIBLE);
            addBudgetButton.setVisibility(View.VISIBLE);

            Log.d("listView budgetlist", "listViewAdapter set");

            budgetListView.setAdapter(adapter);

            //set listener for list item click
            budgetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick budget list", "click " + String.valueOf(position));
                    //On click change current budget
                    CurrentBudgetFragment.currentBudget = budgetListItemList.get(position).getBudgetID();
                    Log.d("budgelistonclick", "current budget is " + CurrentBudgetFragment.currentBudget + " " +
                    budgetListItemList.get(position).getBudgetName());
                    AsyncLoadList loadList = new AsyncLoadList();
                    loadList.execute();
                    AsyncCurrentBudgetLoadHeader loadHeader = new AsyncCurrentBudgetLoadHeader();
                    loadHeader.execute();
                    AsyncCurrentBudgetLoadList loadCurrentBudgetList = new AsyncCurrentBudgetLoadList();
                    loadCurrentBudgetList.execute();
                }
            });
            registerForContextMenu(budgetListView);
            budgetListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    longClickPos = position;
                    return false;

                }

            });
        }

        private void PopulateList(){

            myDBHelper = new DataBaseHelperCategory(context);
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
            Calendar c = Calendar.getInstance();
            Timestamp time = new Timestamp(c.getTime().getTime());
            myDBHelper.updateBudgetTimestamp(CurrentBudgetFragment.currentBudget,time);
            Log.d("budget","time stamp update budget is " + CurrentBudgetFragment.currentBudget);

            Log.d("budgetlist lvadapter", "PopulateList method is running yay");

            budgetListItemList = myDBHelper.getAllBudgetsList();
            try{Log.d("budgetlistonclick", "budgetlistitem " + budgetListItemList.get(0).getBudgetName());
                CurrentBudgetFragment.budgetName = budgetListItemList.get(0).getBudgetName();
            }catch(Exception e){
                CurrentBudgetFragment.budgetName = "";
            }


            Log.d("budgetlist lvadapter", "getallbudgets " + budgetListItemList.size());


            //set listview adapter
            View view = rootView.findViewById(R.id.listViewFrameAllBudgetsFragment);
            budgetListView = (ListView)view.findViewById(R.id.listViewAllBudgetsFragment);
            adapter = new ListViewAdapterAllBudgets(context,budgetListItemList);
        }
    }



    private class AsyncCurrentBudgetLoadHeader extends AsyncTask<Void,Void,String[]>{

        double diff;
        double allExp;
        double totSpent;





        @Override
        protected String[] doInBackground(Void... params) {

            return populateHeader();
        }



        @Override
        protected void onPostExecute(String[] result){

            //set text from string[] result that is returned by doInBackground

            String ovUn;



            //round because double doesn't know how to math
            double roundTotSpent = Math.round(totSpent *100.0)/100.0;
            double roundAllExp = Math.round(allExp *100.0)/100.0;

            if(roundTotSpent < roundAllExp){
                ovUn = "Under";
                CurrentBudgetFragment.containerLayout.setBackgroundColor(getResources().getColor(R.color.colorListGreen));
                CurrentBudgetFragment.topBarColor = getResources().getColor(R.color.colorListGreen);
                if(SwipeViews.swipePosition == 0) {
                    SwipeViews.topBar.setBackgroundColor(getResources().getColor(R.color.colorListGreen));
                }
            }else if(roundTotSpent == roundAllExp){
                ovUn = "Even";
                CurrentBudgetFragment.containerLayout.setBackgroundColor(getResources().getColor(R.color.colorListNeutral));
                CurrentBudgetFragment.topBarColor = getResources().getColor(R.color.colorListNeutral);
                if(SwipeViews.swipePosition == 0) {
                    SwipeViews.topBar.setBackgroundColor(getResources().getColor(R.color.colorListNeutral));
                }
            }else {
                ovUn = "Over";
                CurrentBudgetFragment.containerLayout.setBackgroundColor(getResources().getColor(R.color.colorListRed));
                CurrentBudgetFragment.topBarColor = getResources().getColor(R.color.colorListRed);
                if(SwipeViews.swipePosition == 0) {
                    SwipeViews.topBar.setBackgroundColor(getResources().getColor(R.color.colorListRed));
                }
            }


            //set text of textViews
            Log.d("budgetname in header", "budget is " + CurrentBudgetFragment.budgetName);
            CurrentBudgetFragment.budgetNameText.setText(CurrentBudgetFragment.budgetName);
            CurrentBudgetFragment.projectedExpenses.setText(result[0]);
            CurrentBudgetFragment.spent.setText(result[1]);
            CurrentBudgetFragment.difference.setText(result[2]);
            CurrentBudgetFragment.overUnder.setText(ovUn);




            //progress bar is visible by default. Turn invisible once loading is complete
            //View headerLoadingPanel = currentBudgetRootView.findViewById(R.id.headerLoadingPanel);
            //headerLoadingPanel.setVisibility(View.INVISIBLE);


        }


        private String[] populateHeader(){

            Log.d("listDataObj Budget", "Entered PopulateHeader, current budget is " + CurrentBudgetFragment.budgetName + " " + CurrentBudgetFragment.currentBudget);

            //set unusedcategorylist
            CurrentBudgetFragment.unusedCategoryList.clear();
            CurrentBudgetFragment.unusedCategoryList = myDBHelper.getUnusedCategories(CurrentBudgetFragment.currentBudget);
            for(int i =0; i < CurrentBudgetFragment.unusedCategoryList.size(); i++){
                Log.d("allCategoriesNotUsed BL", CurrentBudgetFragment.unusedCategoryList.get(i).getCategoryName());
            }


            ListDataObj listData = myDBHelper.createListData(CurrentBudgetFragment.currentBudget);


            //Debug logs to check that all data is in the list

            Log.d("listDataObj", "budget name: " + listData.getBudgetName());
            Log.d("listDataObj", "all expenses:" + listData.getAllExpenses());
            Log.d("listDataObj", "total spent: " + listData.getTotalSpent());
            Log.d("listDataObj", "income category check: " + listData.getCategoryList().get(0));
            Log.d("listDataObj", "projected income: " + listData.getExpenseList().get(0));
            Log.d("listDataObj", "earned income: " + listData.getSpentList().get(0));

            for(int i = 1; i < listData.getCategoryList().size(); i++){
                Log.d("listDataObj", "categories after income are: " + i + ". " + listData.getCategoryList().get(i));
            }
            for(int i = 1; i < listData.getCategoryList().size(); i++){
                Log.d("listDataObj", "estimated expenses after income are: " + i + ". " + listData.getExpenseList().get(i));
            }
            for(int i = 1; i < listData.getCategoryList().size(); i++){
                Log.d("listDataObj", "spent for each category after income are: " + i + ". " + listData.getSpentList().get(i));
            }

            //pass data from list to objects
            CurrentBudgetFragment.budgetName = listData.getBudgetName();
            allExp = listData.getAllExpenses();
            totSpent = listData.getTotalSpent();
            Log.d("populateheader", "listdata spent is " + listData.getTotalSpent());
            diff = totSpent - allExp;






            //formatter to convert double under 1000 to currency (only for difference text view)
            DecimalFormat lowFmt = new DecimalFormat("+$#,##0.00;-$#,##0.00");

            //formatter to convert double over 1000 to currency (only for difference text view)
            DecimalFormat highFmt = new DecimalFormat("+$#,##0.0;-$#,##0.0");

            //standard currency format
            NumberFormat fmt = NumberFormat.getCurrencyInstance();

            String fmtAllExp = fmt.format(allExp);
            String fmtTotSpent = fmt.format(totSpent);
            String fmtDiff;



            //create shortened format of difference
            if(Math.abs(diff) < 1000){

                fmtDiff = lowFmt.format(diff);
            }else if(Math.abs(diff) >= 1000 && Math.abs(diff) < 1000000 ){

                diff = diff / 1000;
                fmtDiff = highFmt.format(diff) + "K";
            }else if (Math.abs(diff) >= 1000000 && Math.abs(diff) < 1000000000){

                diff = diff / 1000000;
                fmtDiff = highFmt.format(diff) + "M";
            }else{

                diff = diff / 1000000000;
                fmtDiff = highFmt.format(diff) + "B";
            }





            //String Array to return
            String[] results = {fmtAllExp,fmtTotSpent,fmtDiff};
            return results;



        }

    }


    private class AsyncCurrentBudgetLoadList extends AsyncTask<Void,Void,Boolean>{


        ListViewAdapter adapter;
        ListDataObj listData;

        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup currentBudgetRootView = (ViewGroup)inflater.inflate(R.layout.activity_main,null);

        @Override
        protected void onPreExecute(){

            View listLoadingPanel = currentBudgetRootView.findViewById(R.id.listLoadingPanel);
            listLoadingPanel.setVisibility(View.VISIBLE);
            addBudgetButton.setVisibility(View.INVISIBLE);
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            PopulateList();
            return null;
        }



        @Override
        protected void onPostExecute(Boolean result){
            //progress bar is visible by default. Turn invisible once loading is complete
            View listLoadingPanel = currentBudgetRootView.findViewById(R.id.listLoadingPanel);
            listLoadingPanel.setVisibility(View.INVISIBLE);
            addBudgetButton.setVisibility(View.VISIBLE);

            Log.d("listView", "listViewAdapter set");


            adapter.notifyDataSetChanged();
            CurrentBudgetFragment.listView.setAdapter(null);
            CurrentBudgetFragment.listView.setAdapter(adapter);

            //set listener for list item click
            CurrentBudgetFragment.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Log.d("onClick", "click " + String.valueOf(position));
                    //use position to get the category from listData
                    int category = listData.getCategoryIDList().get(position);

                    //must check if first position, income category handled differently
                    if(position == 0){
                        //income category has been selected
                        Intent intent = new Intent(context, DisplayIncome.class);
                        startActivity(intent);

                    }else{
                        //an expense category has been selected
                        Intent intent = new Intent(context, DisplayCategory.class);
                        intent.putExtra(CurrentBudgetFragment.EXTRA_MESSAGE_TO_DISPLAY_CATEGORY, category);
                        startActivity(intent);
                    }


                }
            });






        }



        private void PopulateList(){

            Log.d("listViewAdapter", "PopulateList method is running yay");
            listData = myDBHelper.createListData(CurrentBudgetFragment.currentBudget);


            CategoryObj c = new CategoryObj(100, "ReNTs", 0);
            Log.d("uniqueCategory",  String.valueOf(myDBHelper.checkCategoryName(c)));

            //set listview adapter
            View view = currentBudgetRootView.findViewById(R.id.listViewFrame);
            adapter = new ListViewAdapter(context,listData);

        }
    }


    private void addBudget(){


        Intent intent = new Intent(context,AddBudget.class);
        startActivity(intent);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.budget_long_click_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.blcm_rename:
                Log.d("longClickMenuClick", "blf rename position " + longClickPos);
                renameBudgetDialog();
                return true;
            case R.id.blcm_delete:
                Log.d("longClickMenuClick", "blf delete position " + longClickPos);
                deleteBudgetDialog();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }



    private void deleteBudgetDialog(){

        new AlertDialog.Builder(context)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to permanently delete this budget?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        AsyncDeleteBudget deleteBudgetTask = new AsyncDeleteBudget();
                        deleteBudgetTask.execute(new BudgetObj());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                    }

                })
                .show();
    }

    private void renameBudgetDialog(){

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View renameBudgetLayout = inflater.inflate(R.layout.rename_budget_dialog,null);
        final EditText renameBudgetEdit = (EditText)renameBudgetLayout.findViewById(R.id.editTextRenameBudget);

        //create a dialog box to add new category
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        //set the layout the dialog uses
        alertDialogBuilder.setView(renameBudgetLayout);

        //set up dialog
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if (String.valueOf(renameBudgetEdit.getText()).trim().isEmpty()) {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Invalid Budget Name")
                                            .setMessage("Must enter a name for the Budget.")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    renameBudgetDialog();
                                                }
                                            })
                                            .show();
                                }else{
                                    AsyncEditBudget editBudgetTask = new AsyncEditBudget();
                                    Calendar c = Calendar.getInstance();
                                    Timestamp time = new Timestamp(c.getTime().getTime());

                                    //get budget that was long clicked
                                    int longClickedBudget = budgetListItemList.get(longClickPos).getBudgetID();
                                    BudgetObj budget = new BudgetObj(longClickedBudget,
                                            time, String.valueOf(renameBudgetEdit.getText()).trim());
                                    editBudgetTask.execute(budget);
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

        alertDialog.show();
    }

    //background task to delete a budget
    private class AsyncDeleteBudget extends AsyncTask<BudgetObj,Void,Boolean> {

        @Override
        protected Boolean doInBackground(BudgetObj... params) {

            //get budget that was long clicked
            int longClickedBudget = budgetListItemList.get(longClickPos).getBudgetID();

            deleteBudget(longClickedBudget);
            BudgetListFragment.budgetListItemList = getBudgetList();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean deleted){


            Log.d("deleteBudget", String.valueOf(deleted));
            //the next newest budget
            AsyncLoadBudget loadBudget = new AsyncLoadBudget();
            loadBudget.execute();

            //setting up the adapter for allBudgets listView
            BudgetListFragment.adapter = new ListViewAdapterAllBudgets(context,BudgetListFragment.budgetListItemList);
            BudgetListFragment.adapter.notifyDataSetChanged();

            //setting the adapater onto the listView
            BudgetListFragment.budgetListView.setAdapter(null);
            BudgetListFragment.budgetListView.setAdapter(BudgetListFragment.adapter);

            //background tasks to display the current budget
            AsyncCurrentBudgetLoadHeader loadHeader = new AsyncCurrentBudgetLoadHeader();
            loadHeader.execute();
            AsyncCurrentBudgetLoadList loadList = new AsyncCurrentBudgetLoadList();
            loadList.execute();

        }
    }



    //method called to delete the budget
    private void deleteBudget(int budget){

        myDBHelper = new DataBaseHelperCategory(context);

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

        myDBHelper.deleteBudget(budget);

    }


    //background task to delete a budget
    private class AsyncEditBudget extends AsyncTask<BudgetObj,Void,Boolean> {

        @Override
        protected Boolean doInBackground(BudgetObj... params) {

            if(editBudget(params[0])){
                BudgetListFragment.budgetListItemList = getBudgetList();
                return true;
            }else{
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean renamed){


            Log.d("editBudget", String.valueOf(renamed));

            //call the dialog again if couldn't rename
            if(!renamed){
                new AlertDialog.Builder(context)
                        .setTitle("Invalid Budget Name")
                        .setMessage("The budget name entered already exists. Please enter a unique name.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                renameBudgetDialog();
                            }
                        })
                        .show();
            }else {

                //the next newest budget
                AsyncLoadBudget loadBudget = new AsyncLoadBudget();
                loadBudget.execute();

                //setting up the adapter for allBudgets listView
                BudgetListFragment.adapter = new ListViewAdapterAllBudgets(context, BudgetListFragment.budgetListItemList);
                BudgetListFragment.adapter.notifyDataSetChanged();

                //setting the adapater onto the listView
                BudgetListFragment.budgetListView.setAdapter(null);
                BudgetListFragment.budgetListView.setAdapter(BudgetListFragment.adapter);

                //background tasks to display the current budget
                AsyncCurrentBudgetLoadHeader loadHeader = new AsyncCurrentBudgetLoadHeader();
                loadHeader.execute();
                AsyncCurrentBudgetLoadList loadList = new AsyncCurrentBudgetLoadList();
                loadList.execute();
            }

        }
    }



    //method called to delete the budget
    private boolean editBudget(BudgetObj budgetObj){

        myDBHelper = new DataBaseHelperCategory(context);

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

        if(myDBHelper.checkBudgetName(budgetObj.getBudgetName())){
            myDBHelper.updateBudgetName(budgetObj);
            return true;
        }
        else{
            return false;
        }


    }

    //method to update list of all budgets used to populate BudgetListFragment
    private List<BudgetListItemObj> getBudgetList(){

        return myDBHelper.getAllBudgetsList();

    }

    private class AsyncLoadBudget extends AsyncTask<Void,Void, Boolean> {




        @Override
        protected Boolean doInBackground(Void... params) {

            return  LoadBudget();
            //because you have to return something to onPostExecute

        }

        @Override
        protected void onPostExecute(Boolean result){

            if(!result){
                CurrentBudgetFragment.listLoadingPanel.setVisibility(View.VISIBLE);
                CurrentBudgetFragment.headerLoadingPanel.setVisibility(View.VISIBLE);
                createBudget();
            }

        }


        private boolean LoadBudget(){

            Log.d("LoadBudget swipeViews", "made it to load budget");
            //compare most recent spending timestamp and earning timestamp to find most recent budget
            // to load

            int recentBudget;

            myDBHelper = new DataBaseHelperCategory(context);

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


            //myDBHelper.checkCategoryName(categoryObj);

            //myDBHelper.addCategory("Dating", false);




            recentBudget = myDBHelper.getMostRecentBudget();

            if(recentBudget == -1){
                return false;
                //createBudget();
            }else {
                CurrentBudgetFragment.currentBudget = recentBudget;
                Calendar c = Calendar.getInstance();
                Timestamp time = new Timestamp(c.getTime().getTime());
                myDBHelper.updateBudgetTimestamp(CurrentBudgetFragment.currentBudget,time);
                Log.d("loadBudgetSwipeViews", "recent budget is " + CurrentBudgetFragment.currentBudget);
                CurrentBudgetFragment.unusedCategoryList.clear();
                CurrentBudgetFragment.unusedCategoryList = myDBHelper.getUnusedCategories(CurrentBudgetFragment.currentBudget);
                for(int i =0; i < CurrentBudgetFragment.unusedCategoryList.size(); i++){
                    Log.d("allCategoriesNotUsed CB", CurrentBudgetFragment.unusedCategoryList.get(i).getCategoryName());
                }
                return true;


            }





        }

        private void createBudget(){

            Log.d("createBudget", "Entered Create Budget SwipeViews");
            new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("No Budgets Found")
                    .setMessage("Create a budget to continue")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(context,AddBudget.class);
                            startActivity(intent);
                        }
                    })

                    .show();
            //myDBHelper.addBudget("March 2017");
            //LoadBudget();
        }



    }




}
