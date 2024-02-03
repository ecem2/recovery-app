package com.adentech.recovery.ui.result.videos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.adentech.recovery.BuildConfig
import com.adentech.recovery.R
import com.adentech.recovery.RecoveryApplication
import com.adentech.recovery.common.Constants
import com.adentech.recovery.core.common.Resource
import com.adentech.recovery.core.common.Status
import com.adentech.recovery.core.fragments.BaseFragment
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.data.model.MainFileModel
import com.adentech.recovery.databinding.FragmentDeletedVideosBinding
import com.adentech.recovery.extensions.observe
import com.adentech.recovery.extensions.withDelay
import com.adentech.recovery.fbevent.FBEventManager
import com.adentech.recovery.ui.result.ImagesActivity
import com.adentech.recovery.ui.result.fullscreen.DeletedVideoActivity
import com.adentech.recovery.ui.scan.ScanViewModel
import com.adentech.recovery.ui.subscription.SubscriptionActivity
import com.adentech.recovery.utils.FilesFetcher
import com.adentech.recovery.utils.ImagesFilesCollector
import com.adentech.recovery.utils.SearchAllFile
import com.adentech.recovery.view.SpacesItemDecoration
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


@AndroidEntryPoint
class DeletedVideosFragment : BaseFragment<ScanViewModel, FragmentDeletedVideosBinding>() {

    private val deletedVideosAdapter by lazy {
        DeletedVideosAdapter(
            hasReward = false,
            context = requireContext(),
            onItemClicked = { fileModel, position ->
                onItemClicked(fileModel, position)
            }
        )
    }
    private var myMotor: SearchAllFile? = null
    private var volums: ArrayList<String> = ArrayList()
    private var rewardedList: List<Int>? = null
    private val _isStoragePermissionGranted: MutableLiveData<Boolean> = MutableLiveData()
    val isStoragePermissionGranted: LiveData<Boolean> = _isStoragePermissionGranted
    var isProgressDone: Boolean = false
    private var rewardedCount = 0
    private val video: ArrayList<FileModel> = ArrayList()

    override fun getResourceLayoutId() = R.layout.fragment_deleted_videos

    override fun viewModelClass() = ScanViewModel::class.java

