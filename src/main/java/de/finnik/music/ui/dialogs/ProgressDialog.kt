package de.finnik.music.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import de.finnik.music.R


val list: MutableList<Dialog> = ArrayList()

class ProgressDialog(context: Context, text: String) {
    var dialog: Dialog

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.progress, null)
        view.findViewById<TextView>(R.id.tv_progress).setText(text)
        dialog = AlertDialog.Builder(context).setView(view).create()
    }

    fun show() {
        dialog.show()
        list.add(dialog)
        Log.i("TAG", "show: ${hashCode()}")
    }
    fun hide() {
        dialog.hide()
        dialog.dismiss()
        list.remove(dialog)
        Log.i("TAG", "hide: ${hashCode()}")
    }
    companion object {
        fun hideAll() {
            list.forEach(Dialog::hide)
            list.forEach(Dialog::dismiss)
            list.clear()
        }
    }
}
