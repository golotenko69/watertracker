package com.watertracker.app.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.watertracker.app.R

/**
 * Строка выбора варианта сортировки с галочкой (аналог RadioButton, но
 * в едином фирменном стиле приложения, как на макете экрана настроек).
 */
class SortOptionRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val ivCheck: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_sort_option_row, this, true)
        val tvLabel = findViewById<TextView>(R.id.tv_option_label)
        ivCheck = findViewById(R.id.iv_check)

        context.theme.obtainStyledAttributes(attrs, R.styleable.SortOptionRow, 0, 0).apply {
            try {
                tvLabel.text = getString(R.styleable.SortOptionRow_optionLabel)
            } finally {
                recycle()
            }
        }
    }

    fun setSelected(selected: Boolean) {
        ivCheck.visibility = if (selected) View.VISIBLE else View.INVISIBLE
    }
}
