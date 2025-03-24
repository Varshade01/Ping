package com.khrd.pingapp.repository.groups

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.ConditionVariable
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.khrd.pingapp.data.groups.GroupFailure
import com.khrd.pingapp.data.groups.GroupImageUpdateOfflineURI
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.listeners.GroupImageOfflineUpdateHelper
import com.khrd.pingapp.homescreen.usecases.updategroupimage.UpdateGroupImageUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltWorker
class UpdateGroupImageOfflineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val updateGroupImageUseCase: UpdateGroupImageUseCase,
    private val connectionStatus: ConnectionStatus,
) : Worker(context, workerParameters) {

    @Inject
    lateinit var groupImageOfflineUpdateHelper: GroupImageOfflineUpdateHelper

    override fun doWork(): Result {
        return if (connectionStatus.retrieveConnectionStatus()) {
            processWork()
        } else {
            Result.retry()
        }
    }

    private fun processWork(): Result {
        val offlineGroupId = workerParameters.inputData.getString(KEY_OFFLINE_GROUP_ID)
        val offlineUri = Uri.parse(workerParameters.inputData.getString(KEY_OFFLINE_IMAGE_URI))
        return if (offlineGroupId != null) {
            val offlineByteArray = provideImageByteArray(offlineUri)
            updateGroupImage(offlineGroupId, offlineByteArray, offlineUri)
        } else Result.failure()
    }


    private fun updateGroupImage(offlineGroupId: String, offlineByteArray: ByteArray, offlineUri: Uri): Result {
        val workerLock = ConditionVariable()
        var result = Result.failure()
        updateGroupImageUseCase.updateGroupImage(offlineGroupId, offlineByteArray, offlineUri) {
            result = when (it) {
                is GroupSuccess -> {
                    groupImageOfflineUpdateHelper.removeTempImageFile(offlineUri)
                    Result.success()
                }
                is GroupImageUpdateOfflineURI -> Result.retry()
                is GroupFailure -> Result.failure()
                else -> Result.failure()
            }
            workerLock.open()
        }
        //reset previous lock state
        workerLock.close()
        //block worker
        workerLock.block()
        return result
    }

    private fun provideImageByteArray(imageFromGallery: Uri): ByteArray {
        val conditionVariable = ConditionVariable()
        var imageBitmap: Bitmap? = null

        Glide.with(this.applicationContext)
            .asBitmap()
            .load(imageFromGallery)
            .override(300, 300)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageBitmap = resource
                    conditionVariable.open()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    conditionVariable.open()
                }
            })
        conditionVariable.block()

        return convertToByteArray(imageBitmap)
    }

    private fun convertToByteArray(resource: Bitmap?): ByteArray {
        val bytes = ByteArrayOutputStream()
        resource?.compress(Bitmap.CompressFormat.JPEG, IMAGE_COMPRESS_QUALITY, bytes)
        return bytes.toByteArray()
    }


    companion object {
        const val KEY_OFFLINE_GROUP_ID = "offline_group_id"
        const val KEY_OFFLINE_IMAGE_URI = "offline_uri"
        const val IMAGE_COMPRESS_QUALITY = 70
    }
}