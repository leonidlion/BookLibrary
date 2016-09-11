package com.leodev.booklibrary.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leodev.booklibrary.models.Item;
import com.leodev.booklibrary.models.Result;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BooksApi {
    public static final String BASE_URL = "https://www.googleapis.com/books/v1/";
    public static final String KEY_RELEVANCE = "relevance";
    public static final String KEY_NEWEST = "newest";
    public static final String KEY_BOOKS = "books";
    public static final String KEY_MAGAZINES = "magazines";
    private BooksApiService booksApiService;

    private int maxResults;

    private BooksApi(BooksApiService booksApiService) {
        this.booksApiService = booksApiService;
        maxResults = 20;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        if (maxResults > 40) {
            this.maxResults = 40;
        } else if (maxResults < 1) {
            this.maxResults = 1;
        } else {
            this.maxResults = maxResults;
        }
    }

    public static BooksApi create() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        BooksApiService booksApiService = retrofit.create(BooksApiService.class);

        return new BooksApi(booksApiService);
    }

    public void searchBooks(String terms, int page, final ResultCallback callback) {
        String paramTerms = checkParamTerms(terms);
        String paramMaxResults = String.valueOf(maxResults);
        String paramStartIndex = String.valueOf(page * maxResults);

        Call<Result> call = booksApiService.searchBooks(
                paramTerms,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void searchBooksInTitle(String title, int page, final ResultCallback callback) {
        String paramTerms = checkParamTerms(title);
        paramTerms = "intitle:" + paramTerms;
        String paramMaxResults = String.valueOf(maxResults);
        String paramStartIndex = String.valueOf(page * maxResults);
        Call<Result> call = booksApiService.searchBooks(
                paramTerms,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void searchBooksInAuthor(String author, int page, final ResultCallback callback) {
        String paramTerms = checkParamTerms(author);
        paramTerms = "inauthor:" + paramTerms;
        String paramMaxResults = String.valueOf(maxResults);
        String paramStartIndex = String.valueOf(page * maxResults);
        Call<Result> call = booksApiService.searchBooks(
                paramTerms,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void searchBooksInPublisher(String publisher, int page, final ResultCallback callback) {
        String paramTerms = checkParamTerms(publisher);
        paramTerms = "inpublisher:" + paramTerms;
        String paramMaxResults = String.valueOf(maxResults);
        String paramStartIndex = String.valueOf(page * maxResults);
        Call<Result> call = booksApiService.searchBooks(
                paramTerms,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void searchBooksPrintType(String terms,
                                     String printType, int page, final ResultCallback callback) {
        if (!printType.matches(KEY_BOOKS + "|" + KEY_MAGAZINES)) {
            Throwable t = new Throwable("printType parameter should be 'books' or 'magazines'");
            callback.onFailure(t);
            return;
        }
        String paramTerms = checkParamTerms(terms);
        String paramMaxResults = String.valueOf(maxResults);
        String paramStartIndex = String.valueOf(page * maxResults);
        Call<Result> call = booksApiService.searchBooksPrintType(
                paramTerms,
                printType,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void searchBooksLangRestrict(String terms,
                                        String lang, int page, final ResultCallback callback) {
        String paramTerms = checkParamTerms(terms);
        String paramMaxResults = String.valueOf(maxResults);
        String paramStartIndex = String.valueOf(page * maxResults);
        Call<Result> call = booksApiService.searchBooksLangRestrict(
                paramTerms,
                lang,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void searchBooksOrderBy(String terms,
                                   String orderBy, int page, final ResultCallback callback) {
        if (!orderBy.matches(KEY_RELEVANCE + "|" + KEY_NEWEST)) {
            Throwable t = new Throwable("orderBy parameter should be 'relevance' or 'newest'");
            callback.onFailure(t);
            return;
        }
        String paramTerms = checkParamTerms(terms);
        String paramMaxResults = String.valueOf(maxResults);
        String paramStartIndex = String.valueOf(page * maxResults);
        Call<Result> call = booksApiService.searchBooksOrderBy(
                paramTerms,
                orderBy,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void getBook(String title, final ItemCallback callback) {
        String paramTitle = checkParamTerms(title);
        String paramMaxResults = String.valueOf(1);
        String paramStartIndex = String.valueOf(0);
        Call<Result> call = booksApiService.searchBooks(
                paramTitle,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    public void getBookById(String id, final ItemCallback callback) {
        Call<Item> call = booksApiService.getBookById(id);
        callbackItem(callback, call);
    }

    public void getBookForIsbn(String isbn, final ItemCallback callback) {
        String paramIsbn = checkParamTerms(isbn);
        paramIsbn = "isbn:" + paramIsbn;
        String paramMaxResults = String.valueOf(1);
        String paramStartIndex = String.valueOf(0);
        Call<Result> call = booksApiService.searchBooks(
                paramIsbn,
                paramMaxResults,
                paramStartIndex
                );
        callback(callback, call);
    }

    private void callbackItem(final ItemCallback callback, final Call<Item> call) {
        call.enqueue(new Callback<Item>() {
            @Override
            public void onResponse(Call<Item> call, Response<Item> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Item> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private void callback(final ResultCallback callback, Call<Result> call) {
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    private void callback(final ItemCallback callback, Call<Result> call) {
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                List<Item> items = response.body().getItems();
                if (items != null && items.size() == 1) {
                    callback.onSuccess(items.get(0));
                } else {
                    callback.onFailure(new Throwable("Item not founded"));
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                callback.onFailure(t);
            }

        });
    }

    private String checkParamTerms(String terms) {
        return terms;
    }
}
