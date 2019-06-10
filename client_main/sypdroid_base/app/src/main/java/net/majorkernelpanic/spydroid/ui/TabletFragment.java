/*
 * Copyright (C) 2011-2013 GUIGUI Simon, fyhertz@gmail.com
 *
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 *
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.spydroid.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.apparatus.StatusView;

import net.majorkernelpanic.spydroid.R;

/**
 * 显示状态图标的界面
 */
public class TabletFragment extends Fragment {
    //显示图标状态-各种信号状态指示的view
    private StatusView statusView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tablet, container, false);
        statusView = (StatusView) rootView.findViewById(R.id.status_bar);
        statusView.registerStatusBarReceiver();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
