package com.carlinkit.musicstarter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    lateinit var pref: SharedPreferences
    lateinit var appNameView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appNameView = findViewById(R.id.selected_app)
        pref = getSharedPreferences("play_music", MODE_PRIVATE)

        val recyclerView: RecyclerView = findViewById(R.id.package_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        pref.getString("app", null)?.let {
            appNameView.text = it
        }

        val adapter = PackageItemAdapter(
            onItemClick = { appInfo ->
                appInfo.resolveInfo.serviceInfo.run {
                    pref.edit().putString("package", packageName).apply()
                    pref.edit().putString("name", name).apply()
                    pref.edit().putString("app", appInfo.appName).apply()
                }

                appNameView.text = appInfo.appName
            },
            onPlayClick = { appInfo ->
                val componentName = appInfo.resolveInfo.serviceInfo.run {
                    ComponentName(packageName, name)
                }
                MusicLauncher(baseContext).play(componentName)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                if (parent.getChildAdapterPosition(view) != parent.adapter!!.itemCount - 1){
                    outRect.bottom = 5;
                }
            }
        })

        lifecycleScope.launch {
            val appInfos: List<AppInfo> = withContext(Dispatchers.IO) {
                fun Context.getPackageInfo(): List<ResolveInfo> {
                    //val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
                    val intent = Intent(MediaBrowserService.SERVICE_INTERFACE)
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
//                        packageManager.queryBroadcastReceivers(
//                            intent, PackageManager.ResolveInfoFlags.of(0))
                        packageManager.queryIntentServices(
                            intent, PackageManager.ResolveInfoFlags.of(0))
                    } else {
                        //packageManager.getPackageInfo(packageName, 0)
                        packageManager.queryIntentServices(intent, 0)
                    }
                }

                getPackageInfo()
                    .map {
                        AppInfo(
                            appName = it.loadLabel(packageManager).toString(),
                            packageName = it.serviceInfo.packageName,
                            icon = it.loadIcon(packageManager),
                            resolveInfo = it
                        )
                    }
            }

            adapter.dataList = appInfos
        }
    }
}

data class AppInfo (
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val resolveInfo: ResolveInfo
)

class PackageItemAdapter(
    private val onItemClick: (AppInfo) -> Unit,
    private val onPlayClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<PackageItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
        , View.OnClickListener {
        var titleView: TextView = itemView.findViewById(R.id.package_name)
        var iconView: ImageView = itemView.findViewById(R.id.iv_icon)
        var button: ImageButton = itemView.findViewById(R.id.play_button)

        var appInfo: AppInfo? = null
            set(value) {
                field = value
                value ?: return
                titleView.text = value.appName
                iconView.setImageDrawable(value.icon)
            }

        init {
            itemView.setOnClickListener(this)
            button.setOnClickListener {
                Log.e("hmhm", "hmhm button $appInfo")
                appInfo?.let { onPlayClick(it) }
            }
        }

        override fun onClick(v: View?) {
            appInfo?.let { onItemClick(it) }
        }
    }

    var dataList: List<AppInfo> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.package_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.appInfo = dataList[position]
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}