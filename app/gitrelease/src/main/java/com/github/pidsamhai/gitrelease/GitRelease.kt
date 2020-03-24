package com.github.pidsamhai.gitrelease

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ProgressCallback
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.pidsamhai.gitrelease.response.GitReleaseResponse
import com.github.pidsamhai.gitrelease.response.ReleaseResponse
import com.github.pidsamhai.gitrelease.response.ResponseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class GitRelease(private val activity: AppCompatActivity) {
    private val baseUrl: String = "https://api.github.com/repos"
    var owner: String? = null
    var repo: String? = null
    var loading: Boolean = true
    var currentVersion: String? = null
    private var isNew: Boolean = false
    private var releaseDialog: AlertDialog
    private var loadingDialog: AlertDialog
    private var updateDialog: AlertDialog
    var title: String = "Checking new version..."
    var massage: String = "Loading..."
    var releaseTitle = "New version !!"
    var releaseMassage: String? = ""
    private var body: String? = ""
    private var url: String? = null
    private val handler: Handler = Handler()

    init {
        releaseDialog = AlertDialog.Builder(activity)
            .setView(activity.layoutInflater.inflate(R.layout.dialog_release_new_version, null))
            .setPositiveButton("UPDATE") { _, _ ->
                dlFIle()
            }
            .setNegativeButton("Cancel", null)
            .create()
        releaseDialog.setOnShowListener {
            releaseDialog.findViewById<TextView>(R.id.title)!!.text = releaseTitle
            releaseDialog.findViewById<TextView>(R.id.massage)!!.text = releaseMassage
            releaseDialog.findViewById<TextView>(R.id.body)!!.text = body
        }
        loadingDialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(massage)
            .setCancelable(false)
            .setNegativeButton("Cancel") { _, _ ->
                cancelCheck()
            }
            .create()
        updateDialog = AlertDialog.Builder(activity)
            .setView(activity.layoutInflater.inflate(R.layout.dialog_progress, null))
            .setCancelable(false)
            .create()
    }

    private fun hideUpdate() {
        updateDialog.dismiss()
    }

    private fun showUpdate() {
        updateDialog.show()
    }

    private fun cancelCheck() = getRelease().cancel()

    private fun hideLoading() {
        if (loading)
            loadingDialog.dismiss()
    }

    private fun showLoading() {
        if (loading)
            loadingDialog.show()
    }

    private fun showRelease() {
        releaseDialog.show()
    }

    private fun hideRelease() {
        releaseDialog.dismiss()
    }

    private fun fullUrl(): String {
        val fullUrl = "$baseUrl/$owner/$repo/releases"
        Log.i(TAG, "fullUrl: $fullUrl")
        return fullUrl
    }

    fun getRelease() = GlobalScope.launch(Dispatchers.Main) {
        val data = getData()
        if (!data.isError) {
            data.data.response?.let {
                url = it[0].assets!![0]!!.browserDownloadUrl
                releaseMassage = it[0].tagName
                body = it[0].body
            }
            showRelease()
        }
    }

    private suspend fun getData(): ReleaseResponse {
        showLoading()
        return try {
            val (_, _, resObj) = fullUrl()
                .httpGet()
                .awaitObjectResponse(gsonDeserializerOf(Array<GitReleaseResponse>::class.java))
            Log.i(TAG, "getRelease: $resObj")
            if (currentVersion != resObj[0].tagName) {
                isNew = true
            }
            val data = ResponseData(resObj, null)
            hideLoading()
            ReleaseResponse(data, false, isNew)
        } catch (e: Exception) {
            Log.e(TAG, "getRelease: ${e.message}", e)
            val data = ResponseData(null, e)
            hideLoading()
            ReleaseResponse(data, true, isNew)
        }
    }

    private fun dlFIle() {
        showUpdate()
        Log.e(TAG, "dlFIle: $url")
        val path = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val updateFile = File(path, "abc.apk")
        Log.e(TAG, "dlFIle: $path")
        url?.let {
            Fuel.download(it)
                .fileDestination { _, _ ->
                    updateFile
                }
                .progress(object : ProgressCallback {
                    @SuppressLint("SetTextI18n")
                    override fun invoke(readBytes: Long, totalBytes: Long) {
                        val abc = (readBytes.toFloat() / totalBytes.toFloat() * 100).toInt()
                        GlobalScope.launch(Dispatchers.Main) {
                            updateDialog.findViewById<ProgressBar>(R.id.progress)!!.progress = abc
                            updateDialog.findViewById<TextView>(R.id.minMax)!!.text =
                                "$readBytes / $totalBytes"
                            updateDialog.findViewById<TextView>(R.id.percent)!!.text = "$abc% "
                        }
                        Log.e(TAG, "Download: $abc")
                    }
                })
                .response { _, _, result ->
                    when (result) {
                        is Result.Success -> {
                            hideUpdate()
                            installApk()
                        }
                        else -> {
                            Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                            hideUpdate()
                        }
                    }

                }
        }
    }

    private fun installApk() {
        try {
            val path = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val updateFile = File(path, "abc.apk")
            val auth = "com.github.pidsamhai.sample"
            val uri = FileProvider.getUriForFile(activity, auth, updateFile)
            Log.e(TAG, "Path: $path")
            Log.e(TAG, "File Path: $updateFile")
            Log.e(TAG, "Provider Apk Path: $uri")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}