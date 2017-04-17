package com.karanchuk.roman.testtranslate.ui.translator;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.karanchuk.roman.testtranslate.data.source.local.TablesPersistenceContract;
import com.karanchuk.roman.testtranslate.ui.view.CustomEditText;
import com.karanchuk.roman.testtranslate.ui.fullscreen.FullscreenActivity;
import com.karanchuk.roman.testtranslate.R;
import com.karanchuk.roman.testtranslate.ui.target_lang.TargetLangActivity;
import com.karanchuk.roman.testtranslate.data.DictDefinition;
import com.karanchuk.roman.testtranslate.data.TranslatedItem;
import com.karanchuk.roman.testtranslate.data.source.TranslatorDataSource;
import com.karanchuk.roman.testtranslate.data.source.TranslatorRepository;
import com.karanchuk.roman.testtranslate.data.source.local.TranslatorLocalDataSource;
import com.karanchuk.roman.testtranslate.ui.source_lang.SourceLangActivity;
import com.karanchuk.roman.testtranslate.utils.TranslatorAPIUtils;
import com.karanchuk.roman.testtranslate.data.source.local.TablesPersistenceContract.TranslatedItemEntry;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 8.4.17.
 */

public class TranslatorFragment extends Fragment implements
        TranslatorRepository.HistoryTranslatedItemsRepositoryObserver {
    private View mView, mViewActionBar;
    private CustomEditText mCustomEditText;
    private ImageButton mGetPhoto,
            mButtonGetSourceVoice,
            mButtonGetTargetVoice,
            mButtonSetFavorite,
            mButtonShare,
            mButtonFullscreen,
            mClearEditText;
    private RelativeLayout mTranslateResultContainer;
    private ActionBar mActionBar;
    private Button mButtonSrcLang, mButtonTrgLang;
    private ImageButton mButtonSwitchLang;
    private TextView mTranslatedResult, mTvLink;
    private RecyclerView mTranslateRecyclerView;
    private ArrayList<DictDefinition> mDictDefinitions;
    private RecyclerView.LayoutManager mLayoutManager;
    private TranslatorRepository mRepository;
    private Handler mMainHandler;
    private List<TranslatedItem> mTranslatedItems;
    private RelativeLayout mContainerEdittext;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_translator, container, false);

        TranslatorDataSource localDataSource = TranslatorLocalDataSource.getInstance(getContext());

        mRepository = TranslatorRepository.getInstance(localDataSource);
        mRepository.addHistoryContentObserver(this);
        mTranslatedItems = mRepository.getTranslatedItems(TranslatedItemEntry.TABLE_NAME_HISTORY);


        mMainHandler = new Handler(getContext().getMainLooper());

        initToolbar();
        handleKeyboardVisibility();

        mLayoutManager = new LinearLayoutManager(mView.getContext());
        mViewActionBar = mActionBar.getCustomView();
        mButtonSrcLang = (Button) mViewActionBar.findViewById(R.id.left_actionbar_button);
        mButtonSwitchLang = (ImageButton) mViewActionBar.findViewById(R.id.center_actionbar_button);
        mButtonTrgLang = (Button) mViewActionBar.findViewById(R.id.right_actionbar_button);

        mCustomEditText = (CustomEditText) mView.findViewById(R.id.edittext);
        initCustomEditText();

        mClearEditText = (ImageButton) mView.findViewById(R.id.clearEditText);
        mTranslateResultContainer = (RelativeLayout) mView.findViewById(R.id.translate_result_container);

        mClearEditText.setVisibility(View.INVISIBLE);

        mGetPhoto = (ImageButton) mView.findViewById(R.id.getAudioSpelling);
        mButtonGetSourceVoice = (ImageButton) mView.findViewById(R.id.get_source_voice);

        mTranslatedResult = (TextView) mView.findViewById(R.id.textview_translate_result);

        mButtonFullscreen = (ImageButton) mView.findViewById(R.id.fullscreen_translated_word);

        mContainerEdittext = (RelativeLayout) mView.findViewById(R.id.container_edittext);

        mButtonGetSourceVoice = (ImageButton) mView.findViewById(R.id.get_source_voice);
        mButtonGetTargetVoice = (ImageButton) mView.findViewById(R.id.get_target_voice);
        mButtonSetFavorite = (ImageButton) mView.findViewById(R.id.set_favorite);
        mButtonShare = (ImageButton) mView.findViewById(R.id.share_translated_word);


        mTranslateRecyclerView = (RecyclerView) mView.findViewById(R.id.container_dict_defin);
        mTranslateRecyclerView.setLayoutManager(mLayoutManager);

        mDictDefinitions = new ArrayList<>();
        for (int i = 1; i <= 10; i++){
            mDictDefinitions.add(new DictDefinition(String.valueOf(i),
                    "время ср, раз м, момент м, срок м, пора ж, период м",
                    "(period, time, moment, pore)",
                    "dayling saving time \u2014 летнее время\ntake some time \u2014 занять некоторое время"));
        }
        mTranslateRecyclerView.setAdapter(new TranslatorRecyclerAdapter(mDictDefinitions));


        mButtonFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FullscreenActivity.class);
                intent.putExtra("translated_result", mTranslatedResult.getText().toString());
                startActivity(intent);
            }
        });

        mClearEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCustomEditText.getText().clear();
            }
        });


        mButtonSrcLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SourceLangActivity.class);
                startActivity(intent);
            }
        });
        mButtonSwitchLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String srcText = mButtonSrcLang.getText().toString(),
                        trgText = mButtonTrgLang.getText().toString();
                mButtonSrcLang.setText(trgText);
                mButtonTrgLang.setText(srcText);
            }
        });
        mButtonTrgLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TargetLangActivity.class);
                startActivity(intent);
            }
        });

        mButtonGetSourceVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "get source voice was clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mButtonGetTargetVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "get target voice was clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mButtonSetFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "set favorite was clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "share was clicked", Toast.LENGTH_SHORT).show();
            }
        });



