package de.finnik.music.ui.dashboard

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
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
import de.finnik.music.ui.PlaylistDialog
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
            musicPlayerService.initSongPlayer(
                mainActivity.songs,
                mainActivity.playlistStore.playlists[0]
            )
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
        adapter = SongAdapter(requireContext(), R.layout.list_adapter, displayingSongs,
            Consumer {
                val dialog = PlaylistDialog(
                    requireContext(),
                    mainActivity.playlistStore.playlists,
                    it,
                    mainActivity.playlistStore
                )
                dialog.show()
            })
        adapter
        val list_download = root.findViewById<ListView>(R.id.list_download)
        list_download.adapter = adapter

        val spinner_playlists = root.findViewById<Spinner>(R.id.spinner_playlists)

        val spinner_adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mainActivity.playlistStore.playlists.map { it.name })
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
                reloadSongs(mainActivity.playlistStore.playlists[position])
                if (this@DashboardFragment.connectedToService)
                    musicPlayerService.setPlaylist(mainActivity.playlistStore.playlists[position])
            }
        }

        list_download.setOnItemClickListener { parent, view, position, id ->
            play(position)
        }

        val iv_add_playlist = root.findViewById<ImageView>(R.id.iv_add_playlist)
        iv_add_playlist.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_playlist)
            val editText = EditText(requireContext())
            builder.setView(editText)
            builder.setPositiveButton(R.string.ok) { _, _ ->
                val toString = editText.text.toString()
                if (mainActivity.playlistStore.newPlaylist(toString)) {
                    Toast.makeText(
                        requireContext(),
                        "Successfully created playlist $toString",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Playlist $toString already exists!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            builder.setNegativeButton(R.string.cancel, { dialog, which -> dialog.dismiss() })
            builder.show()
        }

        if (connectedToService) {
            mainActivity.musicPlayerView.setService(musicPlayerService)
        }

        mainActivity.playlistStore.addEditListener(Consumer {
                reloadSongs(mainActivity.playlistStore.playlists[spinner_playlists.selectedItemPosition])
        })

        mainActivity.playlistStore.addChangeListener(Consumer {
            spinner_adapter.clear()
            spinner_adapter.addAll(mainActivity.playlistStore.playlists.map { it.name })
        })

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