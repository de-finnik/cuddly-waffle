package de.finnik.music.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import de.finnik.music.R
import de.finnik.music.songs.Song
import java.io.File
import java.util.function.Consumer

class RenameDialog(context: Context, song: Song, dir: File, dialogDismiss: Consumer<Song.SongInfo>) {
    private val dialog: AlertDialog

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_rename_song, null)
        val et_title = view.findViewById<EditText>(R.id.et_title)
        val et_artist = view.findViewById<EditText>(R.id.et_artist)
        val btn_submit = view.findViewById<Button>(R.id.btn_submit)

        et_title.setText(song.title)
        et_artist.setText(song.artist)

        fun findWrongInput(): Boolean {
            return et_title.text.isNullOrEmpty() || et_artist.text.isNullOrEmpty()
        }

        dialog = AlertDialog.Builder(context)
            .setView(view)
            .setOnDismissListener {
                if(findWrongInput()) {
                    return@setOnDismissListener
                }
                val info = Song.SongInfo(et_title.text.toString(), et_artist.text.toString())
                Song.SongInfo.store(song.id, dir, info)
                dialogDismiss.accept(info)
            }
            .create()

        btn_submit.setOnClickListener {
            if(findWrongInput()) {
                Toast.makeText(context, context.getString(R.string.empty_input), Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
            }
        }
    }

    fun show() {
        dialog.show()
    }
}