package com.anubhav.imageviewplayground.customcirclesview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import coil.ImageLoader
import coil.request.ImageRequest
import coil.target.Target
import coil.transform.CircleCropTransformation
import com.anubhav.imageviewplayground.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.math.min

class ImageCirclesView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var radius: Float = 0f
    private var leftImageUrl: String? = null
    private var rightImageUrl: String? = null
    private var imageLibrary: Int = 0 // Default to Glide

    private val bitmapPaint = Paint()
    private var leftBitmap: Bitmap? = null
    private var rightBitmap: Bitmap? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageCirclesView)

        radius = typedArray.getDimension(R.styleable.ImageCirclesView_radius, 0f)
        leftImageUrl = typedArray.getString(R.styleable.ImageCirclesView_leftImageUrl)
        rightImageUrl = typedArray.getString(R.styleable.ImageCirclesView_rightImageUrl)
        imageLibrary = typedArray.getInt(R.styleable.ImageCirclesView_imageLibrary, 0)

        typedArray.recycle()

        /*if (leftImageUrl != null || rightImageUrl != null) {*/
        loadImage(leftImageUrl, true)
        loadImage(rightImageUrl, false)
        /*}*/
    }

    private fun loadImage(url: String?, isLeft: Boolean) {
        when (imageLibrary) {
            0 -> { // Glide
                Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .circleCrop()
                    .override((2 * radius).toInt(), (2 * radius).toInt())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            //val scaledBitmap = scaleBitmapToSize(resource, (2 * radius).toInt())
                            if (isLeft) {
                                leftBitmap = resource
                            } else {
                                rightBitmap = resource
                            }
                            invalidate() // Redraw the view
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}

                    })
            }
            1 -> { // Coil
                val size = (3 * radius).toInt()
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(size)
                    .transformations(CircleCropTransformation())
                    .target(object : Target {
                        override fun onSuccess(result: Drawable) {
                            if (result is BitmapDrawable) {
                                if (isLeft) {
                                    leftBitmap = result.bitmap
                                } else {
                                    rightBitmap = result.bitmap
                                }
                                invalidate() // Redraw the view
                            }
                        }

                        override fun onError(error: Drawable?) {
                            // Handle errors, if needed
                        }
                    })
                    .build()
                val imageLoader = ImageLoader(context)
                imageLoader.enqueue(request)
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate the x-coordinates for center of circles
        val leftCircleX = width / 4f
        val rightCircleX = 3 * width / 4f

        // Use centerY for both circles
        val centerY = height / 2f

        leftBitmap?.let {
            val squareBitmap = getCentralSquareBitmap(it)
            bitmapPaint.isAntiAlias = true
            val shader = BitmapShader(squareBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            bitmapPaint.shader = shader
            canvas.drawCircle(leftCircleX, centerY, radius, bitmapPaint)
        }

        rightBitmap?.let {
            val squareBitmap = getCentralSquareBitmap(it)
            bitmapPaint.isAntiAlias = true
            val shader = BitmapShader(squareBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            bitmapPaint.shader = shader
            canvas.drawCircle(rightCircleX, centerY, radius, bitmapPaint)
        } // Drawing as circles

        leftBitmap?.let {
            val bitmapX = leftCircleX - it.width / 2
            val bitmapY = centerY - it.height / 2

            canvas.drawBitmap(it, bitmapX, bitmapY, bitmapPaint)
        }

        rightBitmap?.let {
            val bitmapX = rightCircleX - it.width / 2
            val bitmapY = centerY - it.height / 2

            canvas.drawBitmap(it, bitmapX, bitmapY, bitmapPaint)
        }
    }

    private fun getCentralSquareBitmap(original: Bitmap): Bitmap {
        val dimension = min(original.width, original.height)
        val xOffset = (original.width - dimension) / 2
        val yOffset = (original.height - dimension) / 2

        return Bitmap.createBitmap(original, xOffset, yOffset, dimension, dimension)
    }
}
