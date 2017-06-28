package com.karanchuk.roman.testtranslate.presentation.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.karanchuk.roman.testtranslate.R;
import com.karanchuk.roman.testtranslate.data.TranslatorDataSource;
import com.karanchuk.roman.testtranslate.data.TranslatorRepository;
import com.karanchuk.roman.testtranslate.data.local.TablesPersistenceContract.TranslatedItemEntry;
import com.karanchuk.roman.testtranslate.data.local.TranslatorLocalDataSource;
import com.karanchuk.roman.testtranslate.presentation.model.TranslatedItem;
import com.karanchuk.roman.testtranslate.presentation.view.adapter.StoredRecyclerAdapter;
import com.karanchuk.roman.testtranslate.utils.ContentManager;
import com.karanchuk.roman.testtranslate.utils.UIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.karanchuk.roman.testtranslate.presentation.Constants.CUR_SELECTED_ITEM_SRC_LANG;
import static com.karanchuk.roman.testtranslate.presentation.Constants.CUR_SELECTED_ITEM_TRG_LANG;
import static com.karanchuk.roman.testtranslate.presentation.Constants.EDITTEXT_DATA;
import static com.karanchuk.roman.testtranslate.presentation.Constants.PREFS_NAME;
import static com.karanchuk.roman.testtranslate.presentation.Constants.SRC_LANG;
import static com.karanchuk.roman.testtranslate.presentation.Constants.TRANSL_CONTENT;
import static com.karanchuk.roman.testtranslate.presentation.Constants.TRANSL_RESULT;
import static com.karanchuk.roman.testtranslate.presentation.Constants.TRG_LANG;
import static com.karanchuk.roman.testtranslate.presentation.Constants.UNIQUE_FAVORITES_FRAGMENT_ID;

/**
 * Created by roman on 9.4.17.
 */

