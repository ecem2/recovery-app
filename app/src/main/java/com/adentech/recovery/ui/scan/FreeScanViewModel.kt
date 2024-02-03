package com.adentech.recovery.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adentech.recovery.core.common.Resource
import com.adentech.recovery.core.viewmodel.BaseViewModel
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.data.preferences.Preferences
import com.adentech.recovery.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FreeScanViewModel @Inject constructor(
    val preferences: Preferences,
    private val repository: ImageRepository
): BaseViewModel() {

    var isProgressDone: Boolean = false

    private val _imageList = MutableLiveData<Resource<ArrayList<FileModel>>>()
    val imageList: LiveData<Resource<ArrayList<FileModel>>> = _imageList

    init {
        _imageList.postValue(Resource.loading(null))
    }

    fun getAllGalleryImages() = viewModelScope.launch {
        val images = repository.getGalleryImages()
        if (images.data.isNullOrEmpty() || images.data.size == 0) {
            _imageList.postValue(Resource.error(images.message.toString(), null))
        } else {
            _imageList.postValue(images)
        }
    }
}