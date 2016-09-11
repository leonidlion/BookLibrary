package com.leodev.booklibrary.api;

import com.leodev.booklibrary.models.Item;

public interface ItemCallback {
    void onSuccess(Item item);
    void onFailure(Throwable t);
}
