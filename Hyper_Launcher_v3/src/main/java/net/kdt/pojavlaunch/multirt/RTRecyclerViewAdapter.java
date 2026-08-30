package net.kdt.pojavlaunch.multirt;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.kdt.pojavlaunch.Architecture;
import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.Tools;
import com.ashmeet.hyperlauncher.LauncherPreference.Preference.LauncherPreferences;

import java.io.IOException;
import java.util.List;

public class RTRecyclerViewAdapter extends RecyclerView.Adapter<RTRecyclerViewAdapter.RTViewHolder> {

    public interface OnRuntimeSelectedListener {
        void onRuntimeSelected(Runtime runtime);
        void onRuntimeDeleted();
    }

    private OnRuntimeSelectedListener mListener;
    private boolean mIsDeleting = false;

    public void setOnRuntimeSelectedListener(OnRuntimeSelectedListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public RTViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recyclableView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multirt_runtime,parent,false);
        return new RTViewHolder(recyclableView);
    }

    @Override
    public void onBindViewHolder(@NonNull RTViewHolder holder, int position) {
        final List<Runtime> runtimes = MultiRTUtils.getRuntimes();
        holder.bindRuntime(runtimes.get(position),position);
    }

    @Override
    public int getItemCount() {
        return MultiRTUtils.getRuntimes().size();
    }

    public boolean isDefaultRuntime(Runtime rt) {
        return LauncherPreferences.PREF_DEFAULT_RUNTIME.equals(rt.name);
    }

    @SuppressLint("NotifyDataSetChanged") //not a problem, given the typical size of the list
    public void setDefault(Runtime rt){
        LauncherPreferences.PREF_DEFAULT_RUNTIME = rt.name;
        LauncherPreferences.DEFAULT_PREF.edit().putString("defaultRuntime",LauncherPreferences.PREF_DEFAULT_RUNTIME).apply();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged") //not a problem, given the typical size of the list
    public void setIsEditing(boolean isEditing) {
        mIsDeleting = isEditing;
        notifyDataSetChanged();
    }

    public boolean getIsEditing(){
        return mIsDeleting;
    }


    public class RTViewHolder extends RecyclerView.ViewHolder {
        final TextView mJavaVersionTextView;
        final TextView mFullJavaVersionTextView;
        final ColorStateList mDefaultColors;
        final ImageView mSelectedIcon;
        final ImageButton mDeleteButton;
        final Context mContext;
        Runtime mCurrentRuntime;
        int mCurrentPosition;

        public RTViewHolder(View itemView) {
            super(itemView);
            mJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version);
            mFullJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version_full);
            mSelectedIcon = itemView.findViewById(R.id.multirt_view_selected_icon);
            mDeleteButton = itemView.findViewById(R.id.multirt_view_removebtn);

            mDefaultColors =  mFullJavaVersionTextView.getTextColors();
            mContext = itemView.getContext();

            setupOnClickListeners();
        }

        @SuppressLint("NotifyDataSetChanged") // same as all the other ones
        private void setupOnClickListeners(){
            itemView.setOnClickListener(v -> {
                if (mIsDeleting) return;
                if(mCurrentRuntime != null && !isDefaultRuntime(mCurrentRuntime)) {
                    setDefault(mCurrentRuntime);
                    if (mListener != null) mListener.onRuntimeSelected(mCurrentRuntime);
                }
            });

            mDeleteButton.setOnClickListener(v -> {
                if (mCurrentRuntime == null) return;

                if(MultiRTUtils.getRuntimes().size() < 2) {
                    new MaterialAlertDialogBuilder(mContext)
                            .setTitle(R.string.global_error)
                            .setMessage(R.string.multirt_config_removeerror_last)
                            .setPositiveButton(android.R.string.ok,(adapter, which)->adapter.dismiss())
                            .show();
                    return;
                }

                sExecutorService.execute(() -> {
                    try {
                        MultiRTUtils.removeRuntimeNamed(mCurrentRuntime.name);
                        mDeleteButton.post(() -> {
                            if(getBindingAdapter() != null)
                                getBindingAdapter().notifyDataSetChanged();
                            if (mListener != null) mListener.onRuntimeDeleted();
                        });

                    } catch (IOException e) {
                        Tools.showError(itemView.getContext(), e);
                    }
                });

            });
        }

        public void bindRuntime(Runtime runtime, int pos) {
            mCurrentRuntime = runtime;
            mCurrentPosition = pos;
            if(runtime.versionString != null && Tools.DEVICE_ARCHITECTURE == Architecture.archAsInt(runtime.arch)) {
                mJavaVersionTextView.setText(runtime.name
                        .replace(".tar.xz", "")
                        .replace("-", " "));
                mFullJavaVersionTextView.setText(runtime.versionString);
                mFullJavaVersionTextView.setTextColor(mDefaultColors);

                updateButtonsVisibility();

                boolean defaultRuntime = isDefaultRuntime(runtime);
                mSelectedIcon.setVisibility(defaultRuntime && !mIsDeleting ? View.VISIBLE : View.GONE);
                itemView.setSelected(defaultRuntime);
                return;
            }

            // Problematic runtime moment, force propose deletion
            mDeleteButton.setVisibility(View.VISIBLE);
            mSelectedIcon.setVisibility(View.GONE);
            if(runtime.versionString == null){
                mFullJavaVersionTextView.setText(R.string.multirt_runtime_corrupt);
            }else{
                mFullJavaVersionTextView.setText(mContext.getString(R.string.multirt_runtime_incompatiblearch, runtime.arch));
            }
            mJavaVersionTextView.setText(runtime.name);
            mFullJavaVersionTextView.setTextColor(Color.RED);
        }

        private void updateButtonsVisibility(){
            mDeleteButton.setVisibility(mIsDeleting ? View.VISIBLE : View.GONE);
            if (mIsDeleting) {
                mSelectedIcon.setVisibility(View.GONE);
            }
        }
    }
}
