package net.kdt.pojavlaunch.profiles;

import static net.kdt.pojavlaunch.extra.ExtraCore.getValue;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ExpandableListView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.kdt.pojavlaunch.JVersionList;
import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.extra.ExtraConstants;

public class VersionSelectorDialog {
    public static void open(Context context, boolean hideCustomVersions, VersionSelectorListener listener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.version_select_hint);
        ExpandableListView expandableListView = (ExpandableListView) LayoutInflater.from(context)
                .inflate(R.layout.dialog_expendable_list_view , null);
        JVersionList jVersionList = (JVersionList) getValue(ExtraConstants.RELEASE_TABLE);
        JVersionList.Version[] versionArray;
        if(jVersionList == null || jVersionList.versions == null) versionArray = new JVersionList.Version[0];
        else versionArray = jVersionList.versions;
        VersionListAdapter adapter = new VersionListAdapter(versionArray, hideCustomVersions, context);

        expandableListView.setAdapter(adapter);
        builder.setView(expandableListView);
        androidx.appcompat.app.AlertDialog dialog = builder.show();

        expandableListView.setOnChildClickListener((parent, v1, groupPosition, childPosition, id) -> {
            String version = adapter.getChild(groupPosition, childPosition);
            listener.onVersionSelected(version, adapter.isSnapshotSelected(groupPosition));
            dialog.dismiss();
            return true;
        });
    }
}
