package org.fcitx.fcitx5.android.input.clipboard

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.dimensions.dp
import splitties.views.dsl.core.styles.AndroidStyles
import splitties.views.dsl.constraintlayout.*
import splitties.views.dsl.core.*
import splitties.views.imageResource
import splitties.views.setPaddingDp

sealed class ClipboardInstructionUi(override val ctx: Context, protected val theme: Theme) : Ui {

    class Enable(ctx: Context, theme: Theme) : ClipboardInstructionUi(ctx, theme) {

        private val androidStyles = AndroidStyles(ctx)

        private val instructionText = textView {
            setText(R.string.instruction_enable_clipboard_listening)
            setPaddingDp(12, 8, 12, 8)
            setTextColor(theme.keyTextColor)
        }

        val enableButton = androidStyles.button.borderless {
            setText(R.string.clipboard_enable)
            setTextColor(theme.accentKeyBackgroundColor)
        }

        override val root = constraintLayout {
            add(instructionText, lParams(wrapContent, wrapContent) {
                topOfParent()
                startOfParent()
                endOfParent()
            })
            add(enableButton, lParams(wrapContent, wrapContent) {
                below(instructionText)
                endOfParent(dp(8))
            })
        }
    }

    class Empty(ctx: Context, theme: Theme) : ClipboardInstructionUi(ctx, theme) {

        private val icon = imageView {
            imageResource = R.drawable.ic_baseline_content_paste_24
            colorFilter = PorterDuffColorFilter(theme.altKeyTextColor, PorterDuff.Mode.SRC_IN)
        }

        private val instructionText = textView {
            setText(R.string.instruction_copy)
            setTextColor(theme.keyTextColor)
        }

        override val root = constraintLayout {
            add(icon, lParams(dp(90), dp(90)) {
                topOfParent(dp(24))
                startOfParent()
                endOfParent()
            })
            add(instructionText, lParams(wrapContent, wrapContent) {
                below(icon, dp(16))
                startOfParent()
                endOfParent()
            })
        }
    }
}
