package org.otempo.util;

import android.content.Context;

public class LayoutUtils {
	/**
	 * Converts from density-independent-pixels to actual pixels.
	 * @param dips The metric in dips
	 * @param context The context which holds display metrics info.
	 * @return The number of pixels.
	 */
	public static int dips(float dips, Context context) {
		return Math.round(context.getResources().getDisplayMetrics().density * dips);
	}
}
