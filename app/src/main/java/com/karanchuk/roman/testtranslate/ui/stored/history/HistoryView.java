package com.karanchuk.roman.testtranslate.ui.stored.history;

import com.karanchuk.roman.testtranslate.ui.base.BaseView;
import com.karanchuk.roman.testtranslate.data.database.model.TranslatedItem;

import java.util.List;

/**
 * Created by roman on 29.6.17.
 */

public interface HistoryView extends BaseView<HistoryPresenter> {

    void handleShowClearStored();

    void handleShowingSearchView(List<TranslatedItem> items);
}