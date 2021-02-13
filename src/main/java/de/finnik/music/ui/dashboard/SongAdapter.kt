package de.finnik.music.ui.dashboard

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import de.finnik.music.R
import de.finnik.music.songs.Song
import java.util.function.Consumer

class SongAdapter(context: Context, resource: Int, objects: List<Song>, add_to_playlist: Consumer<Song>): ArrayAdapter<Song>(context, resource, objects){
    private val mContext: Context?
    private var mResource = 0
    private val add_to_playlist: Consumer<Song>


    init {
        this.mContext = context
        this.mResource = resource
        this.add_to_playlist = add_to_playlist
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val song: Song = getItem(position)!!
        val holder: SongHolder
        if (view == null) {
            val inflater = LayoutInflater.from(mContext)
            view = inflater.inflate(mResource, parent, false)
            holder = SongHolder()
            holder.thumbnail = view.findViewById(R.id.iv_thumbnail)
            holder.title = view.findViewById(R.id.tv_title)
            holder.uploader = view.findViewById(R.id.tv_uploader)
            holder.length = view.findViewById(R.id.tv_length)
            holder.add_playlist = view.findViewById(R.id.iv_add_playlist)
            view.tag = holder
        } else {
            holder = view.tag as SongHolder
        }

        holder.thumbnail.setImageDrawable(BitmapDrawable(context.resources, song.thumbnail))
        val info = song.videoInfo
        holder.title.text = song.title
        holder.uploader.text = song.artist
        holder.length.text = mContext?.resources?.getString(R.string.duration, info.duration / 60, info.duration % 60)
        holder.add_playlist.setOnClickListener {
            add_to_playlist.accept(song)
        }
        return view!!
    }

    private class SongHolder {
        lateinit var thumbnail: ImageView
        lateinit var title: TextView
        lateinit var uploader: TextView
        lateinit var length: TextView
        lateinit var add_playlist: ImageView
    }
}
