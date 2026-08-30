package com.kdt.mcgui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import net.ashmeet.hyperlauncher.R;
import net.kdt.pojavlaunch.Tools;

public class MineEditText extends TextInputEditText {
	public MineEditText(@NonNull Context ctx) {
		super(ctx);
		init();
	}

	public MineEditText(@NonNull Context ctx, @Nullable AttributeSet attrs) {
		super(ctx, attrs);
		init();
	}

	public MineEditText(@NonNull Context ctx, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(ctx, attrs, defStyleAttr);
		init();
	}

	private void init() {
		setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));
		setHintTextColor(ContextCompat.getColor(getContext(), R.color.secondary_text));
		int padding = (int) Tools.dpToPx(12);
		setPadding(padding, padding, padding, padding);
		setBackgroundResource(R.drawable.background_card);
	}
}
