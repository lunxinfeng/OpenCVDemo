package com.izis.yzext

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dlg_setting.*

class SettingDialog(context: Context?, themeResId: Int) : Dialog(context, themeResId) {
    private val game = GameInfo()
    var listener:OnClickListener? = null

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_setting)

        rb_19.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.boardSize = 19 }
        rb_13.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.boardSize = 13 }
        rb_9.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.boardSize = 9 }
//        rb_start.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.type = 1 }
//        rb_middle.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.type = 2 }
        rb_black.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.bw = 1 }
        rb_white.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.bw = 2 }
//        rb_black2.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.nextBW = 1 }
//        rb_white2.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.nextBW = 2 }
        btnStart.setOnClickListener {
            listener?.onPositive(game)
            dismiss()
        }

        rb_19.isChecked = true
//        rb_start.isChecked = true
        rb_black.isChecked = true
//        rb_black2.isChecked = true
    }

    interface OnClickListener{
        fun onPositive(gameInfo: GameInfo)
    }
}