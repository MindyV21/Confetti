package com.codepath.confetti.models;

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
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.Conversion;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

/**
 * Class to store data for predictions on a note image file
 */
public class PinView extends SubsamplingScaleImageView {

    public static final String TAG = "PinView";

    private final Paint paint = new Paint();
    private final PointF vPin = new PointF();
    private ArrayList<PointF> sPin = new ArrayList<>();
    private ArrayList<Prediction> pins = new ArrayList<>();
    private Bitmap pin;

    public PinView(Context context) {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    /**
     * Set a pin on the image file
     * @param sPin
     * @param prediction
     */
    public void setPin(PointF sPin, Prediction prediction) {
        this.sPin.add(sPin);
        pins.add(prediction);
        initialise();
        invalidate();
    }

    /**
     * Gets the coordinates for a specified pin
     * @param prediction
     * @return
     */
    public PointF getPin(Prediction prediction) {
        if (sPin.size() == 0) return null;
        return sPin.get(pins.indexOf(prediction));
    }

    /**
     * Removes pin from the image file
     * @param prediction
     * @return
     */
    public boolean removePin(Prediction prediction){
        if (pins.contains(prediction)){
            sPin.remove(pins.indexOf(prediction));
            pins.remove(prediction);
            initialise();
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes all pins from canvas
     */
    public void removeAllPins() {
        sPin.clear();
        pins.clear();
        initialise();
        invalidate();
    }

    /**
     * Retrieve all pin predictions
     * @return
     */
    public ArrayList<Prediction> getPinPredictions(){
        return pins;
    }

    /**
     * init pin properties
     */
    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
        pin = Conversion.getBitmapFromVectorDrawable(this.getContext(), R.drawable.arrow_right_drop_circle);
        float w = (density/420f) * pin.getWidth();
        float h = (density/420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);
    }

    /**
     * Draws pins onto the image file (canvas)
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        for (PointF point : sPin) {
            if (point != null && pin != null) {
                PointF vPin = sourceToViewCoord(point);
                float vX = vPin.x - (pin.getWidth() / 2);
                float vY = vPin.y - pin.getHeight();
                canvas.drawBitmap(pin, vX, vY, paint);
            }
        }

    }
}