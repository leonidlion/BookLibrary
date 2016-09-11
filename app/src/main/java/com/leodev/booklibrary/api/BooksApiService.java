package com.leodev.booklibrary.api;

import com.leodev.booklibrary.models.Item;
import com.leodev.booklibrary.models.Result;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface BooksApiService {
    @GET("volumes")
    Call<Result> searchBooks(@Query("q") String terms,
                             @Query("maxResults") String maxResults,
                             @Query("startIndex") String startIndex);

    @GET("volumes")
    Call<Result> searchBooksOrderBy(@Query("q") String terms,
                                    @Query("orderBy") String orderBy,
                                    @Query("maxResults") String maxResults,
                                    @Query("startIndex") String startIndex);

    @GET("volumes")
    Call<Result> searchBooksLangRestrict(@Query("q") String terms,
                                    @Query("langRestrict") String lang,
                                    @Query("maxResults") String maxResults,
                                    @Query("startIndex") String startIndex);

    @GET("volumes")
    Call<Result> searchBooksPrintType(@Query("q") String terms,
                                         @Query("printType") String printType,
                                         @Query("maxResults") String maxResults,
                                         @Query("startIndex") String startIndex);

    @GET("volumes/{volumeId}")
    Call<Item> getBookById(@Path("volumeId") String id);

}
