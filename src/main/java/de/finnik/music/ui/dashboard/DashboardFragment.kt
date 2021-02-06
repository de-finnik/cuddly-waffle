package de.finnik.music.ui.dashboard

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.finnik.music.R
import de.finnik.music.Song
import de.finnik.music.player.MusicNotification
import de.finnik.music.player.MusicPlayerService
import java.io.File
import java.util.function.Consumer

private val songList: MutableList<Song> = ArrayList()

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var adapter: SongAdapter

    private var connectedToService = false
    private lateinit var musicPlayerService: MusicPlayerService
    private lateinit var musicNotification: MusicNotification

    private val serviceConnection = object:ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            connectedToService = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connectedToService = true
            musicPlayerService = (service as MusicPlayerService.MusicBinder).service
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        adapter = SongAdapter(requireContext(), R.layout.list_adapter, songList)
        val list_download = root.findViewById<ListView>(R.id.list_download)
        list_download.adapter = adapter
        loadSongs()
        list_download.setOnItemClickListener { parent, view, position, id ->
                play(adapter.getItem(position)!!)
        }

        Intent(requireActivity(), MusicPlayerService::class.java).also { intent ->
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        return root
    }

    fun play(song: Song) {
        if (connectedToService) {
            musicPlayerService.play(song)
        }
   }

    fun loadSongs() {
        val dir = File(requireActivity().application.filesDir, "audio")
        dir.mkdirs()
        songList.clear()
        songList.addAll(Song.findSongs(dir))
        adapter.notifyDataSetChanged()
    }
}