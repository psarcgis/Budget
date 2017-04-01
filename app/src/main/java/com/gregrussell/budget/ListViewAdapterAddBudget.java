package com.gregrussell.budget;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by greg on 3/31/2017.
 */

public class ListViewAdapterAddBudget extends BaseAdapter {

    List<CategoryObj> categoryObjList = new ArrayList<CategoryObj>();
    private LayoutInflater inflater;
    Context context;

    public ListViewAdapterAddBudget(Context context, List<CategoryObj> categoryObjList){

        Log.d("listViewAdapterAll", "List view adapter class has run " + categoryObjList.size() );
        this.context = context;
        this.categoryObjList = categoryObjList;
        inflater = ((Activity)context).getLayoutInflater();
    }


    @Override
    public int getCount() {
        return categoryObjList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Log.d("listViewAdapterAll", "List view adapter getView method");
        View currentView = convertView;
        if(currentView == null){
            currentView = inflater.inflate(R.layout.add_budget_category_list_item_layout,parent,false);
        }
        final TextView category = (TextView)currentView.findViewById(R.id.categoryAddBudgetListItem);
        TextView remove = (TextView)currentView.findViewById(R.id.removeCategoryAddBudgetListItem);

        //assign textViews
        category.setText(categoryObjList.get(position).getCategoryName());
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("categoriesList adapter", "categoryObjList size is " + categoryObjList.size() + " removed will be " + categoryObjList.get(position).getCategoryName());
                Log.d("categoriesList adapter", "position is " + categoryObjList.size());
                Log.d("categoriesList adapter", "removed category id is " + categoryObjList.get(position).getID());

                //categories in the DB will have an ID. only add those
                if(categoryObjList.get(position).getID() != 0) {
                    AddBudget.unusedCategoriesList.add(categoryObjList.get(position));
                }
                Log.d("categoriesList adapter", "added to unused list is " + categoryObjList.get(position).getCategoryName());
                AddBudget.usedCategoriesList.remove(position);

                ListViewAdapterAddBudget adapter = new ListViewAdapterAddBudget(context, categoryObjList);
                AddBudget.listViewCategoriesAdd.setAdapter(null);
                AddBudget.listViewCategoriesAdd.setAdapter(adapter);
            }
        });

        return currentView;


    }
}