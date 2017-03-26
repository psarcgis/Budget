package com.gregrussell.budget;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by greg on 3/24/2017.
 */

public class SwipeViews extends Activity {

    private static final int NUM_PAGES = 2;
    public static ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    public static TextView FRAG_TITLE;
    public static View TOP_BAR;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.swipe_views_layout);

        //pager allows for swiping between fragments
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new SwipeViewsPagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //sliding tab class allows for icon to display which fragment we are on
        SlidingTabLayout slide = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slide.setViewPager(mPager);

        FRAG_TITLE = (TextView)findViewById(R.id.fragmentTitleSwipeBies);
        TOP_BAR = (View)findViewById(R.id.topBarSwipeViews);

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
}



