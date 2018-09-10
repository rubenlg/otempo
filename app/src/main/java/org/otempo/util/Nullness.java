package org.otempo.util;

import android.support.annotation.Nullable;

public class Nullness {
	public static <T> T checkNotNull(@Nullable T e){
	   if(e == null){
	      throw new NullPointerException();
	   }
	   return e;
	} 
	public static <T> boolean equals(@Nullable T a, @Nullable T b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}
}