//        mTvLink = (TextView) mView.findViewById(R.id.tv_link);
//        mTvLink.setMovementMethod(LinkMovementMethod.getInstance());

        return mView;
    }

    @Override
    public void onStop() {
        super.onStop();
        mRepository.removeHistoryContentObserver(this);
    }


    public void initToolbar(){
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if ( mActionBar != null) {
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setCustomView(R.layout.actionbar_translator);

            mActionBar.setShowHideAnimationEnabled(false);
            mActionBar.setElevation(0);
            mActionBar.setTitle("");
            mActionBar.show();
        }
    }

    public void handleKeyboardVisibility(){
        KeyboardVisibilityEvent.setEventListener(
                getActivity(),
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        // some code depending on keyboard visiblity status
                        if (isOpen && isAdded()){
                            try {
                                mContainerEdittext.setBackground(
                                        Drawable.createFromXml(
                                                getResources(),
                                                getResources().getLayout(R.layout.edittext_border_active)));
                                mCustomEditText.setCursorVisible(true);
                            } catch (XmlPullParserException | IOException e) {
                                e.printStackTrace();
                            }
                        } else if (!isOpen && isAdded()){
                            try {
                                mContainerEdittext.setBackground(
                                        Drawable.createFromXml(
                                                getResources(),
                                                getResources().getLayout(R.layout.edittext_border)));
                                mCustomEditText.setCursorVisible(false);
                                addToHistory();
                            } catch (XmlPullParserException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void hideKeyboard(){
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mView.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void showKeyboard(){
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void initCustomEditText(){
        mCustomEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mCustomEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        mCustomEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && mCustomEditText.getText().length() != 0) {
                    try {
                        TranslatorAPIUtils.getTranslate(mCustomEditText.getText().toString(), "ru-en", mTranslatedResult);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    Log.d("keyboard state", "ACTION_DONE & customEditText is not empty");
                }
                return false;
            }
        });

        mCustomEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mCustomEditText.getText().length() != 0 && !mClearEditText.isShown()){
                    mClearEditText.setVisibility(View.VISIBLE);
                    mGetPhoto.setImageResource(R.drawable.volume_up_indicator_dark512);
                } else if (mCustomEditText.getText().length() == 0 && mClearEditText.isShown()){
                    mClearEditText.setVisibility(View.INVISIBLE);
                    mGetPhoto.setImageResource(R.drawable.camera_dark512);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mCustomEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                }
            }
        });
//        showKeyboard();
    }

    public void addToHistory(){
        if (mCustomEditText.getText().length() != 0){
            TranslationSaver saver = new TranslationSaver();
            new Thread(saver).start();
        }
    }

    private class TranslationSaver implements Runnable{
        @Override
        public void run() {
            TranslatedItem item = new TranslatedItem(
                    mButtonSrcLang.getText().toString(),
                    mButtonTrgLang.getText().toString(),
                    mCustomEditText.getText().toString(),
                    mTranslatedResult.getText().toString(),
                    "false",
                    mDictDefinitions.toString());
            if (!mTranslatedItems.contains(item)) {
                mRepository.saveTranslatedItem(TranslatedItemEntry.TABLE_NAME_HISTORY, item);
            } else {
                int index = mTranslatedItems.indexOf(item);
                item.setIsFavorite(mTranslatedItems.get(index).getIsFavorite());
                mRepository.saveTranslatedItem(TranslatedItemEntry.TABLE_NAME_HISTORY, item);
            }
        }
    }




    @Override
    public void onHistoryTranslatedItemsChanged() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mTranslatedItems = mRepository.getTranslatedItems(TranslatedItemEntry.TABLE_NAME_HISTORY);
            }
        });
    }
}


//        View.OnClickListener editTextClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v.getId() == mCustomEditText.getId()){
//                    mCustomEditText.setCursorVisible(true);
//                }
//            }
//        };
//        mCustomEditText.setOnClickListener(editTextClickListener);


//        View.OnClickListener lostFocusEditTextClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v.getId() == mTranslateResultContainer.getId()){
//                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    if (in.isAcceptingText()) {
//                        in.hideSoftInputFromWindow(mCustomEditText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                        mCustomEditText.setCursorVisible(false);
////                        Log.d("keyboard state", "lostFocusEditText");
//                    }
//                }
//            }
//        };
//        mTranslateResultContainer.setOnClickListener(lostFocusEditTextClickListener);



