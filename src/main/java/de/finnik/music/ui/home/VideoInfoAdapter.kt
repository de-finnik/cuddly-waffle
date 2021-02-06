package de.finnik.music.ui.home

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
import de.finnik.music.ThumbnailStore
import java.io.File
import java.io.InputStream
import java.net.URL


class VideoInfoAdapter(context: Context, resource: Int, objects: List<VideoInfo?>): ArrayAdapter<VideoInfo>(context, resource, objects){
    private val mContext: Context?
    private var mResource = 0


    init {
        this.mContext = context
        this.mResource = resource
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val info: VideoInfo = getItem(position)!!
        val holder: InfoHolder
        if (view == null) {
            val inflater = LayoutInflater.from(mContext)
            view = inflater.inflate(mResource, parent, false)
            holder = InfoHolder()
            holder.thumbnail = view.findViewById(R.id.iv_thumbnail)
            holder.title = view.findViewById(R.id.tv_title)
            holder.uploader = view.findViewById(R.id.tv_uploader)
            holder.length = view.findViewById(R.id.tv_length)
            view.tag = holder
        } else {
            holder = view.tag as InfoHolder
        }

        DownloadThumbnailTask(holder.thumbnail, mContext!!).execute(info.thumbnail, info.id)
        holder.title.text = info.fulltitle
        holder.uploader.text = info.uploader
        holder.length.text = mContext?.resources?.getString(R.string.duration, info.duration / 60, info.duration % 60)
        return view!!
    }

    private class DownloadThumbnailTask(imageView: ImageView, context: Context): AsyncTask<String, String, Drawable?>() {

        val imageView: ImageView
        val mContext: Context
        init {
            this.imageView = imageView
            this.mContext = context
        }
        override fun doInBackground(vararg params: String?): Drawable? {
            return mContext.scaledDrawable(params[0]!!)
        }

        override fun onPostExecute(result: Drawable?) {
            super.onPostExecute(result)
            imageView.setImageDrawable(result)
        }

        fun Context.scaledDrawable(url: String): Drawable {
            val inputStream: InputStream = URL(url).content as InputStream
            val bmp = BitmapFactory.decodeStream(inputStream)

            return scaleBitmapToDrawable(bmp)
        }
   }
    companion object {
        fun Context.scaleBitmapToDrawable(bitmap: Bitmap): Drawable {
            val bmpScaled = Bitmap.createScaledBitmap(bitmap, 500, 300, false)
            return BitmapDrawable(resources, bmpScaled)
        }
    }
    private class InfoHolder {
        lateinit var thumbnail: ImageView
        lateinit var title: TextView
        lateinit var uploader: TextView
        lateinit var length: TextView
    }
}
