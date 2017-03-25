package com.gregrussell.budget;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by greg on 3/24/2017.
 */

public class BudgetListFragment extends Fragment{

    Context context;
    ViewGroup rootView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        context = getActivity();

        rootView = (ViewGroup) inflater.inflate(R.layout.activity_main, container, false);

        return rootView;
    }
}
