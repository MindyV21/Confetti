package com.codepath.confetti.utlils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.codepath.confetti.R;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class PinView extends SubsamplingScaleImageView {

    public static final String TAG = "PinView";

    private final Paint paint = new Paint();
    private final PointF vPin = new PointF();
    private PointF sPin;
    private Bitmap pin;

    public PinView(Context context) {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise(context);
    }

    public void setPin(Context context, PointF sPin) {
        this.sPin = sPin;
        initialise(context);
        invalidate();
    }

    private void initialise(Context context) {
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = getBitmapFromVectorDrawable(context, R.drawable.ic_baseline_arrow_forward_ios_24);
//        pin = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_baseline_person_24);
        float w = (density/420f) * pin.getWidth();
        float h = (density/420f) * pin.getHeight();
        Log.d(TAG, "pin width: " + w + " height: " + h);
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            Log.d(TAG, "photo not ready!!!");
            return;
        }

        paint.setAntiAlias(true);

        if (sPin != null && pin != null) {
            Log.d(TAG, "pin not null");
            sourceToViewCoord(sPin, vPin);
            float vX = vPin.x - (pin.getWidth()/2);
            float vY = vPin.y - pin.getHeight();
            canvas.drawBitmap(pin, vX, vY, paint);
        }

    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}