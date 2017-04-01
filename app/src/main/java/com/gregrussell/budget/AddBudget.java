package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 3/31/2017.
 */

public class AddBudget extends Activity {

    public static ListView listViewCategoriesAdd;
    public static List<CategoryObj> usedCategoriesList;
    public static List<CategoryObj> unusedCategoriesList;
    DataBaseHelperCategory myDBHelper;
    ImageView addCategoryButton;
    int spinnerPosition;
    EditText editTextBudgetName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_budget);
        listViewCategoriesAdd = (ListView) findViewById(R.id.listViewAddBudget);
        addCategoryButton = (ImageView) findViewById(R.id.addCategoryAddNewBudget);
        editTextBudgetName = (EditText)findViewById(R.id.editTextAddNewBudget);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });
        AsyncGetLists task = new AsyncGetLists();
        task.execute();

        Button createBudgetButton = (Button)findViewById(R.id.createAddNewBudget);
        createBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBudget();
            }
        });
        ImageView backArrow = (ImageView)findViewById(R.id.backButtonAddNewBudget);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private class AsyncGetLists extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            usedCategoriesList = getUsedList();
            unusedCategoriesList = getUnusedList();

            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            ListViewAdapterAddBudget adapter = new ListViewAdapterAddBudget(AddBudget.this, usedCategoriesList);
            listViewCategoriesAdd.setAdapter(adapter);
        }
    }

    private List<CategoryObj> getUsedList() {

        myDBHelper = new DataBaseHelperCategory(AddBudget.this);
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
        List<CategoryObj> usedList = myDBHelper.getDefaultCategories();
        return usedList;
    }

    private List<CategoryObj> getUnusedList() {

        myDBHelper = new DataBaseHelperCategory(AddBudget.this);
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
        List<CategoryObj> unusedList = myDBHelper.getOtherCategories();
        return unusedList;
    }

    private void addCategory() {

        /**Bring up dialog x
         * allow user to pick from already created category that's not being used x
         * or
         * name new category - MUST ENTER A NAME x
         * decide if category will become a default - not default by default x
         * expense $0.00 by default
         *
         */

        LayoutInflater inflater = this.getLayoutInflater();
        final View addCategoryDialog = inflater.inflate(R.layout.add_category_dialog_layout, null);
        final CheckBox checkBox = (CheckBox) addCategoryDialog.findViewById(R.id.checkBoxAddCategoryDialog);
        final RadioButton radioUseExisting = (RadioButton) addCategoryDialog.findViewById(R.id.radioUseExistingAddCategoryDialog);
        final RadioButton radioAddNew = (RadioButton) addCategoryDialog.findViewById(R.id.radioAddNewAddCategoryDialog);
        final Spinner categorySpinner = (Spinner) addCategoryDialog.findViewById(R.id.spinnerAddCategoryDialog);
        final EditText categoryNameEditText = (EditText) addCategoryDialog.findViewById(R.id.editTextNameAddCategoryDialog);


        //create a dialog box to add new category
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        //set the layout the dialog uses
        alertDialogBuilder.setView(addCategoryDialog);

        //set up dialog
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Log.d("spinnerPosition", String.valueOf(spinnerPosition));
                                if (!radioAddNew.isChecked() && !radioUseExisting.isChecked()) {
                                    new AlertDialog.Builder(AddBudget.this)
                                            .setTitle("Invalid Category Name")
                                            .setMessage("Must select or enter a name for the new category.")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addCategory();
                                                }
                                            })
                                            .show();
                                } else if (radioAddNew.isChecked() && String.valueOf(categoryNameEditText.getText()).trim().isEmpty()) {
                                    new AlertDialog.Builder(AddBudget.this)
                                            .setTitle("Invalid Category Name")
                                            .setMessage("Must select or enter a name for the new category.")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addCategory();
                                                }
                                            })
                                            .show();
                                } else if (radioAddNew.isChecked()) {
                                    CategoryObj categoryObj = new CategoryObj();
                                    int isDefault;
                                    if (checkBox.isChecked()) {
                                        isDefault = 1;
                                    } else isDefault = 0;
                                    categoryObj.setCategoryName(String.valueOf(categoryNameEditText.getText()).trim());
                                    categoryObj.setDefaultCategory(isDefault);
                                    usedCategoriesList.add(categoryObj);
                                    Log.d("categoriesListNew","usedCategories size " + usedCategoriesList.size());
                                    Log.d("categoriesListNew","unusedCategories size " + unusedCategoriesList.size());
                                    ListViewAdapterAddBudget adapter = new ListViewAdapterAddBudget(AddBudget.this, usedCategoriesList);
                                    listViewCategoriesAdd.setAdapter(null);
                                    listViewCategoriesAdd.setAdapter(adapter);
                                } else if (spinnerPosition >= unusedCategoriesList.size()) {
                                    new AlertDialog.Builder(AddBudget.this)
                                            .setTitle("Invalid Category Name")
                                            .setMessage("Must select or enter a name for the new category.")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addCategory();
                                                }
                                            })
                                            .show();
                                } else {
                                    CategoryObj categoryObj = unusedCategoriesList.get(spinnerPosition);
                                    Log.d("existingCategory", categoryObj.getCategoryName());
                                    usedCategoriesList.add(categoryObj);
                                    Log.d("categoriesListNew","usedCategories size " + usedCategoriesList.size());
                                    Log.d("categoriesListNew","unusedCategories size " + unusedCategoriesList.size());
                                    ListViewAdapterAddBudget adapter = new ListViewAdapterAddBudget(AddBudget.this, usedCategoriesList);
                                    unusedCategoriesList.remove(spinnerPosition);
                                    listViewCategoriesAdd.setAdapter(null);
                                    listViewCategoriesAdd.setAdapter(adapter);
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
        final AlertDialog alertDialog = alertDialogBuilder.create();

        //disable spinner and existing radio button if no existing categories, check radioAddNew
        if (unusedCategoriesList.size() == 0) {
            radioUseExisting.setEnabled(false);
            categorySpinner.setEnabled(false);
            radioAddNew.setChecked(true);
        }

        //setting what happens on click of radio buttons, spinner, and edit text
        radioAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check radioAddNew, uncheck radioUseExisting, give focus to edit text, make
                // keyboard appear
                checkBox.setVisibility(View.VISIBLE);
                radioAddNew.setChecked(true);
                radioUseExisting.setChecked(false);
                categoryNameEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });
        radioUseExisting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //uncheck radioAddNew, check radioUseExisting take focus from edit text, make
                // keyboard disappear, hide checkbox
                checkBox.setVisibility(View.INVISIBLE);
                radioAddNew.setChecked(false);
                radioUseExisting.setChecked(true);
                categoryNameEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(categoryNameEditText.getWindowToken(), 0);
            }
        });
        categoryNameEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check radioAddNew, uncheck radioUseExisting
                radioAddNew.setChecked(true);
                radioUseExisting.setChecked(false);
                checkBox.setVisibility(View.VISIBLE);
            }
        });
        categoryNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //when gains focus, check radioAddNew, uncheck radioUseExisting
                    radioAddNew.setChecked(true);
                    radioUseExisting.setChecked(false);
                    checkBox.setVisibility(View.VISIBLE);
                }
            }
        });
        categorySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //uncheck radioAddNew, check radioUseExisting, take focus from edit text, make
                // keyboard disappear, hide checkbox
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    radioAddNew.setChecked(false);
                    radioUseExisting.setChecked(true);
                    categoryNameEditText.clearFocus();
                    checkBox.setVisibility(View.INVISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(categoryNameEditText.getWindowToken(), 0);
                }
                return false;
            }
        });

        //adapter for categorySpinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout_add_category_dialog) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(R.id.spinnerLayoutTextView)).setText("");

                    //set the hint for the spinner
                    ((TextView) v.findViewById(R.id.spinnerLayoutTextView)).setHint(getItem(getCount()));
                }
                return v;
            }


            @Override
            public int getCount() {

                //last item in adapter will be hint, which shouldn't be displayed in drop down
                return super.getCount() - 1;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //add categories in unusedCategoryList
        for (int i = 0; i < unusedCategoriesList.size(); i++) {
            spinnerAdapter.add(unusedCategoriesList.get(i).getCategoryName());
        }

        //add hint to end of adapter
        //adapter.add("Test");
        if (unusedCategoriesList.size() > 0) {
            spinnerAdapter.add(this.getResources().getString(R.string.spinnerAddCategoryHint));
        } else {

            //display no categories if there are none
            spinnerAdapter.add("No Categories");
            spinnerAdapter.add("No Categories");
        }

        //set spinner adapter
        categorySpinner.setAdapter(spinnerAdapter);

        //set default selection to hint
        categorySpinner.setSelection(spinnerAdapter.getCount());

        //onItemSelectListener for spinner
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // show it
        alertDialog.show();
    }

    private class AsyncCreateBudget extends AsyncTask<String,Void,Boolean>{


        @Override
        protected Boolean doInBackground(String... budgetName) {

            if(myDBHelper.checkBudgetName(budgetName[0])){

                CurrentBudgetFragment.currentBudget = myDBHelper.addBudget(budgetName[0], usedCategoriesList);

                return true;
            }
            else{
                return false;
            }
        }
        protected void onPostExecute(Boolean added){

            Log.d("AsyncAddBudget", String.valueOf(added));

            if(!added){
                new AlertDialog.Builder(AddBudget.this)
                        .setTitle("Invalid Budget Name")
                        .setMessage("The budget name entered already exists. Please enter a unique name.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                addBudget();
                            }
                        })
                        .show();
            }else {

                //close out of the activity and return to SwipeViews
                finish();

            }


        }

    }

    private void addBudget(){
        if (String.valueOf(editTextBudgetName.getText()).trim().isEmpty()) {
            new AlertDialog.Builder(AddBudget.this)
                    .setTitle("Invalid Budget Name")
                    .setMessage("Must enter a name for the new budget.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }else{
            AsyncCreateBudget createBudget = new AsyncCreateBudget();
            createBudget.execute(String.valueOf(editTextBudgetName.getText()).trim());
        }



    }
}
