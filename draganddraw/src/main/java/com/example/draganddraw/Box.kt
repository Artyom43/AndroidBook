package com.example.draganddraw

import android.graphics.PointF

class Box (val start: PointF) {

    var end: PointF = start

    val left: Float
        get() = Math.min(start.x, end.y)

    val right: Float
        get() = Math.min(start.x, end.y)
    val top: Float
        get() = Math.min(start.x, end.y)
    val bottom: Float
        get() = Math.min(start.x, end.y)
}