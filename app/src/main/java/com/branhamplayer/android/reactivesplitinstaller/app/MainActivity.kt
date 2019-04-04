package com.branhamplayer.android.reactivesplitinstaller.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.branhamplayer.android.reactivesplitinstaller.InstallationStatus
import com.branhamplayer.android.reactivesplitinstaller.ReactiveSplitInstaller
import com.google.android.play.core.splitcompat.SplitCompat
import io.reactivex.disposables.CompositeDisposable
import android.text.method.ScrollingMovementMethod
import com.google.android.play.core.splitinstall.SplitInstallHelper


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var button1: AppCompatButton? = null
    private var button2: AppCompatButton? = null
    private var button3: AppCompatButton? = null
    private var logView: AppCompatTextView? = null

    private val compositeDisposable = CompositeDisposable()
    private lateinit var installer: ReactiveSplitInstaller

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        installer = ReactiveSplitInstaller(this)

        button1 = findViewById(R.id.feature_one)
        button1?.setOnClickListener(this)

        button2 = findViewById(R.id.feature_two)
        button2?.setOnClickListener(this)

        button3 = findViewById(R.id.unavailable_feature)
        button3?.setOnClickListener(this)

        logView = findViewById(R.id.log)
        logView?.movementMethod = ScrollingMovementMethod()

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onClick(view: View?) {

        val moduleName = when (view) {
            button1 -> getString(R.string.module_feature1)
            button2 -> getString(R.string.module_feature2)
            else -> "Does not exist"
        }

        if (view == button1) {
            installer.addModule(moduleName)
                .install()
                .subscribe({ status ->
                    Log.d("INSTALLER_STATUS", status::class.java.simpleName)
                    logView?.appendLine(status::class.java.simpleName)

                    when (status) {
                        is InstallationStatus.Installed -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                SplitInstallHelper.updateAppInfo(this@MainActivity)
                            }

                            launchActivity("com.branhamplayer.android.reactivesplitinstaller.feature1.FeatureOneActivity")
                        }
                    }
                }, { exception ->
                    Log.e("INSTALLER_STATUS", exception.message)
                    logView?.appendLine("Error code: ${exception.message}")
                }, {
                    Log.d("INSTALLER_STATUS", "Done")
                    logView?.appendLine("Done")
                }).also {
                    compositeDisposable.add(it)
                }
        }

        if (view == button2) {
            installer.addModule(moduleName)
                .install()
                .subscribe({ status ->
                    Log.d("INSTALLER_STATUS", status::class.java.simpleName)
                    logView?.appendLine(status::class.java.simpleName)

                    when (status) {
                        is InstallationStatus.Installed -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                SplitInstallHelper.updateAppInfo(this@MainActivity)
                            }

                            launchActivity("com.branhamplayer.android.reactivesplitinstaller.feature2.FeatureTwoActivity")
                        }
                    }
                }, { exception ->
                    Log.e("INSTALLER_STATUS", "Error code: ${exception.message}")
                    logView?.appendLine("Error code: ${exception.message}")
                }, {
                    Log.d("INSTALLER_STATUS", "Done")
                    logView?.appendLine("Done")
                }).also {
                    compositeDisposable.add(it)
                }
        }

        if (view == button3) {
            installer.addModule(moduleName)
                .install()
                .subscribe({ status ->
                    Log.d("INSTALLER_STATUS", status::class.java.simpleName)
                    logView?.appendLine(status::class.java.simpleName)
                }, { exception ->
                    Log.e("INSTALLER_STATUS", "Error code: ${exception.message}")
                    logView?.appendLine("Error code: ${exception.message}")
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

    private fun AppCompatTextView.appendLine(text: String) {
        this.append("$text\n")
    }
}
