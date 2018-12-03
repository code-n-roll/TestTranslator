package com.karanchuk.roman.testtranslate.presentation.ui.stored;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.karanchuk.roman.testtranslate.R;
import com.karanchuk.roman.testtranslate.data.database.TablePersistenceContract;
import com.karanchuk.roman.testtranslate.data.database.model.TranslatedItem;
import com.karanchuk.roman.testtranslate.data.database.repository.TranslatorLocalRepository;
import com.karanchuk.roman.testtranslate.data.database.repository.TranslatorRepository;
import com.karanchuk.roman.testtranslate.data.database.repository.TranslatorRepositoryImpl;
import com.karanchuk.roman.testtranslate.presentation.ui.stored.favorites.FavoritesFragment;
import com.karanchuk.roman.testtranslate.presentation.ui.stored.history.HistoryFragment;
import com.karanchuk.roman.testtranslate.utils.ContentManager;
import com.karanchuk.roman.testtranslate.utils.UIUtils;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.List;

/**
 * Created by roman on 8.4.17.
 */

public class StoredFragment extends Fragment implements
        TranslatorRepositoryImpl.HistoryTranslatedItemsRepositoryObserver,
        TranslatorRepositoryImpl.FavoritesTranslatedItemsRepositoryObserver {
    private static final int HISTORY_FRAGMENT = 0;
    private static final int FAVORITES_FRAGMENT = 1;
    private static final String MAIN_HANDLER_THREAD = StoredFragment.class.getName() + "MAIN_HANDLER_THREAD";

    private StoredPagerAdapter mFavoritesAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ImageButton mClearStored;
    private View mMainActivityContainer;
    private AHBottomNavigation mBottomNavigation;
    private ClearStoredDialogFragment mClearHistoryDialog;

    private List<TranslatedItem> mFavoritesItems;
    private List<TranslatedItem> mHistoryItems;

    private TranslatorRepositoryImpl mRepository;
    private ContentManager mContentManager;
    private Handler mMainHandler;
    private HandlerThread mMainHandlerThread;
    private Handler mUIHandler;
    private int mCurPosition = 0;
    private int mBottomPadding;
    private Bundle mBundle;

    private ViewPager.OnPageChangeListener mStorePageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position,
                                   float positionOffset,
                                   int positionOffsetPixels) {
//        Log.d("viewpager log",
//                "onPageScrellod, position="+String.valueOf(position)+
//                        ",positionOffset="+String.valueOf(positionOffset)+
//                        ",positionOffsetPixels="+String.valueOf(positionOffsetPixels)
//        );
        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0 && mHistoryItems.isEmpty() ||
                    position == 1 && mFavoritesItems.isEmpty()){
                mClearStored.setVisibility(View.INVISIBLE);
            } else {
                mClearStored.setVisibility(View.VISIBLE);
            }

            if (mCurPosition != position){
                mContentManager.notifyTranslatedItemChanged();
            }
            mCurPosition = position;
