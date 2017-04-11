package com.karanchuk.roman.testtranslate.data;

import java.util.UUID;

/**
 * Created by roman on 9.4.17.
 */

public class TranslatedItem {
    private String mId;
    private String mSrcLanguage;
    private String mTrgLanguage;
    private String mSrcMeaning;
    private String mTrgMeaning;
    private boolean mIsFavorite;

    public TranslatedItem(String id, String srcLanguage,
                          String trgLanguage,
                          String srcMeaning,
                          String trgMeaning,
                          boolean isFavorite) {
        mId = id;
        mSrcLanguage = srcLanguage;
        mTrgLanguage = trgLanguage;
        mSrcMeaning = srcMeaning;
        mTrgMeaning = trgMeaning;
        mIsFavorite = isFavorite;
    }

    public TranslatedItem(String srcLanguage,
                          String trgLanguage,
                          String srcMeaning,
                          String trgMeaning,
                          boolean isFavorite){
        this(UUID.randomUUID().toString(), srcLanguage,
                trgLanguage, srcMeaning, trgMeaning, isFavorite);
    }



    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getSrcLanguage() {
        return mSrcLanguage;
    }

    public void setSrcLanguage(String srcLanguage) {
        mSrcLanguage = srcLanguage;
    }

    public String getTrgLanguage() {
        return mTrgLanguage;
    }

    public void setTrgLanguage(String trgLanguage) {
        mTrgLanguage = trgLanguage;
    }

    public String getSrcMeaning() {
        return mSrcMeaning;
    }

    public void setSrcMeaning(String srcMeaning) {
        mSrcMeaning = srcMeaning;
    }

    public String getTrgMeaning() {
        return mTrgMeaning;
    }

    public void setTrgMeaning(String trgMeaning) {
        mTrgMeaning = trgMeaning;
    }

    public boolean isFavorite() {
        return mIsFavorite;
    }

    public void setFavorite(boolean favorite) {
        mIsFavorite = favorite;
    }


}
