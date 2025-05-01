package com.example.textn.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.textn.R

class FontHelper {

    fun applyGlobalFontSize(activity: Activity, fontSize: Int) {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        applyFontSizeToViewGroup(rootView, fontSize)
    }

    private fun applyFontSizeToViewGroup(viewGroup: ViewGroup, fontSize: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            when (child) {
                is TextView -> {
                    child.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                }
                is ViewGroup -> {
                    applyFontSizeToViewGroup(child, fontSize)
                }
            }
        }
    }

    // Helper to get scaled font size based on text type
    fun getScaledFontSize(context: Context, baseSize: Int, textType: TextType): Float {
        val scale = when (textType) {
            TextType.HEADING -> 1.5f
            TextType.SUBHEADING -> 1.25f
            TextType.BODY -> 1.0f
            TextType.CAPTION -> 0.8f
        }

        return baseSize * scale
    }

    enum class TextType {
        HEADING,
        SUBHEADING,
        BODY,
        CAPTION
    }
}