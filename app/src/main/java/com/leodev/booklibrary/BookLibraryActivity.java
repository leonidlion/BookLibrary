package com.leodev.booklibrary;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;


public class BookLibraryActivity extends SingleFragmentActivity {

    public static Intent newIntent (Context context){
        return new Intent(context, BookLibraryActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return BookLibraryFragment.newInstance();
    }
}
