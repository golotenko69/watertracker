package com.watertracker.app.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.watertracker.app.R

/**
 * Диалог подтверждения удаления (раздел 2.4 ТЗ: "Вы уверены, что хотите удалить?").
 * Используется на экране детального просмотра.
 */
object DialogUtils {
    fun confirmDelete(context: Context, onConfirmed: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.delete_confirm_title))
            .setMessage(context.getString(R.string.delete_confirm_message))
            .setNegativeButton(context.getString(R.string.cancel), null)
            .setPositiveButton(context.getString(R.string.delete)) { dialog, _ ->
                onConfirmed()
                dialog.dismiss()
            }
            .show()
    }
}