//        Log.d("viewpager log", "onPageSelected, position="+ String.valueOf(position));
        }

        @Override
        public void onPageScrollStateChanged(int state) {
//        Log.d("viewpager log", "onPageScrollStateChanged, state="+String.valueOf(state));
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stored, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViewsOnFragment(view);
        // findViewsOnActivity();
        //
        mContentManager = ContentManager.getInstance();
        TranslatorRepository localDataSource = TranslatorLocalRepository.getInstance(getContext());
        mRepository = TranslatorRepositoryImpl.getInstance(localDataSource);
        mRepository.addHistoryContentObserver(this);
        mRepository.addFavoritesContentObserver(this);
        //
        // mClearHistoryDialog = new ClearStoredDialogFragment();
        // mBundle = new Bundle();
        //
        // UIUtils.changeSoftInputModeWithOrientation(getActivity());
        // handleKeyboardVisibility();
        //
        initViewPager();
    }

    private void initClearStored(){
        if (mHistoryItems.isEmpty()){
            mClearStored.setVisibility(View.INVISIBLE);
        } else {
            mClearStored.setVisibility(View.VISIBLE);
        }
        mClearStored.setOnClickListener(bundle -> clickOnClearStored(mBundle));
    }

    private void findViewsOnFragment(View view){
        mTabLayout = view.findViewById(R.id.tablayout_favorites);
        mViewPager = view.findViewById(R.id.viewpager_stored);
        mClearStored = view.findViewById(R.id.imagebutton_clear_stored);
    }

    private void findViewsOnActivity(){
        mBottomNavigation = getActivity().findViewById(R.id.navigation);
    }

    private void clickOnClearStored(Bundle bundle){
        switch(mViewPager.getCurrentItem()) {
            case HISTORY_FRAGMENT:
//                bundle.putString("title"," History");
//                mClearHistoryDialog.setArguments(bundle);
//                mClearHistoryDialog.show(getFragmentManager(), CLEAR_HISTORY_DIALOG);
                break;
            case FAVORITES_FRAGMENT:
//                bundle.putString("title"," Favorites");
//                mClearHistoryDialog.setArguments(bundle);
//                mClearHistoryDialog.show(getFragmentManager(), CLEAR_HISTORY_FAVORITES);
                break;
            default:
                break;
        }
        UIUtils.showToast(getContext(), getResources().getString(R.string.next_release_message));
    }

    @Override
    public void onStart() {
        super.onStart();

        mMainHandlerThread = new HandlerThread(MAIN_HANDLER_THREAD);
        mMainHandlerThread.start();
        mMainHandler = new Handler(mMainHandlerThread.getLooper());
        mUIHandler = new Handler(Looper.getMainLooper());

        mMainHandler.post(() -> {
            mFavoritesItems = mRepository.getTranslatedItems(TablePersistenceContract.
                    TranslatedItemEntry.TABLE_NAME_FAVORITES);
            mHistoryItems = mRepository.getTranslatedItems(TablePersistenceContract.
                    TranslatedItemEntry.TABLE_NAME_HISTORY);
        });
        mMainHandler.post(() -> {
            mUIHandler.post(this::initClearStored);
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mRepository.removeFavoritesContentObserver(this);
        mRepository.removeHistoryContentObserver(this);
        mMainHandlerThread.quit();
        mMainHandlerThread = null;
        mMainHandler = null;
        mUIHandler = null;
    }

    private void initViewPager(){
        mFavoritesAdapter = new StoredPagerAdapter(getChildFragmentManager());
        mFavoritesAdapter.addFragment(new FavoritesFragment(), getResources().getString(R.string.title_favorites));
        mFavoritesAdapter.addFragment(new HistoryFragment(), getResources().getString(R.string.title_history));
        mViewPager.setAdapter(mFavoritesAdapter);
        mViewPager.addOnPageChangeListener(mStorePageChangeListener);
        mViewPager.setOffscreenPageLimit(2);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onFavoritesTranslatedItemsChanged() {
        mMainHandler.post(() -> {
            mFavoritesItems.clear();
            mFavoritesItems.addAll(mRepository.getTranslatedItems(TablePersistenceContract.
                    TranslatedItemEntry.TABLE_NAME_FAVORITES));
        });
    }

    @Override
    public void onHistoryTranslatedItemsChanged() {
        mMainHandler.post(() -> {
            mHistoryItems.clear();
            mHistoryItems.addAll(mRepository.getTranslatedItems(TablePersistenceContract.
                    TranslatedItemEntry.TABLE_NAME_HISTORY));
        });
    }

    private void handleKeyboardVisibility(){
        KeyboardVisibilityEvent.setEventListener(
                getActivity(),
                isOpen -> {
                    if (isOpen && isAdded()){
                            // mBottomPadding = UIUtils.hideBottomNavViewGetBottomPadding(
                            //         getActivity(),
                            //         mMainActivityContainer,
                            //         mBottomNavigation);
                    } else if (!isOpen && isAdded()){
                            UIUtils.showBottomNavViewSetBottomPadding(
                                    getActivity(),
                                    mMainActivityContainer,
                                    mBottomNavigation,
                                    mBottomPadding);
                    }
                });
    }
}