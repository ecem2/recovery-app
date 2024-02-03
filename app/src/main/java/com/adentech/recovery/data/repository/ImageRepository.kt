package com.adentech.recovery.data.repository

import com.adentech.recovery.core.common.Resource
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.data.model.MainFileModel

interface ImageRepository {

    suspend fun getGalleryImages(): Resource<ArrayList<FileModel>>

    suspend fun getGalleryVideos(): Resource<ArrayList<FileModel>>

    suspend fun getAllTrashFiles(): Resource<MainFileModel>

}