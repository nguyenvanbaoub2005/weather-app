package com.example.textn.ui.view.component

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.max
import kotlin.math.min

/**
 * FloatingActionButton tùy chỉnh cho phép kéo theo chiều dọc
 */
class DraggableFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr) {

    private var dX = 0f
    private var dY = 0f
    private var lastAction = 0
    private var isDraggable = true

    // Giới hạn khoảng di chuyển (chỉ theo chiều dọc)
    private var minY = 0f
    private var maxY = 0f
    private var initialY = 0f

    init {
        // Thiết lập listener cho khi view được gắn vào cửa sổ
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (minY == 0f || maxY == 0f) {
                val parent = parent as? ViewGroup
                parent?.let {
                    // Thiết lập giới hạn di chuyển từ trên xuống dưới của parent
                    initialY = y
                    minY = height.toFloat() // padding từ trên xuống
                    maxY = parent.height - height.toFloat() // padding từ dưới lên
                }
            }
        }
    }

    /**
     * Cho phép hoặc vô hiệu hóa tính năng kéo thả
     */
    fun setDraggable(draggable: Boolean) {
        isDraggable = draggable
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDraggable) {
            return super.onTouchEvent(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dX = x - event.rawX
                dY = y - event.rawY
                lastAction = MotionEvent.ACTION_DOWN
            }
            MotionEvent.ACTION_MOVE -> {
                // Không thay đổi tọa độ x để chỉ di chuyển theo chiều dọc
                val newY = event.rawY + dY

                // Giới hạn trong phạm vi cho phép
                y = max(minY, min(maxY, newY))

                lastAction = MotionEvent.ACTION_MOVE
            }
            MotionEvent.ACTION_UP -> {
                if (lastAction == MotionEvent.ACTION_DOWN) {
                    // Nếu chỉ là click (không di chuyển) thì thực hiện sự kiện click
                    performClick()
                }
            }
            else -> return super.onTouchEvent(event)
        }
        return true
    }
}