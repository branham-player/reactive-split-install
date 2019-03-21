package com.branamplayer.android.reactivesplitinstaller

import android.app.Activity
import com.branamplayer.android.reactivesplitinstaller.exceptions.InstallationFailedException
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.util.Locale

class ReactiveSplitInstaller(private val activity: Activity) {

    companion object {
        const val REQUEST_CODE = 62352
    }

    private val manager = SplitInstallManagerFactory.create(activity)
    private var request: SplitInstallRequest.Builder? = null
    private var requestSessionId: Int? = null

    fun addModule(moduleName: String): ReactiveSplitInstaller {
        if (request == null) request = SplitInstallRequest.newBuilder()

        request?.addModule(moduleName)
        return this
    }

    fun addLanguage(locale: Locale): ReactiveSplitInstaller {
        if (request == null) request = SplitInstallRequest.newBuilder()

        request?.addLanguage(locale)
        return this
    }

    fun install() = Flowable.create<InstallationStatus>({ emitter ->
        val listener = SplitInstallStateUpdatedListener { state ->
            when (state.status()) {
                SplitInstallSessionStatus.CANCELED -> {
                    emitter.onNext(InstallationStatus.Canceled)
                    emitter.onComplete()
                }

                SplitInstallSessionStatus.CANCELING ->
                    emitter.onNext(InstallationStatus.Canceling)

                SplitInstallSessionStatus.DOWNLOADED ->
                    emitter.onNext(InstallationStatus.Downloaded(state.totalBytesToDownload()))

                SplitInstallSessionStatus.DOWNLOADING -> {
                    val percentDownloaded = state.bytesDownloaded().toDouble() / state.totalBytesToDownload().toDouble()

                    emitter.onNext(
                        InstallationStatus.Downloading(
                            bytesDownloaded = state.bytesDownloaded(),
                            percentDownloaded = percentDownloaded,
                            totalBytesToDownload = state.totalBytesToDownload()
                        )
                    )
                }

                SplitInstallSessionStatus.FAILED ->
                    emitter.onError(InstallationFailedException(state.errorCode()))

                SplitInstallSessionStatus.INSTALLED -> {
                    emitter.onNext(
                        InstallationStatus.Installed(
                            languages = state.languages().toList(),
                            moduleNames = state.moduleNames().toList()
                        )
                    )

                    emitter.onComplete()
                }

                SplitInstallSessionStatus.INSTALLING ->
                    emitter.onNext(InstallationStatus.Installing)

                SplitInstallSessionStatus.PENDING ->
                    emitter.onNext(InstallationStatus.Pending)

                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                    manager.startConfirmationDialogForResult(state, activity, ReactiveSplitInstaller.REQUEST_CODE)
                    emitter.onNext(InstallationStatus.RequiresUserConfirmation)
                }

                SplitInstallSessionStatus.UNKNOWN ->
                    emitter.onNext(InstallationStatus.Unknown)
            }
        }

        manager.registerListener(listener)

        manager.startInstall(request?.build())
            .addOnSuccessListener { sessionId ->
                emitter.onNext(InstallationStatus.RequestAccepted(sessionId))
                requestSessionId = sessionId
            }
            .addOnCompleteListener {
                emitter.onNext(InstallationStatus.RequestCompleted)
                manager.unregisterListener(listener)

                requestSessionId = null
            }
            .addOnFailureListener { exception ->
                val installationException = exception as SplitInstallException
                emitter.onError(InstallationFailedException(installationException.errorCode))
                manager.unregisterListener(listener)

                requestSessionId = null
            }

        request = null
    }, BackpressureStrategy.LATEST)
}
