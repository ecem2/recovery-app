package com.adentech.recovery.ui.result.gallery

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.adentech.recovery.R
import com.adentech.recovery.common.Constants.IMAGE_DELETED
import com.adentech.recovery.common.Constants.IMAGE_PATH
import com.adentech.recovery.core.common.Status
import com.adentech.recovery.core.fragments.BaseFragment
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.databinding.FragmentGalleryImagesBinding
import com.adentech.recovery.ui.result.fullscreen.GalleryActivity
import com.adentech.recovery.ui.scan.ScanViewModel
import com.adentech.recovery.view.SpacesItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton

@AndroidEntryPoint
class GalleryImagesFragment : BaseFragment<ScanViewModel, FragmentGalleryImagesBinding>() {

    private val imageAdapter by lazy { GalleryAdapter(onItemClicked = ::onItemClicked) }

    override fun getResourceLayoutId() = R.layout.fragment_gallery_images

    override fun viewModelClass() = ScanViewModel::class.java

    override fun onInitDataBinding() {
        setupRecyclerView()
        viewModel.getAllGalleryImages()
        getImages()
    }

    private fun setupRecyclerView() {
        viewBinding.rvGalleryImages.apply {
            adapter = imageAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_4)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
            setHasFixedSize(true)
        }
    }

    private fun getImages() {
        viewModel.imageList.observe(viewLifecycleOwner) { images ->
            when (images.status) {
                Status.SUCCESS -> {
                    viewBinding.llEmptyGallery.visibility = View.GONE
                    viewBinding.rvGalleryImages.hideSkeleton()
                    images.data?.let { checkProgressBar(it) }
                    imageAdapter.submitList(images.data)
                }

                Status.ERROR -> {
                    if (images.data == null) {
                        imageAdapter.submitList(arrayListOf())
                        viewBinding.rvGalleryImages.hideSkeleton()
                        viewBinding.llEmptyGallery.visibility = View.VISIBLE
                        viewBinding.progressBar.visibility = View.GONE
                    }
                }

                Status.LOADING -> {
                    viewBinding.llEmptyGallery.visibility = View.GONE
                    viewBinding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkProgressBar(list: ArrayList<FileModel>) {
        if (list.size.compareTo(0) != 0) viewBinding.progressBar.visibility = View.GONE
    }

    private fun onItemClicked(image: FileModel) {
        try {
            val intent = Intent(requireActivity(), GalleryActivity::class.java)
            intent.putExtra(IMAGE_PATH, image)
            deleteImageActivityResultLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private val deleteImageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imageWasDeleted = data?.getBooleanExtra(IMAGE_DELETED, false) ?: false
            if (imageWasDeleted) {
                viewModel.getAllGalleryImages()
                getImages()
            }
        }
    }
}