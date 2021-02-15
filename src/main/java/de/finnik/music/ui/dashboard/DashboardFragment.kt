package de.finnik.music.ui.dashboard

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.finnik.music.MainActivity
import de.finnik.music.R
import de.finnik.music.player.MusicPlayerService
import de.finnik.music.songs.Song
import de.finnik.music.ui.dialogs.PlaylistDialog
import de.finnik.music.ui.dialogs.RenameDialog
import java.util.function.Consumer


class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var spinner_playlists: Spinner

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
            if(musicPlayerService.isInitialized().not()) {
                musicPlayerService.initSongPlayer(
                    mainActivity.songs,
                    mainActivity.getPlaylists()[spinner_playlists.selectedItemPosition]
                )
            }
            mainActivity.musicPlayerView.setService(musicPlayerService)
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
        registerForContextMenu(list_download)

        spinner_playlists = root.findViewById<Spinner>(R.id.spinner_playlists)

        val spinner_adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mainActivity.getPlaylists().map { it.name })
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_playlists.adapter = spinner_adapter

        mainActivity.songs.addListener(Consumer {
            list_download.post {
                reloadSongs()
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
                reloadSongs()
            }
        }

        list_download.setOnItemClickListener { parent, view, position, id ->
            play(position, spinner_playlists.selectedItemPosition)
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
            reloadSongs()
        })

        mainActivity.playlistStore.addChangeListener(Consumer {
            spinner_adapter.clear()
            spinner_adapter.addAll(mainActivity.getPlaylists().map { it.name })
        })

        Intent(requireActivity(), MusicPlayerService::class.java).also { intent ->
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        return root
    }

    fun reloadSongs() {
        val playlist = mainActivity.getPlaylists()[spinner_playlists.selectedItemPosition]
        playlist.ids.forEach { Log.i("TAG", "reloadSongs: $it") }
        displayingSongs.clear()
        displayingSongs.addAll(playlist.ids.map { mainActivity.songs.getId(it) })
        adapter.notifyDataSetChanged()
    }

    fun play(index: Int, playlistIndex: Int) {
        if (connectedToService) {
            val playlist = mainActivity.getPlaylists()[playlistIndex]
            if (this@DashboardFragment.connectedToService) {
                musicPlayerService.setPlaylist(playlist)
            }

            musicPlayerService.play(playlist.createQuery(index))
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v.id == R.id.list_download) {
            menu.add(0, 0, 2, "Change title/artist")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.groupId != 0) {
            return false
        }
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        val song = displayingSongs[info.position]

        val dialog = RenameDialog(requireContext(), song, mainActivity.audio_dir, Consumer {
            mainActivity.loadSongs()
        })
        dialog.show()
        return super.onContextItemSelected(item)
    }
}