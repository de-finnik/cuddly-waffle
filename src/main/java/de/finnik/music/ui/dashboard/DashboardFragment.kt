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
import de.finnik.music.MainActivity
import de.finnik.music.R
import de.finnik.music.songs.Song
import de.finnik.music.player.MusicPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.function.Consumer


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    private var connectedToService = false
    private lateinit var musicPlayerService: MusicPlayerService

    private val serviceConnection = object:ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            connectedToService = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connectedToService = true
            musicPlayerService = (service as MusicPlayerService.MusicBinder).service
            (requireActivity() as MainActivity).musicPlayerView.setService(musicPlayerService)
        }
    }

    companion object {
        private lateinit var adapter: SongAdapter
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val mainActivity = requireActivity() as MainActivity
        adapter = SongAdapter(requireContext(), R.layout.list_adapter, mainActivity.songs)
        val list_download = root.findViewById<ListView>(R.id.list_download)
        list_download.adapter = adapter


        mainActivity.songs.addListener(Consumer {
            requireActivity().runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        })

        list_download.setOnItemClickListener { parent, view, position, id ->
                play(position)
        }

        if(connectedToService) {
            mainActivity.musicPlayerView.setService(musicPlayerService)
        }

        Intent(requireActivity(), MusicPlayerService::class.java).also { intent ->
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        return root
    }

    fun play(index: Int) {
        if (connectedToService) {
            musicPlayerService.play((requireActivity() as MainActivity).songs, index)
        }
    }

}