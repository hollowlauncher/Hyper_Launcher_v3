package com.kdt.mcgui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.button.MaterialButton;

import net.ashmeet.hyperlauncher.R;

public class MineButton extends MaterialButton {
	
	public MineButton(Context ctx) {
		this(ctx, null);
	}
	
	public MineButton(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init();
	}

	private void init() {
		setTypeface(ResourcesCompat.getFont(getContext(), R.font.noto_sans_bold));

		setInsetTop(0);
		setInsetBottom(0);

		setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.minebutton_color, null));
		setTextColor(ResourcesCompat.getColor(getResources(), R.color.minebutton_text_color, null));
		setRippleColorResource(R.color.control_button_pressed_color);
		setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen._13ssp));
		setElevation(0f);
		setAllCaps(false);
	}

}
