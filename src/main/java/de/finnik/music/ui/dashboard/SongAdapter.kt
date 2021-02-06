package de.finnik.music.ui.dashboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bawaviki.youtubedl_android.mapper.VideoInfo
import de.finnik.music.R
import de.finnik.music.Song
import de.finnik.music.ThumbnailStore
import java.io.File
import java.io.InputStream
import java.net.URL

class SongAdapter(context: Context, resource: Int, objects: List<Song>): ArrayAdapter<Song>(context, resource, objects){
    private val mContext: Context?
    private var mResource = 0


    init {
        this.mContext = context
        this.mResource = resource
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
            view.tag = holder
        } else {
            holder = view.tag as SongHolder
        }

        holder.thumbnail.setImageDrawable(BitmapDrawable(context.resources, song.thumbnail))
        val info = song.videoInfo
        holder.title.text = song.title
        holder.uploader.text = song.artist
        holder.length.text = mContext?.resources?.getString(R.string.duration, info.duration / 60, info.duration % 60)
        return view!!
    }

    private class SongHolder {
        lateinit var thumbnail: ImageView
        lateinit var title: TextView
        lateinit var uploader: TextView
        lateinit var length: TextView
    }
}