public class FavoritesFragment extends Fragment implements
        SearchView.OnQueryTextListener,
        TranslatorRepository.FavoritesTranslatedItemsRepositoryObserver,
        TranslatorRepository.HistoryTranslatedItemsRepositoryObserver,
        ContentManager.TranslatedItemChanged
{
    private RecyclerView mFavoritesRecycler;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration mDividerItemDecoration;
    private SearchView mSearchViewFavorites;
    private ImageButton mButtonIsFavorite;
    private ImageButton mClearStored;
    private TranslatorRepository mRepository;
    private List<TranslatedItem> mFavoritesTranslatedItems,
                                    mHistoryTranslatedItems,
                                    mCopyFavoritesTranslatedItems;
    private Handler mMainHandler;
    private View mView;
    private View mEmptyView;
    private View mContentView;
    private View mEmptySearchView;
    private TextView mTextViewEmptyFavorites;
    private TextView mTextViewEmptySearch;
    private ImageView mImageViewEmptyFavorites;
    private ImageView mImageViewEmptySearch;
    private ContentManager mContentManager;
    private SharedPreferences mSettings;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainHandler = new Handler(getContext().getMainLooper());


        mSettings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        View parentView = getParentFragment().getView();
        if (parentView != null)
            mClearStored = parentView.findViewById(R.id.imagebutton_clear_stored);

        mContentManager = ContentManager.getInstance();

        TranslatorDataSource localDataSource = TranslatorLocalDataSource.getInstance(getContext());
        mRepository = TranslatorRepository.getInstance(localDataSource);
        mFavoritesTranslatedItems = mRepository.getTranslatedItems(TranslatedItemEntry.TABLE_NAME_FAVORITES);
        mHistoryTranslatedItems = mRepository.getTranslatedItems(TranslatedItemEntry.TABLE_NAME_HISTORY);
        mCopyFavoritesTranslatedItems = new ArrayList<>(mFavoritesTranslatedItems);

        Collections.reverse(mFavoritesTranslatedItems);
        Collections.reverse(mCopyFavoritesTranslatedItems);
        Collections.reverse(mHistoryTranslatedItems);

        mView = inflater.inflate(R.layout.content_favorites, container, false);
        findViewsOnFragment();

        mEmptySearchView.setVisibility(View.INVISIBLE);

        mTextViewEmptyFavorites.setText(R.string.empty_favorites);
        mImageViewEmptyFavorites.setImageResource(R.drawable.bookmark_black_shape_light512);

        mTextViewEmptySearch.setText(R.string.empty_search);
        mImageViewEmptySearch.setImageResource(R.drawable.bookmark_black_shape_light512);

        mLayoutManager = new LinearLayoutManager(mView.getContext());
        mFavoritesRecycler.setLayoutManager(mLayoutManager);

        mSearchViewFavorites.setIconifiedByDefault(false);
        mSearchViewFavorites.setQueryHint("Search in Favorites");
        mSearchViewFavorites.setOnQueryTextListener(this);
        mSearchViewFavorites.setVisibility(View.GONE);

        mDividerItemDecoration =
                new DividerItemDecoration(mFavoritesRecycler.getContext(), RecyclerView.VERTICAL);
        mFavoritesRecycler.addItemDecoration(mDividerItemDecoration);

        final BottomNavigationView navigation = getActivity().findViewById(R.id.navigation);
        final View translatorNavigationItem = navigation.findViewById(R.id.navigation_translate);

        mFavoritesRecycler.setAdapter(new StoredRecyclerAdapter(
                mFavoritesTranslatedItems,
                (item)->clickOnItemStoredRecycler(item,translatorNavigationItem),
                (item)->clickOnSetFavoriteItem(item),
                UNIQUE_FAVORITES_FRAGMENT_ID));

        registerForContextMenu(mFavoritesRecycler);

        chooseCurView();

        return mView;
    }

    private void clickOnSetFavoriteItem(TranslatedItem item){
        if (item.isFavorite()){
            item.isFavoriteUp(false);
            mRepository.deleteTranslatedItem(TranslatedItemEntry.TABLE_NAME_FAVORITES, item);
            mFavoritesRecycler.getAdapter().notifyItemChanged(mFavoritesTranslatedItems.indexOf(item));
        } else {
            item.isFavoriteUp(true);
            mRepository.saveTranslatedItem(TranslatedItemEntry.TABLE_NAME_FAVORITES, item);
            mFavoritesRecycler.getAdapter().notifyItemChanged(mFavoritesTranslatedItems.indexOf(item));
        }
        mRepository.updateTranslatedItem(TranslatedItemEntry.TABLE_NAME_HISTORY, item);

//        UIUtils.showToast(getContext(),"isFavorite was clicked in favorites");
    }

    private void clickOnItemStoredRecycler(final TranslatedItem item, final View view){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(EDITTEXT_DATA, item.getSrcMeaning());
        editor.putString(SRC_LANG, item.getSrcLanguageForUser());
        editor.putString(TRG_LANG, item.getTrgLanguageForUser());
        editor.putString(TRANSL_RESULT, item.getTrgMeaning());
        editor.putString(TRANSL_CONTENT, item.getDictDefinition());
        editor.putString(CUR_SELECTED_ITEM_SRC_LANG, item.getSrcLanguageForAPI());
        editor.putString(CUR_SELECTED_ITEM_TRG_LANG, item.getTrgLanguageForAPI());
        editor.apply();
        view.performClick();
//        UIUtils.showToast(getContext(),"item was clicked in favorites");
    }

    private void findViewsOnFragment(){
        mEmptyView =  mView.findViewById(R.id.include_content_favorites_empty_item_list);
        mContentView = mView.findViewById(R.id.include_content_favorites_full_item_list);
        mEmptySearchView = mView.findViewById(R.id.include_content_favorites_empty_search);
        mTextViewEmptyFavorites =  mView.findViewById(R.id.textview_empty_item_list);
        mImageViewEmptyFavorites =  mView.findViewById(R.id.imageview_empty_item_list);
        mTextViewEmptySearch =  mView.findViewById(R.id.textview_empty_search);
        mImageViewEmptySearch =  mView.findViewById(R.id.imageview_empty_search);
        mButtonIsFavorite = mView.findViewById(R.id.imagebutton_isfavorite_favorite_item);
        mFavoritesRecycler = mView.findViewById(R.id.favorites_items_list);
        mSearchViewFavorites = mView.findViewById(R.id.search_view_favorites);
    }

    @Override
    public void onStart() {
        super.onStart();

        mContentManager.addContentObserver(this);
        mRepository.addFavoritesContentObserver(this);
        mRepository.addHistoryContentObserver(this);
    }

    private void chooseCurView(){
        if (!mFavoritesTranslatedItems.isEmpty()){
            mEmptyView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterForContextMenu(mFavoritesRecycler);

        mRepository.removeHistoryContentObserver(this);
        mRepository.removeFavoritesContentObserver(this);
        mContentManager.removeContentObserver(this);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
//        mFavoritesTranslatedItems = mRepository.getTranslatedItems(TranslatedItemEntry.TABLE_NAME_FAVORITES);
        newText = newText.toLowerCase();
        ArrayList<TranslatedItem> newList = new ArrayList<>();
        for (TranslatedItem item : mFavoritesTranslatedItems){
            String srcMeaning = item.getSrcMeaning(),
                    trgMeaning = item.getTrgMeaning();
            if (srcMeaning.contains(newText) ||
                    trgMeaning.contains(newText)){
                newList.add(item);
            }
        }
        StoredRecyclerAdapter adapter = (StoredRecyclerAdapter)mFavoritesRecycler.getAdapter();
        adapter.setFilter(newList);

        chooseCurSearchView(newList);
        return true;
    }

    private void chooseCurSearchView(List<TranslatedItem> list){
        if (list.isEmpty()){
            mEmptySearchView.setVisibility(View.VISIBLE);
            mFavoritesRecycler.setVisibility(View.INVISIBLE);
        } else {
            mEmptySearchView.setVisibility(View.INVISIBLE);
            mFavoritesRecycler.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFavoritesTranslatedItemsChanged() {
        mMainHandler.post(() -> {
            mCopyFavoritesTranslatedItems.clear();
            mCopyFavoritesTranslatedItems.addAll(mRepository.getTranslatedItems(TranslatedItemEntry.TABLE_NAME_FAVORITES));
            Collections.reverse(mCopyFavoritesTranslatedItems);

//            mFavoritesRecycler.getAdapter().notifyDataSetChanged();
        });
    }

    @Override
    public void onHistoryTranslatedItemsChanged() {
        mMainHandler.post(() -> {
            mHistoryTranslatedItems.clear();
            mHistoryTranslatedItems.addAll(mRepository.getTranslatedItems(TranslatedItemEntry.TABLE_NAME_HISTORY));
            Collections.reverse(mHistoryTranslatedItems);

        });
    }

    @Override
    public void onTranslatedItemsChanged() {
        if (mFavoritesRecycler != null) {
            mFavoritesTranslatedItems.clear();
            mFavoritesTranslatedItems.addAll(mCopyFavoritesTranslatedItems);
            chooseCurView();
            mFavoritesRecycler.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == UNIQUE_FAVORITES_FRAGMENT_ID) {
            switch (item.getItemId()) {
                case R.id.menu_item_delete:
                    performContextItemDeletion();
                    chooseCurView();
                    chooseClearStoredVisibility();
                    UIUtils.showToast(getContext(),"item was longclicked contextmenu in favorites");
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        return super.onContextItemSelected(item);
    }

    private void chooseClearStoredVisibility(){
        if (!mFavoritesTranslatedItems.isEmpty()){
            mClearStored.setVisibility(View.VISIBLE);
        } else {
            mClearStored.setVisibility(View.INVISIBLE);
        }
    }

    private void performContextItemDeletion(){
        StoredRecyclerAdapter adapter = (StoredRecyclerAdapter) mFavoritesRecycler.getAdapter();
        int position = adapter.getPosition();
        TranslatedItem item = mFavoritesTranslatedItems.get(position);
        mRepository.deleteTranslatedItem(TranslatedItemEntry.TABLE_NAME_FAVORITES, item);
        item.isFavoriteUp(false);
        mRepository.updateTranslatedItem(TranslatedItemEntry.TABLE_NAME_HISTORY, item);
        mFavoritesTranslatedItems.remove(position);
        mFavoritesRecycler.getAdapter().notifyItemRemoved(position);
    }
}
