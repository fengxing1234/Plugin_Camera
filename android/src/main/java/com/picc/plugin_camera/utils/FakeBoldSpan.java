package com.picc.plugin_camera.utils;

import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class FakeBoldSpan extends CharacterStyle {

    private int color;

    public FakeBoldSpan(int color) {
        this.color = color;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        //tp.setFakeBoldText(true);//一种伪粗体效果，比原字体加粗的效果弱一点
        tp.setStyle(Paint.Style.FILL_AND_STROKE);
        tp.setColor(color);//字体颜色
        tp.setStrokeWidth(1);//控制字体加粗的程度
    }
}
