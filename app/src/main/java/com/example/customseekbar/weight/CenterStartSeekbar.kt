package com.example.customseekbar.weight

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

/**
 * 中间为起点，两边滑动的 seekabr
 * @author zqh 2021/12/22
 */
class CenterStartSeekbar : View {

    private val TAG: String = "CenterStartSeekbar"

    private val mBackgroundPaint: Paint = Paint()
    private val mProgressPaint: Paint = Paint()

    // 当前画布的 大小 数据
    private var canvasClipBounds = Rect()

    //Seekbar 的宽度
    private var mSeekbarWidth: Float = 0f

    // seekbar 的背景RectF 数据「用户绘制数据」
    private val seekbarBgRectF: RectF = RectF()

    //progress 的 坐标数据
    private val seekbarProgressRectF: RectF = RectF()

    //seekbar 的宽度
    private var mSeekbarHeight = 6


    //seekbar 进度与背景的 圆角
    private var mProgressRadius = 10f

    //进度 所在位置
    private var mProgressPosition = 0f

    //滑块的半径
    private var mThumbRadiu = 18f

    private var lastProgress = 0
    private var lastTouchX = 0f


    /**
     * 是否是中间开始的 seekbar
     */
    var isCenterSeekbar = true

    /**
     * 进度
     */
    private var progress = 0

    var mMaxProgress = 100
    var mMinProgress = 0

    var mThumbColor = Color.RED
    var mProgressColor = Color.RED
    val enable = true

    var seekbarInterface: CenterSeekbarInterface? = null


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attes: AttributeSet?) : super(context, attes) {
        init()
    }


    private fun init() {
        //初始化 seekbar 地槽 画笔
        mBackgroundPaint.isAntiAlias = true
        mBackgroundPaint.color = Color.DKGRAY
        mBackgroundPaint.style = Paint.Style.FILL;
        mBackgroundPaint.strokeWidth = 8f

        mProgressPaint.isAntiAlias = true
        mProgressPaint.color = mProgressColor
        mProgressPaint.style = Paint.Style.FILL
        mProgressPaint.strokeWidth = 8f

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
            h = dip2px(context, 30f)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {//只有宽自适应
            h = heightSpecSize
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {//只有高自适应
            h = dip2px(context, 30f)
        }
        setMeasuredDimension(w, h)

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //准备 必要基础数据
        canvas?.getClipBounds(canvasClipBounds)
        mSeekbarWidth = canvasClipBounds.width() - 100f - paddingLeft - paddingRight

        val centerX: Float = canvasClipBounds.centerX().toFloat()//中心 X点
        val centerY: Float = canvasClipBounds.centerY().toFloat()//中心 Y点
        var startX = centerX - mSeekbarWidth / 2f //起始位置


        //绘制 背景
        seekbarBgRectF.left = startX
        seekbarBgRectF.top = centerY + mSeekbarHeight
        seekbarBgRectF.right = startX + mSeekbarWidth
        seekbarBgRectF.bottom = centerY

        canvas?.drawRoundRect(seekbarBgRectF, mProgressRadius, mProgressRadius, mBackgroundPaint)

        //绘制 进度
        if (isCenterSeekbar) {
            startX = centerX
            mProgressPosition =
                startX + ((progress * (mSeekbarWidth / 2f)) / (mMaxProgress - mMinProgress))
        } else {
            mProgressPosition = startX + progress * mSeekbarWidth / (mMaxProgress - mMinProgress)
        }

        seekbarProgressRectF.top = centerY + mSeekbarHeight
        seekbarProgressRectF.bottom = centerY

        if (progress > 0) { //右半部分
            seekbarProgressRectF.right = mProgressPosition
            seekbarProgressRectF.left = startX
        } else { //左半部分
            seekbarProgressRectF.right = startX
            seekbarProgressRectF.left = mProgressPosition
        }
        mProgressPaint.setColor(mProgressColor)
        canvas?.drawRoundRect(
            seekbarProgressRectF,
            mProgressRadius,
            mProgressRadius,
            mProgressPaint
        )

        //绘制 滑块
        mProgressPaint.setColor(mThumbColor)
        canvas?.drawCircle(mProgressPosition,
            centerY + mSeekbarHeight / 2f, mThumbRadiu, mProgressPaint)

    }

    /**
     * 触碰监听，更新滑动数据
     */
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

            mThumbRadiu = 24f
            progress = transformProgress((rawx - canvasClipBounds.left).roundToInt())
            lastProgress = progress
            invalidate()

            Log.i(TAG, "onTouchEvent ACTION_DOWN progress = $progress")

            //接口
            seekbarInterface?.onStartTrackingTouch(this)

        } else if (action == MotionEvent.ACTION_MOVE) {
            //lastTouchX 与 lastProgress 为了减少不必要的重复数据

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

            mThumbRadiu = 18f
            invalidate()

            //接口
            seekbarInterface?.onStopTrackingTouch(this)
        }


        return true
    }

    /**
     * 点击seekbar的位置，转换为 进度。
     * <p>
     * return  progress
     * -maxProgress          minProgress              maxProgress
     * \------------------------0---------------------------\
     * min                   center     touch-->\          max
     * (min center touch max are positions in the screen)
     * 计算原理：
     * touch progress =  (touch - center) / (max - center) * maxProgress;
     *
     * @param progressValue 点击的位置
     */
    private fun transformProgress(progressValue: Int): Int {
        var result: Int

        val centerX = canvasClipBounds.width() / 2f
        val min = centerX - mSeekbarWidth / 2f
        val max = centerX + mSeekbarWidth / 2f

        if (isCenterSeekbar) {


            if (progressValue > centerX) {//右边
                if (progressValue > max) {
                    result = mMaxProgress
                } else {
                    result = ((progressValue - centerX) * (mMaxProgress - mMinProgress) / (mSeekbarWidth / 2f)).roundToInt();
                }

            } else if (progressValue < centerX) {//左边
                if (progressValue < min) {
                    result = -mMaxProgress
                } else {
                    result =
                        (((progressValue - centerX) / (mSeekbarWidth / 2f) * (mMaxProgress - mMinProgress)).roundToInt());
                }
            } else {
                result = mMinProgress
            }
        } else {
            val centerX = canvasClipBounds.centerX()
            val min = centerX - mSeekbarWidth / 2f
            val max = centerX + mSeekbarWidth / 2f

            if (progressValue > max) {
                result = mMaxProgress
            } else if (progressValue < min) {
                result = mMinProgress
            } else {// 根据 滑动的距离，在长度的所占比值，去计算 进度值 的所占值
                result =
                    ((mMaxProgress - mMinProgress) * (progressValue - min) / mSeekbarWidth).roundToInt()
            }

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

    private fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


    interface CenterSeekbarInterface {
        fun onProgressChanged(seekbar: CenterStartSeekbar, progress: Int, formUser: Boolean)
        fun onStartTrackingTouch(seekbar: CenterStartSeekbar)
        fun onStopTrackingTouch(seekbar: CenterStartSeekbar)
    }

}