/*
 * Copyright (C) 2010-2011 Ruben Lopez
 *
 * This file is part of OTempo - Galician Weather
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package org.otempo.view;

import java.util.ArrayList;

import org.otempo.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * Actividad para mostrar el registro de cambios. Ahora que Google permite especificar cambios en cada versión, perdió un poco de sentido...
 */
public class ChangeLog extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Version[] versions = {
                new Version("3.11", R.array.changelog_3_11),
                new Version("3.10", R.array.changelog_3_10),
                new Version("3.9", R.array.changelog_3_9),
                new Version("3.8", R.array.changelog_3_8),
                new Version("3.7", R.array.changelog_3_7),
                new Version("3.6", R.array.changelog_3_6),
                new Version("3.5", R.array.changelog_3_5),
                new Version("3.4", R.array.changelog_3_4),
                new Version("3.3", R.array.changelog_3_3),
                new Version("3.2", R.array.changelog_3_2),
                new Version("3.1", R.array.changelog_3_1),
                new Version("3.0", R.array.changelog_3_0),
                new Version("2.8", R.array.changelog_2_8),
                new Version("2.7", R.array.changelog_2_7),
                new Version("2.6", R.array.changelog_2_6),
                new Version("2.5", R.array.changelog_2_5),
        };

        ArrayList<String> changes = new ArrayList<>();
        for (Version version : versions) {
            version.addToArray(changes);
        }
        setListAdapter(new ArrayAdapter<>(this, R.layout.changelog_item, changes));

        getListView().setTextFilterEnabled(true);
    }

    private class Version {
        Version(String versionName, int versionChangesR) {
            _versionName = versionName;
            _versionChangesR = versionChangesR;
        }

        void addToArray(ArrayList<String> changes) {
            changes.add(_versionName);
            for (String change : getResources().getStringArray(_versionChangesR)) {
                changes.add(" - " + change);
            }
        }

        private final String _versionName;
        private final int _versionChangesR;
    }
}
