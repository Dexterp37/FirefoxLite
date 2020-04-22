/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.urlinput;

import androidx.annotation.NonNull;

public class UrlInputPresenter implements UrlInputContract.Presenter {

    private UrlInputContract.View view;

    @Override
    public void setView(UrlInputContract.View view) {
        this.view = view;
    }

    @Override
    public void onInput(@NonNull CharSequence input, boolean isThrottled) {
        if (view == null) {
            return;
        }

        if (input.length() == 0) {
            this.view.setQuickSearchVisible(false);
            return;
        }
        this.view.setQuickSearchVisible(true);
    }
}
