/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2017, 2018 Christopher Blay <chris.b.blay@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.covertbagel.neko;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public final class NekoTile extends TileService implements PrefState.PrefsListener {

    private PrefState mPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = new PrefState(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        mPrefs.setListener(this);
        updateState();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        mPrefs.setListener(null);
    }

    @Override
    public void onPrefsChanged() {
        updateState();
    }

    private void updateState() {
        final Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        final int foodState = mPrefs.getFoodState();
        final Food food = new Food(foodState);
        if (foodState != 0) {
            NekoService.registerJobIfNeeded(this, food.getInterval(this));
        }
        tile.setIcon(food.getIcon(this));
        tile.setLabel(food.getName(this));
        tile.setState(foodState != 0 ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onClick() {
        if (mPrefs.getFoodState() != 0) {
            // there's already food loaded, let's empty it
            mPrefs.setFoodState(0);
            NekoService.cancelJob(this);
        } else {
            // time to feed the cats
            if (isLocked()) {
                if (isSecure()) {
                    Intent intent = new Intent(this, NekoLockedActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityAndCollapse(intent);
                } else {
                    unlockAndRun(this::showNekoDialog);
                }
            } else {
                showNekoDialog();
            }
        }
    }

    private void showNekoDialog() {
        showDialog(new NekoDialog(this));
    }
}