    override fun onInitDataBinding() {
        if (RecoveryApplication.isPremium){
            viewBinding.cardScanProgress.visibility = View.GONE
            viewBinding.progressBarLL.visibility = View.GONE
            viewBinding.clResultDialog.visibility = View.GONE
            viewBinding.deletedImagesCl.visibility = View.GONE



        }else{
            viewBinding.cardScanProgress.visibility = View.VISIBLE
            viewBinding.llEmptyFolder.visibility = View.VISIBLE


        }

        setupVideos()
        initRecyclerView()
        //handleBackPressed()
        setupBackButton()
//        if (checkStoragePermissions()) {
//            _isStoragePermissionGranted.postValue(true)
//
//        } else {
//            _isStoragePermissionGranted.postValue(false)
//        }




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewModel.getAllTrashedFiles()
            observe(viewModel.allTrashedFilesList, ::getTrashedList)
        } else {
            getFileList()
        }
    }
    private fun initRecyclerView() {
        viewBinding.rvDeletedVideos.apply {
            adapter = deletedVideosAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_6)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
            setHasFixedSize(true)
        }
    }
    private fun setupVideos() {
        viewBinding.llPermissionError.visibility = View.GONE
        viewModel.getAllGalleryVideos()
        observe(viewModel.videoList, ::getVideoList)
    }


    private fun getFileList() {
        volums = FilesFetcher(requireContext()).getStorageVolumes()
        if (volums.isNotEmpty()) {
            myMotor = SearchAllFile(volums, requireActivity(), requireContext())
            myMotor?.execute(*arrayOfNulls(0))
            deletedVideosAdapter.submitList(ImagesFilesCollector.foundVideoList)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun showDialog() {
        if (RecoveryApplication.isPremium) {
            return
        }
        viewBinding.cardScanProgress.visibility = View.GONE
        viewBinding.clResultDialog.visibility = View.VISIBLE
        deletedVideosAdapter.rewardedList = rewardedList
        //deletedImagesAdapter.notifyDataSetChanged()
        val dialogText = getString(R.string.deleted_photos_were_found)
        val color = ContextCompat.getColor(requireContext(), R.color.dialog_red_text)
        val coloredText = changeColor(dialogText, color, color)
        viewBinding.dialogTitle.text = coloredText
        viewBinding.buttonRestoreNow.setOnClickListener {
            // viewModel.isProgressDone = true
            navigateToSubscription()
        }
    }
    private fun onItemClicked(fileModel: FileModel, position: Int) {
        navigateToDeletedVideoActivity(fileModel)
        if (isProgressDone) {
            if (fileModel.isSelected == true) {
            } else {
                if (rewardedCount > 0) {
                    if (fileModel.isRewarded == true) {
                        fileModel.isSelected = true
                    } else {
                        fileModel.isSelected = false
                        navigateToSubscription()
                    }
                } else {
                    navigateToSubscription()
                }
            }
        }
    }
    private fun getVideoList(resource: Resource<ArrayList<FileModel>>) {
        when (resource.status) {
            Status.SUCCESS -> {
                resource.data?.let { if (!RecoveryApplication.isPremium){adjustVideos(it) }}
            }

            Status.ERROR -> {
                viewBinding.cardScanProgress.visibility = View.GONE
                if (resource.data.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                }
            }

            Status.LOADING -> {
                if(RecoveryApplication.isPremium){
                    viewBinding.progressBarLL.visibility = View.GONE

                }else{
                    viewBinding.progressBarLL.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun adjustVideos(videoList: ArrayList<FileModel>) {
        FBEventManager().logEvent("video_list_size", "video_list_size", videoList.size.toString())
        val min = videoList.size / 5
        val max = videoList.size / 2
        val count = if (videoList.size / 50 > 3) {
            if (BuildConfig.DEBUG) 10 else 160
        } else {
            videoList.size / 10 + 1
        }

        val indexesArray: ArrayList<Int> = ArrayList()
        val currentList: ArrayList<FileModel> = ArrayList()

        lifecycleScope.launch {
            if (videoList.isNotEmpty()) {
                viewBinding.llEmptyFolder.visibility = View.GONE
                viewBinding.tvProgressBar.text = getString(R.string.videos_recovered)

                while (indexesArray.size < count) {
                    val randomNumber = Random.nextInt(min, max + 1)
                    if (randomNumber !in indexesArray) {
                        indexesArray.add(randomNumber)
                    }
                }

                if (videoList.isNotEmpty()) {
                    for (img in 1..indexesArray.size) {
                        val randomTime = Random.nextLong(120, 230)
                        delay(randomTime)
                        withContext(Dispatchers.Main) {
                            val chosenVideo: FileModel = videoList[indexesArray[img - 1]]
                            currentList.add(0, chosenVideo)
                            currentList.add(chosenVideo)
                        }
                    }

                    if (!isSameList(currentList, deletedVideosAdapter.currentList)) {
                        deletedVideosAdapter.submitList(currentList.toList())
                        viewBinding.rvDeletedVideos.scrollToPosition(0)
                    }

                    withDelay(2000) {
                        showDialog()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        viewBinding.cardScanProgress.visibility = View.GONE
                        viewBinding.llEmptyFolder.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    private fun getThreeRandomPositions(list: List<Any>): List<Int> {
        val random = Random
        val positions = mutableListOf<Int>()
        val count = viewModel.preferences.getRewardCount()
        val adCount = list.size / 3
        while (positions.size < adCount && list.size >= adCount && count > 0) {
            val randomPosition = random.nextInt(list.size)
            if (!positions.contains(randomPosition)) {
                positions.add(randomPosition)
            }
        }

        return positions
    }

    private fun changeColor(
        string: String,
        firstText: Int,
        secondText: Int
    ): SpannableStringBuilder {
        val words = string.split(" ")
        val firstWord = words.getOrElse(0) { "" }
        val secondWord = words.getOrElse(1) { "" }

        val builder = SpannableStringBuilder(string)
        builder.setSpan(ForegroundColorSpan(firstText), 0, firstWord.length, 0)
        builder.setSpan(
            ForegroundColorSpan(secondText),
            firstWord.length + 1,
            firstWord.length + secondWord.length + 1,
            0
        )

        return builder
    }

    private fun getTrashedList(resource: Resource<MainFileModel>) {
        when (resource.status) {
            Status.SUCCESS -> {
                if (resource.data?.videos?.size == 0 || resource.data?.videos.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                } else {
                    viewBinding.llEmptyFolder.visibility = View.GONE
                    if (!isSameList(deletedVideosAdapter.currentList, resource.data?.videos)) {
                        deletedVideosAdapter.submitList(resource.data?.videos)
                    }
                    resource.data?.videos?.let { checkProgressBar(it) }
                }
                viewBinding.progressBar.visibility = View.GONE
            }

            Status.ERROR -> {
                viewBinding.progressBar.visibility = View.GONE
                viewBinding.llEmptyFolder.visibility = View.VISIBLE
            }

            Status.LOADING -> {
                if(RecoveryApplication.isPremium) {
                    viewBinding.progressLl.visibility = View.GONE
                } else {
                    viewBinding.progressLl.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun isSameList(list1: List<FileModel>?, list2: List<FileModel>?): Boolean {
        return list1 == list2 || (list1 != null && list2 != null && list1.size == list2.size && list1.containsAll(list2))
    }
    private fun checkProgressBar(list: ArrayList<FileModel>) {
        if (list.size.compareTo(0) != 0) {
            viewBinding.progressBar.visibility = View.GONE
            if (activity != null && context != null) {
                askRatings()
            } else {
                return
            }
        }
    }

//    private fun onItemClicked(video: FileModel) {
//        navigateToDeletedVideoActivity(video)
//    }

    private fun navigateToSubscription() {
        val intent = Intent(requireContext(), SubscriptionActivity::class.java)
            .putExtra(Constants.IS_FREE_SCAN, true)
        startActivity(intent)
    }
    private fun navigateToDeletedVideoActivity(video: FileModel) {
        val intent = Intent(requireContext().applicationContext, DeletedVideoActivity::class.java)
        intent.putExtra("videoUri", video)
        intent.putExtra(Constants.VIDEO_PATH, video)
        intent.putExtra(Constants.IS_DELETED, true)
        intent.putExtra(Constants.PREVIEW_MODE, true)
        startActivity(intent)
    }


    private fun setupBackButton() {
        viewBinding.backButton.setOnClickListener {
            val intent = Intent(requireContext(), ImagesActivity::class.java)
            startActivity(intent)
        }
    }
    private fun askRatings() {
        val manager: ReviewManager = ReviewManagerFactory.create(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.result
                val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {

                }.addOnFailureListener {

                }
            }
        }
    }

}