package me.rocka.fcitx5test.input.clipboard

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.text.TextUtils
import me.rocka.fcitx5test.R
import splitties.dimensions.dp
import splitties.resources.styledColor
import splitties.views.dsl.constraintlayout.*
import splitties.views.dsl.core.*
import splitties.views.dsl.material.materialCardView
import splitties.views.horizontalPadding
import splitties.views.imageResource
import splitties.views.verticalPadding

class ClipboardUi(override val ctx: Context) : Ui {

    val text = textView {
        maxLines = 4
        textSize = 14f
        verticalPadding = dp(4)
        horizontalPadding = dp(8)
        ellipsize = TextUtils.TruncateAt.END
        setTextColor(styledColor(android.R.attr.colorForeground))
    }

    val pin = imageView {
        imageResource = R.drawable.ic_baseline_push_pin_24
        colorFilter =
            PorterDuffColorFilter(styledColor(android.R.attr.colorForeground), PorterDuff.Mode.SRC_IN)
        alpha = 0.3f
    }

    val wrapper = constraintLayout {
        add(text, lParams(matchParent, wrapContent) {
            topOfParent()
            bottomOfParent()
            startOfParent()
            endOfParent()
        })
        add(pin, lParams(dp(12), dp(12)) {
            bottomOfParent(dp(2))
            endOfParent(dp(2))
        })
    }

    override val root = materialCardView(theme = R.style.Theme_MaterialComponents_DayNight) {
        minimumWidth = dp(40)
        minimumHeight = dp(30)
        add(wrapper, lParams(matchParent, wrapContent))
    }
}