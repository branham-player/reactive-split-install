package com.branamplayer.android.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.branamplayer.android.reactivesplitinstaller.InstallationStatus
import com.branamplayer.android.reactivesplitinstaller.ReactiveSplitInstaller
import com.google.android.gms.common.wrappers.InstantApps
import com.google.android.play.core.splitcompat.SplitCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var button1: AppCompatButton? = null
    private var button2: AppCompatButton? = null
    private var button3: AppCompatButton? = null

    private val compositeDisposable = CompositeDisposable()
    private lateinit var installer: ReactiveSplitInstaller

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)

        if (!InstantApps.isInstantApp(this)) {
            SplitCompat.install(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        installer = ReactiveSplitInstaller(this)

        button1 = findViewById(R.id.feature_one)
        button1?.setOnClickListener(this)

        button3 = findViewById(R.id.unavailable_feature)
        button3?.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onClick(view: View?) {
        if (view == button1) {
            val moduleName = getString(R.string.module_feature1)

            installer.addModule(moduleName)
                .install()
                .subscribe({ status ->
                    Log.d("INSTALLER_STATUS", status::class.java.simpleName)

                    when (status) {
                        is InstallationStatus.RequestCompleted ->
                            launchActivity("com.branamplayer.android.reactivesplitinstaller.feature1.FeatureOneActivity")
                    }
                }, { exception ->
                    Log.e("INSTALLER_STATUS", exception.message)
                    Toast.makeText(this, "Could not load this module", Toast.LENGTH_LONG).show()
                }).also {
                    compositeDisposable.add(it)
                }
        }

        if (view == button3) {
            installer.addModule("Does not exist")
                .install()
                .subscribe({ status ->
                    Log.d("INSTALLER_STATUS", status::class.java.simpleName)
                }, { exception ->
                    Log.e("INSTALLER_STATUS", exception.message)
                    Toast.makeText(this, "Could not load this module", Toast.LENGTH_LONG).show()
                }).also {
                    compositeDisposable.add(it)
                }
        }
    }

    private fun launchActivity(className: String) {
        val intent = Intent()
        intent.setClassName(this, className)

        startActivity(intent)
    }
}
