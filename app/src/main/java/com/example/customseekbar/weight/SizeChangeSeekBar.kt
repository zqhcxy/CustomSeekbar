package com.example.customseekbar.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.customseekbar.CommonUtil
import kotlin.math.roundToInt

/**
 * 大小变化的 seekbar
 * @author zqhcxy 2022/01/04
 */
class SizeChangeSeekBar : View {

    private val TAG = "SizeChangeSeekBar"

    private val paint: Paint = Paint()

    //  水平自行内缩距离「绘制从中间开始」
    private var HORIZONTAL_PADDING = 0

    // 当前画布的 大小 数据
    private var canvasClipBounds = Rect()

    //seekbar progress 画笔 宽度
    private var seekbarPaintWidth = 0

    //滑块的 描边 画笔宽度
    private var thumbRingPaintWidth = 0

    private var thumbRadius = 0f

    //seekbar 的绘制宽度
    private var seekbarWdith = 0

    private val minCircleRadiu = 5f
    private var maxCircleRadiu = 26f

    var seekbarInterface: CenterSeekbarInterface? = null

    private var progress = 0

    var maxProgress = 100

    var minProgress = 0

    var progressColocr = Color.LTGRAY
    var thumbColor = Color.WHITE

    var enable = true

    constructor(context: Context) :
            this(context, null)

    constructor(context: Context, attes: AttributeSet?) : super(context, attes) {
        init()
    }


    private fun init() {


        seekbarPaintWidth = CommonUtil.dip2px(context, 3f)
        thumbRingPaintWidth = CommonUtil.dip2px(context, 4f)
        HORIZONTAL_PADDING = CommonUtil.dip2px(context, 40f)

        maxCircleRadiu = CommonUtil.dip2px(context, 12f).toFloat()
        thumbRadius = maxCircleRadiu

        paint.setColor(progressColocr)
        paint.isAntiAlias = true
        paint.strokeWidth = seekbarPaintWidth.toFloat()
        paint.style = Paint.Style.FILL

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)

        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        var w = widthSpecSize
        var h = heightSpecSize

        //都是自适应 warp, 默认 高为30dp
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            h = CommonUtil.dip2px(context, 30f)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {//只有宽自适应
            h = heightSpecSize
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {//只有高自适应
            h = CommonUtil.dip2px(context, 30f)
        }
        setMeasuredDimension(w, h)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        canvas.getClipBounds(canvasClipBounds)

        seekbarWdith = canvasClipBounds.width() - paddingLeft - paddingRight - HORIZONTAL_PADDING

        val centerX = canvasClipBounds.centerX().toFloat()
        val centerY = canvasClipBounds.centerY().toFloat()

        val startX = centerX - seekbarWdith / 2f

        //根据进度计算出 滑块位置
        val progressPosition = startX + progress * seekbarWdith / (maxProgress - minProgress)

        //绘制 背景 「头尾两个圆+梯形」

        paint.setColor(progressColocr)

        canvas.drawCircle(startX, centerY, minCircleRadiu, paint)
        canvas.drawCircle(startX + seekbarWdith, centerY, maxCircleRadiu, paint)

        val path = Path()
        path.moveTo(startX, centerY - minCircleRadiu)//起点
        path.lineTo(startX, centerY + minCircleRadiu)
        path.lineTo(startX + seekbarWdith, centerY + maxCircleRadiu)
        path.lineTo(startX + seekbarWdith, centerY - maxCircleRadiu)

        canvas.drawPath(path, paint)

        //绘制进度「可选」


        //绘制滑块
        paint.setColor(thumbColor)

        canvas.drawCircle(progressPosition, centerY, thumbRadius, paint)

        //绘制描边


    }


    private var lastProgress = 0
    private var lastTouchX = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!enable) return false
        if (event == null) return false

        val rawx = event.rawX
        val action = event.action

        if (action == MotionEvent.ACTION_DOWN) {
            //按下，滑块要放大，即 mThumbRadiu 要变大
            //转换 当前点击位置成 progress值
            //更新 最新点击位置的 paogress 值到内存
            //通知 接口

            thumbRadius += 6f
            progress = transformProgress((rawx - canvasClipBounds.left).roundToInt())
            lastProgress = progress
            invalidate()

            Log.i(TAG, "onTouchEvent ACTION_DOWN progress = $progress")

            //接口
            seekbarInterface?.onStartTrackingTouch(this)

        } else if (action == MotionEvent.ACTION_MOVE) {
            if (lastTouchX == rawx) {
                return true
            }
            lastTouchX = rawx
            progress = transformProgress((rawx - canvasClipBounds.left).roundToInt())

            if (lastProgress != progress) {
                lastProgress = progress
                invalidate()

                Log.i(TAG, "onTouchEvent ACTION_MOVE progress = $progress")
                //接口
                seekbarInterface?.onProgressChanged(this, progress, true)
            }


        } else if (action == MotionEvent.ACTION_UP) {

            thumbRadius = maxCircleRadiu
            invalidate()

            //接口
            seekbarInterface?.onStopTrackingTouch(this)
        }


        return true
    }

    private fun transformProgress(progressValue: Int): Int {
        var result: Int

        val centerX = canvasClipBounds.centerX()
        val min = centerX - seekbarWdith / 2f
        val max = centerX + seekbarWdith / 2f

        if (progressValue > max) {
            result = maxProgress
        } else if (progressValue < min) {
            result = minProgress
        } else {// 根据 滑动的距离，在长度的所占比值，去计算 进度值 的所占值
            result =
                ((maxProgress - minProgress) * (progressValue - min) / seekbarWdith).roundToInt()
        }


        return result
    }

    fun setProgress(progress:Int){
        this.progress = progress
        invalidate()

        seekbarInterface?.onProgressChanged(this, progress, false)
    }

    fun getProgress():Int{
        return progress
    }

    interface CenterSeekbarInterface {
        fun onProgressChanged(seekbar: SizeChangeSeekBar, progress: Int, formUser: Boolean)
        fun onStartTrackingTouch(seekbar: SizeChangeSeekBar)
        fun onStopTrackingTouch(seekbar: SizeChangeSeekBar)
    }


}