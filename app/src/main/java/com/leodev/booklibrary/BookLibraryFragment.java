package com.leodev.booklibrary;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leodev.booklibrary.api.BooksApi;
import com.leodev.booklibrary.api.ResultCallback;
import com.leodev.booklibrary.models.ImageLinks;
import com.leodev.booklibrary.models.Item;
import com.leodev.booklibrary.models.Result;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class BookLibraryFragment extends Fragment {
    private static final String TAG = "BookLibraryFragment";
    private static final int LAYOUT = R.layout.activity_book_library;
    private static final String SAVED_PAGE = "PAGE";

    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private RecyclerView mRecyclerView;
    private List<Item> mItemList = new ArrayList<>();
    private BookAdapter mBookAdapter;
    private BooksApi booksApi = BooksApi.create();
    private int mPage = 0;

    public static BookLibraryFragment newInstance() {
        return new BookLibraryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);

        if (savedInstanceState != null){
            mPage = savedInstanceState.getInt(SAVED_PAGE);
            searchBooks(QueryPreferences.getStoredQuery(getContext()), mPage);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        setGridLayout(this.getResources().getConfiguration().orientation);

        setupAdapter();

        mBookAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.i(TAG, "LOAD MORE");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "RUN");
                        mPage++;
                        searchBooks(QueryPreferences.getStoredQuery(getContext()), mPage);
                        mBookAdapter.setLoaded();
                    }
                }, 2000);
            }
        });

        return view;
    }

    private void setupAdapter() {
        if (mRecyclerView == null) return;

        if (mItemList != null){
            if (mRecyclerView.getAdapter() == null){
                mBookAdapter = new BookAdapter(mItemList);
                mRecyclerView.setAdapter(mBookAdapter);
            } else {
                mBookAdapter.notifyDataSetChanged();
            }
        } else {
            setupAdapterToNull();
        }
    }

    private void setupAdapterToNull() {
        mRecyclerView.setAdapter(null);
    }

    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            }
            else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_items, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(getContext(), query);
                updateItems();
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getContext());
        mItemList.clear();
        mPage = 0;
        searchBooks(query, mPage);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PAGE, mPage);
    }

    private void searchBooks(String query, int page){
        booksApi.searchBooks(query, page, new ResultCallback() {
            @Override
            public void onSuccess(Result result) {
                displayResult(result.getItems());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Failure to download: " + t);
            }
        });
    }

    private void displayResult(List<Item> items) {
        for (int i = 0; i < items.size(); i++){
            mItemList.add(items.get(i));
        }
        mBookAdapter.notifyDataSetChanged();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setGridLayout(newConfig.orientation);
    }

    private void setGridLayout(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(3,1);
            mStaggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT){
            mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2,1);
            mStaggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        }
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
    }

    //------------------ADAPTER--------------
    private class BookAdapter extends RecyclerView.Adapter<BookHolder>{
        private List<Item> mItemList;

        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading;
        private int visibleThreshold = 5;
        private int lastVisibleItem, totalItemCount;

        public BookAdapter(List<Item> itemList) {
            mItemList = itemList;
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = mStaggeredGridLayoutManager.getItemCount();
                    lastVisibleItem = getLastVisibleItem(mStaggeredGridLayoutManager.findLastVisibleItemPositions(null));

                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)){
                        if (mOnLoadMoreListener != null){
                            mOnLoadMoreListener.onLoadMore();
                        }
                        isLoading = true;
                    }
                }
            });
        }

        public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
            mOnLoadMoreListener = onLoadMoreListener;
        }


        @Override
        public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_list, parent, false);
            return new BookHolder(view);
        }

        @Override
        public void onBindViewHolder(final BookHolder holder, int position) {
                Item item = mItemList.get(position);
                holder.bindBook(item);
        }

        @Override
        public int getItemCount() {
            return mItemList == null ? 0 : mItemList.size();
        }

        public void setBook(List<Item> book){mItemList = book;}

        public void setLoaded(){
            isLoading = false;
        }
    }

    //------------------HOLDER--------------
    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private TextView mTextView;
        private Item mItem;

        public BookHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mImageView = (ImageView) itemView.findViewById(R.id.book_list_image);
            mTextView = (TextView) itemView.findViewById(R.id.book_list_title);
        }

        public void bindBook(Item item){
            mItem = item;

            ImageLinks imageLinks = mItem.getVolumeInfo().getImageLinks();

            if (imageLinks != null) {
                Picasso.with(getContext())
                        .load(imageLinks.getThumbnail())
                        .placeholder(R.drawable.ic_load_image)
                        .error(R.drawable.ic_error_load)
                        .resize(250,0)
                        .into(mImageView);
            }

            String title = item.getVolumeInfo().getTitle();
            mTextView.setText(title);
        }

        @Override
        public void onClick(View view) {
            Log.i(TAG, "Item clicked");
            Uri address = Uri.parse(mItem.getVolumeInfo().getCanonicalVolumeLink());
            Intent startBrowser = new Intent(Intent.ACTION_VIEW, address);
            startActivity(startBrowser);
        }
    }



}
