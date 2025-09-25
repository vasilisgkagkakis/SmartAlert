package com.unipi.gkagkakis.smartalert.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.*;

public class BlurHelper {
    public static Bitmap blur(Context context, Bitmap image, float radius) {
        Bitmap output = Bitmap.createBitmap(image);
        RenderScript rs = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(rs, image);
        Allocation outputAlloc = Allocation.createFromBitmap(rs, output);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blur.setRadius(radius);
        blur.setInput(input);
        blur.forEach(outputAlloc);
        outputAlloc.copyTo(output);
        rs.destroy();
        return output;
    }
}