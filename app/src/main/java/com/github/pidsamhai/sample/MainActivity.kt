package com.github.pidsamhai.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.pidsamhai.gitrelease.response.ReleaseResponse
import com.mukesh.MarkdownView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: VModel
    private lateinit var adg: AlertDialog
    private lateinit var adg2: AlertDialog
    private lateinit var customView: View

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(VModel::class.java)

        viewModel.release.observe(this, Observer { (data, err, nV): ReleaseResponse ->
            if (!err) {
                hideDialog()
                if (nV) {
                    adg2.show()
                    adg2.findViewById<TextView>(R.id.title)!!.text = "New Version !!"
                    adg2.findViewById<TextView>(R.id.massage)!!.text =
                        "${BuildConfig.VERSION_NAME} -> ${data.data!![0].tagName}"
                    adg2.findViewById<MarkdownView>(R.id.mdkView)!!.setMarkDownText(data.data!![0].body)
                }
            } else {
                hideDialog()
                Toast.makeText(this, data.err!!.message, Toast.LENGTH_SHORT).show()
            }
        })

        checkVersion.setOnClickListener {
            checking()
        }

        initDialog()

        checking()

    }

    private fun checking() {
        showDialog()
        viewModel.getRelease()
    }

    private fun initDialog() {
        customView = layoutInflater.inflate(R.layout.dialog_new_version, null)
        adg = AlertDialog.Builder(this)
            .setNegativeButton("Cancel") { _, _ ->
                adg.dismiss()
            }
            .setTitle("Checking new version...")
            .setMessage("Loading...")
            .setCancelable(false)
            .create()
        adg2 = AlertDialog.Builder(this)
            .setNegativeButton("Update") { _, _ ->
                adg2.dismiss()
            }
            .setView(customView)
            .setCancelable(false)
            .create()
    }

    private fun showDialog() = adg.show()
    private fun hideDialog() = adg.dismiss()

}