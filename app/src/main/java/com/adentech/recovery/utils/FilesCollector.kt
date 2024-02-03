package com.adentech.recovery.utils

import androidx.lifecycle.MutableLiveData
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.data.model.MainFileModel
import kotlin.collections.ArrayList

object FilesCollector {

    val foundFiles: ArrayList<FileModel> = ArrayList()
    val foundImages: ArrayList<FileModel> = ArrayList()
    val foundAudios: ArrayList<FileModel> = ArrayList()
    val foundVideos: ArrayList<FileModel> = ArrayList()

    val allFiles: ArrayList<MainFileModel> = ArrayList()
    val liveDataAllFiles = MutableLiveData<ArrayList<MainFileModel>>()

}