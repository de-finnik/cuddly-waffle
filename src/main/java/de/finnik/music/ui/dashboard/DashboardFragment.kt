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
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.finnik.music.MainActivity
import de.finnik.music.R
import de.finnik.music.player.MusicPlayerService
import de.finnik.music.songs.Playlist
import de.finnik.music.songs.Song
import java.util.function.Consumer


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    private var connectedToService = false
    private lateinit var musicPlayerService: MusicPlayerService

    lateinit var mainActivity: MainActivity
    val displayingSongs: ArrayList<Song> = ArrayList()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            connectedToService = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connectedToService = true
            musicPlayerService = (service as MusicPlayerService.MusicBinder).service
            musicPlayerService.initSongPlayer(mainActivity.songs, mainActivity.playlists[0])
            (requireActivity() as MainActivity).musicPlayerView.setService(musicPlayerService)
        }
    }

    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        mainActivity = requireActivity() as MainActivity
        adapter = SongAdapter(requireContext(), R.layout.list_adapter, displayingSongs)
        val list_download = root.findViewById<ListView>(R.id.list_download)
        list_download.adapter = adapter

        val spinner_playlists = root.findViewById<Spinner>(R.id.spinner_playlists)

        val spinner_adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mainActivity.playlists.map { it.name })
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_playlists.adapter = spinner_adapter

        mainActivity.songs.addListener(Consumer {
            requireActivity().runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        })

        spinner_playlists.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                reloadSongs(mainActivity.playlists[position])
                if (this@DashboardFragment.connectedToService)
                    musicPlayerService.setPlaylist(mainActivity.playlists[position])
            }

        }

        list_download.setOnItemClickListener { parent, view, position, id ->
            play(position)
        }

        if (connectedToService) {
            mainActivity.musicPlayerView.setService(musicPlayerService)
        }

        reloadSongs(mainActivity.playlists[0])

        Intent(requireActivity(), MusicPlayerService::class.java).also { intent ->
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        return root
    }

    fun reloadSongs(playlist: Playlist) {
        displayingSongs.clear()
        displayingSongs.addAll(playlist.ids.map { mainActivity.songs.getId(it) })
        adapter.notifyDataSetChanged()
    }

    fun play(index: Int) {
        if (connectedToService) {
            musicPlayerService.play(index)
        }
    }
}