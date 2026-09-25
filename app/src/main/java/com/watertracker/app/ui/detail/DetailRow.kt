package com.watertracker.app.ui.detail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.watertracker.app.R

/**
 * Компактная составная View: "Подпись ... значение" + разделитель.
 * Используется на экране "Детальный просмотр" для всех полей записи.
 */
class DetailRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val tvLabel: TextView
    private val tvValue: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_detail_row, this, true)
        tvLabel = findViewById(R.id.tv_label)
        tvValue = findViewById(R.id.tv_value)

        context.theme.obtainStyledAttributes(attrs, R.styleable.DetailRow, 0, 0).apply {
            try {
                tvLabel.text = getString(R.styleable.DetailRow_rowLabel)
            } finally {
                recycle()
            }
        }
    }

    fun setValue(value: String) {
        tvValue.text = value
    }
}
