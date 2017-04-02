package com.gregrussell.budget;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by greg on 3/24/2017.
 */

public class SwipeViews extends Activity {

    private static final int NUM_PAGES = 2;
    public static ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    public static TextView fragTitle;
    public static View topBar;
    public static ImageView optionsIcon;
    public static int swipePosition;
    DataBaseHelperCategory myDBHelper;
    ViewGroup swipeViewsLayoutContainer;
    int longClickPos;


    @Override
    protected void onPause(){

        super.onPause();



    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.swipe_views_layout);

        CurrentBudgetFragment.topBarColor = getResources().getColor(R.color.colorListNeutral);
        swipePosition = 0;
        swipeViewsLayoutContainer = (ViewGroup)findViewById(R.id.swipeContainer);

        //pager allows for swiping between fragments
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SwipeViewsPagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //sliding tab class allows for icon to display which fragment we are on
        SlidingTabLayout slide = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slide.setViewPager(mPager);

        fragTitle = (TextView)findViewById(R.id.fragmentTitleSwipeViews);
        topBar = (View)findViewById(R.id.topBarSwipeViews);
        optionsIcon = (ImageView)findViewById(R.id.optionsIconSwipeViews);
        optionsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("menuClick", "Swipe position " + String.valueOf(swipePosition));
                if(swipePosition ==0) {
                    showMenuPos0(optionsIcon);
                }
                else if(swipePosition == 1){
                    showMenuPos1(optionsIcon);
                }
            }
        });



    }

    public class SwipeViewsPagerAdapter extends FragmentStatePagerAdapter {
        public SwipeViewsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            //display current budget first, display budget list on view to the right
            switch (position) {
                case 0:
                    return new CurrentBudgetFragment();
                case 1:
                    return new BudgetListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        //icons to be use in slidingTab
        private int[] imageResId={R.drawable.circle_fragment_selector,R.drawable.circle_fragment_selector};

        @Override
        public CharSequence getPageTitle(int position) {

            //sets up tabStrip icons/text
            Drawable image = getResources().getDrawable(imageResId[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }

        public int getDrawableId(int position) {
            return imageResId[position];
        }
    }

    public void showMenuPos0(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.om0_delete:
                        Log.d("menuClick", "delete");
                        deleteBudgetDialog();
                        return true;
                    case R.id.om0_rename:
                        Log.d("menuClick", "rename");
                        renameBudgetDialog();
                        return true;
                    case R.id.om0_manage:
                        Log.d("menuClick", "manage");
                        return true;
                    case R.id.om0_analyze:
                        Log.d("menuClick", "analyze");
                    case R.id.om0_about:
                        Log.d("menuClick", "about");
                    default:
                        return false;
                }
            }

        });
        popup.inflate(R.menu.options_menu_0);
        popup.show();


    }

    public void showMenuPos1(View v) {
        PopupMenu popup = new PopupMenu(this, v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.om1_manage:
                        Log.d("menuClick", "manage");
                        return true;
                    case R.id.om0_analyze:
                        Log.d("menuClick", "analyze");
                    case R.id.om1_about:
                        Log.d("menuClick", "about");
                    default:
                        return false;
                }
            }

        });
        popup.inflate(R.menu.options_menu_1);
        popup.show();


    }

    private void deleteBudgetDialog(){

        new AlertDialog.Builder(SwipeViews.this)
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

        LayoutInflater inflater = SwipeViews.this.getLayoutInflater();
        View renameBudgetLayout = inflater.inflate(R.layout.rename_budget_dialog,null);
        final EditText renameBudgetEdit = (EditText)renameBudgetLayout.findViewById(R.id.editTextRenameBudget);

        //create a dialog box to add new category
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SwipeViews.this);

        //set the layout the dialog uses
        alertDialogBuilder.setView(renameBudgetLayout);

        //set up dialog
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                if (String.valueOf(renameBudgetEdit.getText()).trim().isEmpty()) {
                                    new AlertDialog.Builder(SwipeViews.this)
                                            .setTitle("Invalid Budget Name")
                                            .setMessage("Must enter a name for the Budget.")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    renameBudgetDialog();
                                                }
                                            })
                                            .show();
                                }else{
                                    AsyncEditBudget addBudget = new AsyncEditBudget();
                                    Calendar c = Calendar.getInstance();
                                    Timestamp time = new Timestamp(c.getTime().getTime());
                                    BudgetObj budget = new BudgetObj(CurrentBudgetFragment.currentBudget,
                                            time, String.valueOf(renameBudgetEdit.getText()).trim());
                                    addBudget.execute(budget);
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

            deleteBudget(CurrentBudgetFragment.currentBudget);
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
            BudgetListFragment.adapter = new ListViewAdapterAllBudgets(SwipeViews.this,BudgetListFragment.budgetListItemList);
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

        myDBHelper = new DataBaseHelperCategory(SwipeViews.this);

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
                new AlertDialog.Builder(SwipeViews.this)
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
                BudgetListFragment.adapter = new ListViewAdapterAllBudgets(SwipeViews.this, BudgetListFragment.budgetListItemList);
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

        myDBHelper = new DataBaseHelperCategory(SwipeViews.this);

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
                    SwipeViews.topBar.setBackgroundColor(CurrentBudgetFragment.topBarColor);
                    //change color of status bar and overview header
                    if(Build.VERSION.SDK_INT >= 21){
                        Window window = SwipeViews.this.getWindow();

                        // clear FLAG_TRANSLUCENT_STATUS flag:
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        // finally change the color
                        window.setStatusBarColor(ContextCompat.getColor(SwipeViews.this,R.color.colorListGreenDark));
                        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(
                                getResources().getString(R.string.app_name), BitmapFactory.decodeResource(
                                getResources(),R.mipmap.budget_logo), getResources().getColor(R.color.colorListGreen));
                        (SwipeViews.this).setTaskDescription(taskDescription);
                    }
                }
            }else if(roundTotSpent == roundAllExp){
                ovUn = "Even";
                CurrentBudgetFragment.containerLayout.setBackgroundColor(getResources().getColor(R.color.colorListNeutral));
                CurrentBudgetFragment.topBarColor = getResources().getColor(R.color.colorListNeutral);
                if(SwipeViews.swipePosition == 0) {
                    SwipeViews.topBar.setBackgroundColor(CurrentBudgetFragment.topBarColor);

                    //change color of status bar and overview header
                    if(Build.VERSION.SDK_INT >= 21){
                        Window window = SwipeViews.this.getWindow();

                        // clear FLAG_TRANSLUCENT_STATUS flag:
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        // finally change the color
                        window.setStatusBarColor(ContextCompat.getColor(SwipeViews.this,R.color.colorPrimaryDark));
                        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(
                                getResources().getString(R.string.app_name), BitmapFactory.decodeResource(
                                getResources(),R.mipmap.budget_logo), getResources().getColor(R.color.colorPrimary));
                        (SwipeViews.this).setTaskDescription(taskDescription);
                    }
                }
            }else {
                ovUn = "Over";
                CurrentBudgetFragment.containerLayout.setBackgroundColor(getResources().getColor(R.color.colorListRed));
                CurrentBudgetFragment.topBarColor = getResources().getColor(R.color.colorListRed);
                if(SwipeViews.swipePosition == 0) {
                    SwipeViews.topBar.setBackgroundColor(CurrentBudgetFragment.topBarColor);
                    Log.d("changeColor", "change");

                    //change color of status bar and overview header
                    if(Build.VERSION.SDK_INT >= 21){

                        Log.d("changeColor", "change");
                        Window window = SwipeViews.this.getWindow();

                        // clear FLAG_TRANSLUCENT_STATUS flag:
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

                        // finally change the color
                        window.setStatusBarColor(ContextCompat.getColor(SwipeViews.this,R.color.colorListRedDark));
                        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(
                                getResources().getString(R.string.app_name), BitmapFactory.decodeResource(
                                getResources(),R.mipmap.budget_logo),getResources().getColor( R.color.colorListRed));
                        (SwipeViews.this).setTaskDescription(taskDescription);
                    }
                }
            }


            //set text of textViews
            Log.d("budgetname in header", "budget is " + CurrentBudgetFragment.budgetName);
            if(swipePosition == 0) {
                fragTitle.setText(CurrentBudgetFragment.budgetName);
            }else{
                fragTitle.setText(getResources().getText(R.string.allBudgets));
            }
            CurrentBudgetFragment.projectedExpenses.setText(result[0]);
            CurrentBudgetFragment.spent.setText(result[1]);
            CurrentBudgetFragment.difference.setText(result[2]);
            CurrentBudgetFragment.overUnder.setText(ovUn);




            //progress bar is visible by default. Turn invisible once loading is complete
            //View headerLoadingPanel = currentBudgetRootView.findViewById(R.id.headerLoadingPanel);
            //headerLoadingPanel.setVisibility(View.INVISIBLE);


        }


        private String[] populateHeader(){

            Log.d("swipeViews Budget", "Entered PopulateHeader, current budget is " + CurrentBudgetFragment.budgetName);

            //set unusedcategorylist
            CurrentBudgetFragment.unusedCategoryList.clear();
            CurrentBudgetFragment.unusedCategoryList = myDBHelper.getUnusedCategories(CurrentBudgetFragment.currentBudget);
            for(int i =0; i < CurrentBudgetFragment.unusedCategoryList.size(); i++){
                Log.d("allCategoriesNotUsed BL", CurrentBudgetFragment.unusedCategoryList.get(i).getCategoryName());
            }


            ListDataObj listData = myDBHelper.createListData(CurrentBudgetFragment.currentBudget);


            //Debug logs to check that all data is in the list

            Log.d("SwipeViewsListDataObj", "budget name: " + listData.getBudgetName());
            Log.d("SwipeViewsistDataObj", "all expenses:" + listData.getAllExpenses());
            Log.d("SwipeViewsistDataObj", "total spent: " + listData.getTotalSpent());
            Log.d("SwipeViewsistDataObj", "income category check: " + listData.getCategoryList().get(0));
            Log.d("SwipeViewsistDataObj", "projected income: " + listData.getExpenseList().get(0));
            Log.d("SwipeViewsistDataObj", "earned income: " + listData.getSpentList().get(0));

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

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup currentBudgetRootView = (ViewGroup)inflater.inflate(R.layout.activity_main,null);

        @Override
        protected void onPreExecute(){

            View listLoadingPanel = currentBudgetRootView.findViewById(R.id.listLoadingPanel);
            listLoadingPanel.setVisibility(View.VISIBLE);

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
                        Intent intent = new Intent(SwipeViews.this, DisplayIncome.class);
                        startActivity(intent);

                    }else{
                        //an expense category has been selected
                        Intent intent = new Intent(SwipeViews.this, DisplayCategory.class);
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
            adapter = new ListViewAdapter(SwipeViews.this,listData);

        }
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

            myDBHelper = new DataBaseHelperCategory(SwipeViews.this);

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
            new AlertDialog.Builder(SwipeViews.this)
                    .setCancelable(false)
                    .setTitle("No Budgets Found")
                    .setMessage("Create a budget to continue")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(SwipeViews.this,AddBudget.class);
                            startActivity(intent);
                        }
                    })

                    .show();
            //myDBHelper.addBudget("March 2017");
            //LoadBudget();
        }



    }





}



