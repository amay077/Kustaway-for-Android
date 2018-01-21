package net.amay077.kustaway

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView

import com.viewpagerindicator.CirclePageIndicator

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.ArrayList
import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

import butterknife.ButterKnife
import butterknife.BindView
import twitter4j.Status

import net.amay077.kustaway.adapter.SimplePagerAdapter
import net.amay077.kustaway.databinding.ActivityScaleImageBinding
import net.amay077.kustaway.event.connection.StreamingConnectionEvent
import net.amay077.kustaway.extensions.getTwitterRepo
import net.amay077.kustaway.fragment.ScaleImageFragment
import net.amay077.kustaway.model.TwitterManager
import net.amay077.kustaway.util.ImageUtil
import net.amay077.kustaway.util.MessageUtil
import net.amay077.kustaway.util.StatusUtil
import net.amay077.kustaway.viewmodel.ProfileActivityViewModel
import net.amay077.kustaway.viewmodel.ScaleImageActivityViewModel
import net.amay077.kustaway.widget.ScaleImageViewPager

/**
 * 画像の拡大表示用のActivity、かぶせて使う
 *
 * @author aska
 */
class ScaleImageActivity : AppCompatActivity() {

    private lateinit var pager: ScaleImageViewPager
    private lateinit var transitionImage: ImageView
    private lateinit var symbol: CirclePageIndicator

    private val imageUrls = ArrayList<String>()
    private lateinit var simplePagerAdapter: SimplePagerAdapter

    private lateinit var viewModel: ScaleImageActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DataBindingUtil.setContentView<ActivityScaleImageBinding>(this, R.layout.activity_scale_image)

        viewModel = ViewModelProviders
                .of(this, ScaleImageActivityViewModel.Factory(
                ))
                .get(ScaleImageActivityViewModel::class.java)

        pager = binding.pager
        transitionImage = binding.transitionImage
        symbol = binding.symbol

        simplePagerAdapter = SimplePagerAdapter(this, pager)
        symbol.setViewPager(pager)
        pager.offscreenPageLimit = 4
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                pager.visibility = View.VISIBLE
                transitionImage.visibility = View.GONE
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })

        // インテント経由での起動をサポート
        val firstUrl = intent.let { i ->
            if (Intent.ACTION_VIEW == i.action) {
                val data = i.data ?: return
                data.toString()
            } else {
                val args = i.extras ?: return

                val status = args.getSerializable("status") as Status
                if (status != null) {
                    val index = args.getInt("index", 0)
                    showStatus(status, index)
                }

                args.getString("url")
            }
        }

        val pattern = Pattern.compile("https?://twitter\\.com/\\w+/status/(\\d+)/photo/(\\d+)/?.*")
        val matcher = pattern.matcher(firstUrl)
        if (matcher.find()) {
            val statusId = java.lang.Long.valueOf(matcher.group(1))
            object : AsyncTask<Void, Void, Status>() {
                override fun doInBackground(vararg params: Void): twitter4j.Status? {
                    try {
                        return TwitterManager.getTwitter().showStatus(statusId!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return null
                    }

                }

                override fun onPostExecute(status: twitter4j.Status?) {
                    if (status != null) {
                        showStatus(status, 0)
                    }
                }
            }.execute()
            return
        }

        symbol.visibility = View.GONE
        imageUrls.add(firstUrl)
        val args = Bundle()
        args.putString("url", firstUrl)
        simplePagerAdapter.addTab(ScaleImageFragment::class.java, args)
        simplePagerAdapter.notifyDataSetChanged()
    }

    fun showStatus(status: twitter4j.Status?, index: Int?) {
        val urls = StatusUtil.getImageUrls(status)
        if (urls.size == 1) {
            symbol.visibility = View.GONE
        }
        for (imageURL in urls) {
            imageUrls.add(imageURL)
            val args = Bundle()
            args.putString("url", imageURL)
            simplePagerAdapter.addTab(ScaleImageFragment::class.java, args)
        }

        // Activity Transition 用の TransitionName を設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageUtil.displayImage(imageUrls[index!!], transitionImage)
            transitionImage.transitionName = getString(R.string.transition_tweet_image)
        }

        simplePagerAdapter.notifyDataSetChanged()
        pager.currentItem = index!!
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scale_image, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.save) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSIONS_STORAGE)
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage()
                } else {
                    MessageUtil.showToast(R.string.toast_save_image_failure)
                }
            }
        }
    }

    fun saveImage() {
        val url: URL
        try {
            url = URL(imageUrls[pager.currentItem])
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            MessageUtil.showToast(R.string.toast_save_image_failure)
            return
        }

        val task = object : AsyncTask<Void, Void, Boolean>() {
            override fun doInBackground(vararg params: Void): Boolean? {
                var count: Int
                var isSuccess: Boolean?
                try {
//                    val connection = url.openConnection()
//                    connection.connect()
//                    val input = BufferedInputStream(url.openStream(), 10 * 1024)
//                    val root = File(Environment.getExternalStorageDirectory(), "/Download/")
//                    val file = File(root, Date().time.toString() + ".jpg")
//                    val output = FileOutputStream(file)
//                    val data = ByteArray(1024)
//                    while ((count = input.read(data)) != -1) {
//                        output.write(data, 0, count)
//                    }
//                    output.flush()
//                    output.close()
//                    input.close()
//                    val paths = arrayOf(file.path)
//                    val types = arrayOf("image/jpeg")
//                    MediaScannerConnection.scanFile(applicationContext, paths, types, null)
                    isSuccess = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    isSuccess = false
                }

                return isSuccess
            }

            override fun onPostExecute(isSuccess: Boolean?) {
                if (isSuccess!!) {
                    MessageUtil.showToast(R.string.toast_save_image_success)
                } else {
                    MessageUtil.showToast(R.string.toast_save_image_failure)
                }
            }
        }
        task.execute()
    }

    companion object {
        internal val REQUEST_PERMISSIONS_STORAGE = 1

        /** 画像表示用の StartActivity  */
        fun startActivityWithImage(
                activity: Activity,
                status: Status,
                openIndex: Int,
                sharedView: View?,
                transitionName: String?) {
            val intent = Intent(activity, ScaleImageActivity::class.java)
            intent.putExtra("status", status)
            intent.putExtra("index", openIndex)

            if (sharedView != null && transitionName != null &&
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val options = ActivityOptions.makeSceneTransitionAnimation(activity,
                        sharedView, transitionName)
                activity.startActivity(intent, options.toBundle())
            } else {
                activity.startActivity(intent)
            }
        }
    }
}
