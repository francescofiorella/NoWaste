package com.far.nowaste;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Toolbar;

import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class SearchAnimationView {

    Toolbar mToolbar;
    DrawerLayout drawerLayout;
    Context context;
    Resources resources;

    public SearchAnimationView(Toolbar mToolbar, DrawerLayout drawerLayout, Context context, Resources resources){
        this.mToolbar = mToolbar;
        this.drawerLayout = drawerLayout;
        this.context = context;
        this.resources = resources;
    }

    // animazioni searchView
    public void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {

        mToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.search_background));
        drawerLayout.setStatusBarBackgroundColor(ContextCompat.getColor(context, R.color.quantum_grey_600));

        if (show) {
            int width = mToolbar.getWidth() - (containsOverflow ? resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) - ((resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar, isRtl(resources) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, 0.0f, (float) width);
            createCircularReveal.setDuration(250);
            createCircularReveal.start();
        } else {
            int width = mToolbar.getWidth() - (containsOverflow ? resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) - ((resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar, isRtl(resources) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, (float) width, 0.0f);
            createCircularReveal.setDuration(250);
            createCircularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mToolbar.setBackgroundColor(getThemeColor(context, R.attr.colorPrimary));
                    drawerLayout.setStatusBarBackgroundColor(getThemeColor(context, R.attr.colorPrimaryDark));
                }
            });
            createCircularReveal.start();
            drawerLayout.setStatusBarBackgroundColor(getThemeColor(context, R.attr.colorPrimaryDark));
        }
    }

    private boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    // metodo per restituire il colore selezionato del tema
    private static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }
}
