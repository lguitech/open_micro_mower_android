/*********************************************************************
 *
 *  This file is part of the [OPEN_MICRO_MOWER_ANDROID] project.
 *  Licensed under the MIT License for non-commercial purposes.
 *  Author: Brook Li
 *  Email: lguitech@126.com
 *
 *  For more details, refer to the LICENSE file or contact [lguitech@126.com].
 *
 *  Commercial use requires a separate license.
 *
 *  This software is provided "as is", without warranty of any kind.
 *
 *********************************************************************/

package com.micronavi.mower.component;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import com.micronavi.mower.R;

public class MRImageButton extends Button {

    private int textColor = 0;
    private int textSize = 0;
    private String strText = "";
    private int resourceId = 0;

    private Drawable buttonDrawable;
    private Bitmap mButtonBitmap, bmpRes;

    private Paint mPaintBitmapButton;


    public MRImageButton(Context context) {
        super(context,null);
    }

    public MRImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setClickable(true);
        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MRImageButton,
                0, 0
        );

        textColor = styledAttributes.getColor(R.styleable.MRImageButton_MRBTN_textColor,0x0);
        textSize = (int) styledAttributes.getDimension(R.styleable.MRImageButton_MRBTN_textSize, 24);
        strText = styledAttributes.getString(R.styleable.MRImageButton_MRBTN_text);
        buttonDrawable = styledAttributes.getDrawable(R.styleable.MRImageButton_MRBTN_image);

        if (buttonDrawable != null) {
            if (buttonDrawable instanceof BitmapDrawable) {
                mButtonBitmap = ((BitmapDrawable) buttonDrawable).getBitmap();
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub

        int view_width = this.getMeasuredWidth();
        int view_height = this.getMeasuredHeight();

        int bmp_width = mButtonBitmap.getWidth();
        int bmp_height = mButtonBitmap.getHeight();

        Matrix matrix = new Matrix();
        int dst_width = view_width / 3;
        int dst_height = view_height / 3;

        float sx = (float)dst_width / (float)bmp_width;
        float sy = (float)dst_height / (float)bmp_height;

        matrix.postScale(sy, sy);

        Bitmap bmpRes = Bitmap.createBitmap(mButtonBitmap, 0,0,bmp_width, bmp_height, matrix, true);

        int dx = (int) ((view_width - bmp_width * sy )/2);
        int dy = (view_height - dst_height)/3;

        canvas.drawBitmap(bmpRes, dx, dy, null);

        Paint paint  = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);

        float textWidth = paint.measureText(strText);
        float x = (view_width - textWidth) / 2;
        float y = view_height * 3 / 4 + Math.abs(paint.ascent() + paint.descent()) / 2;
        canvas.drawText(strText, x, y, paint);
        super.onDraw(canvas);
    }
}
