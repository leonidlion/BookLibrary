package com.leodev.booklibrary.api;


import com.leodev.booklibrary.models.Result;


public interface ResultCallback {
    void onSuccess(Result result);
    void onFailure(Throwable t);
}
