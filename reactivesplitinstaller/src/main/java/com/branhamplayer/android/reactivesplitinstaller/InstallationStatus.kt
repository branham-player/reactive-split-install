package com.branhamplayer.android.reactivesplitinstaller

sealed class InstallationStatus {
    object Canceled : InstallationStatus()
    object Canceling : InstallationStatus()

    data class Downloaded(
        val totalBytesDownloaded: Long
    ) : InstallationStatus()

    data class Downloading(
        val bytesDownloaded: Long,
        val percentDownloaded: Double,
        val totalBytesToDownload: Long
    ) : InstallationStatus()

    data class Installed(
        val languages: List<String>,
        val moduleNames: List<String>
    ) : InstallationStatus()

    object Installing : InstallationStatus()
    object Pending : InstallationStatus()

    data class RequestAccepted(
        val taskSessionId: Int
    ) : InstallationStatus()

    object RequiresUserConfirmation : InstallationStatus()
    object Unknown : InstallationStatus()
}
