package com.example.customseekbar

import android.content.Context

object CommonUtil {

     fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}