package de.finnik.music.ui

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import de.finnik.music.R
import de.finnik.music.songs.Playlist
import de.finnik.music.songs.PlaylistStore
import de.finnik.music.songs.Song

class PlaylistDialog(
    context: Context,
    val playlists: List<Playlist>,
    song: Song,
    store: PlaylistStore
) {
    private val dialog: AlertDialog
    private val adapter: PlaylistAdapter

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_playlist, null)
        val lv_playlists = view.findViewById<ListView>(R.id.lv_playlists)
        adapter = PlaylistAdapter(context, R.layout.dialog_add_to_playlist, playlists, song)
        lv_playlists.adapter = adapter
        dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()
        view.findViewById<Button>(R.id.btn_submit).setOnClickListener { dialog.dismiss() }
        dialog.setOnDismissListener {
            adapter.selectedPlaylists.forEach { t, u ->
                if (u) store.add(
                    song,
                    t.name
                ) else store.remove(song, t.name)
            }
        }
    }

    fun show() {
        dialog.show()
    }
}

class PlaylistAdapter(context: Context, resource: Int, objects: List<Playlist>, val song: Song) :
    ArrayAdapter<Playlist>(context, resource, objects) {

    val selectedPlaylists: HashMap<Playlist, Boolean>

    init {
        selectedPlaylists = HashMap()
        objects.forEach { selectedPlaylists[it] = it.contains(song.id) }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return if (convertView == null) {
            val checkBox = CheckBox(context)
            val playlist = getItem(position)!!
            checkBox.setText(playlist.name)
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                selectedPlaylists.put(playlist, isChecked)
            }
            checkBox.isChecked = selectedPlaylists.getOrDefault(playlist, false)
            checkBox
        } else {
            convertView
        }
    }
}