package com.codepath.confetti.utlils;

import android.content.res.Resources;
import android.util.TypedValue;

public class Conversion {

    public static float convertDptoFloat(Float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    };
}
