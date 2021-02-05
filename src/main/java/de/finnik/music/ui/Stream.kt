package de.finnik.music.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import android.widget.VideoView
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import de.finnik.music.R
import java.net.URL

class Stream(val context: Context, val info: VideoInfo) {
    val uri: Uri = Uri.parse(info.formats.filter { it.formatId == "18" }.first().url)
    val videoView: VideoView
    //val dialog: Dialog
    val popupWindow: PopupWindow
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.player, null)
        videoView = view.findViewById(R.id.vv_player)
        videoView.setVideoURI(uri)


        popupWindow = PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, false)
        popupWindow.windowLayoutType = WindowManager.LayoutParams.FIRST_SUB_WINDOW

        var dx: Int = 500
        var dy: Int = 500
        var offsetx: Int = 0
        var offsety: Int = 0
        view.setOnTouchListener(View.OnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dx = event.rawX.toInt()
                    dy = event.rawY.toInt()
                    Log.i("TAG", "down: $dx $dy")
                    v.performClick()
                }
                MotionEvent.ACTION_MOVE -> {
                    offsetx = (event.rawX - dx).toInt()
                    offsety = (event.rawY - dy).toInt()

                    val x: Int = (event.rawX - view.measuredWidth /2).toInt()
                    val y: Int = (event.rawY - view.measuredHeight /2).toInt()
                    Log.i("TAG", ": ${view.measuredWidth} ${view.measuredHeight}")

                    Log.i("TAG", ": $offsetx $offsety ${event.rawX} ${event.rawY} $dx $dy $x $y")
                    popupWindow.update(x,y, -1, -1 ,true)
                }

            }
            true
        })
        //dialog = AlertDialog.Builder(context).setView(view).create()
    }
    fun show(view: View) {
        //dialog.show()
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY,0,0)

        videoView.start()
    }

}