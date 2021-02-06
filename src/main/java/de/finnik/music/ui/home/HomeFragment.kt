package de.finnik.music.ui.home

import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bawaviki.youtubedl_android.YoutubeDL
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import de.finnik.music.DownloadTask
import de.finnik.music.Downloader
import de.finnik.music.R
import de.finnik.music.ThumbnailStore
import de.finnik.music.ui.ProgressDialog
import de.finnik.music.ui.Stream
import java.io.File
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

private val listInfo: MutableList<VideoInfo> = ArrayList()

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: VideoInfoAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        adapter = VideoInfoAdapter(requireContext(),  R.layout.list_adapter, listInfo)
        val et_search = root.findViewById<EditText>(R.id.et_search)
        val btn_search = root.findViewById<Button>(R.id.btn_search)
        val seekBar = root.findViewById<SeekBar>(R.id.sb_amount)
        btn_search.setOnClickListener {
            val text = et_search.text.toString()
            if (text.isNotEmpty()) {
                val progressDialog = ProgressDialog(requireContext(), "Durchsuche YouTube nach $text!")
                LoadTask(this, progressDialog).execute(text, (seekBar.progress+1).toString())
            }
       }
        val list_video_info = root.findViewById<ListView>(R.id.list_result)
        list_video_info.adapter = adapter
        list_video_info.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            DownloadTask().download(adapter.getItem(i)!!.id, File(requireActivity().application.filesDir, "audio"))
            val stream = Stream(requireContext(), adapter.getItem(i)!!)
            stream.show(view1)
        }

        return root
    }

    fun setVideoInfos(list: List<VideoInfo>) {
        listInfo.clear()
        listInfo.addAll(list)
        adapter.notifyDataSetChanged()
    }

    companion object {
        class LoadTask(context: HomeFragment, progressDialog: ProgressDialog):AsyncTask<String, String, List<VideoInfo>>() {
            private val fragmentReference: WeakReference<HomeFragment> = WeakReference(context)
            private val progressReference: WeakReference<ProgressDialog> = WeakReference(progressDialog)

            override fun onPreExecute() {
                val progress = progressReference.get()
                Log.i("TAG", "onPostExecute: ${progress.hashCode()}")
                progress?.show()
            }

            override fun doInBackground(vararg params: String?): List<VideoInfo> {
                return YoutubeDL.getInstance().getYoutubeSearchInfo(params[0], params[1]!!.toInt())
            }

            override fun onPostExecute(result: List<VideoInfo>?) {
                val fragment = fragmentReference.get()
                val progress = progressReference.get()
                Log.i("TAG", "onPostExecute: ${progress.hashCode()}")
                fragment?.setVideoInfos(result!!)
                ProgressDialog.hideAll()
            }
        }
    }
}