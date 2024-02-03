package com.adentech.recovery.ui.result.deleted

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.adentech.recovery.BuildConfig
import com.adentech.recovery.R
import com.adentech.recovery.RecoveryApplication
import com.adentech.recovery.common.Constants
import com.adentech.recovery.common.Constants.IMAGE_PATH
import com.adentech.recovery.common.Constants.IS_DELETED
import com.adentech.recovery.core.activities.BaseActivity
import com.adentech.recovery.core.common.Resource
import com.adentech.recovery.core.common.Status
import com.adentech.recovery.core.fragments.BaseFragment
import com.adentech.recovery.data.model.FileModel
import com.adentech.recovery.data.model.MainFileModel
import com.adentech.recovery.databinding.FragmentDeletedImagesBinding
import com.adentech.recovery.extensions.observe
import com.adentech.recovery.extensions.withDelay
import com.adentech.recovery.fbevent.FBEventManager
import com.adentech.recovery.ui.result.ImagesActivity
import com.adentech.recovery.ui.result.fullscreen.DeletedImageActivity
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
import java.io.File
import kotlin.random.Random


//import android.annotation.SuppressLint
//import android.app.Activity
//import android.app.Dialog
//import android.content.Context
//import android.content.Intent
//import android.os.Handler
//import android.os.Looper
//import android.text.SpannableStringBuilder
//import android.text.style.ForegroundColorSpan
//import android.view.View
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.GridLayoutManager
//import com.adentech.recovery.BuildConfig
//import com.adentech.recovery.R
//import com.adentech.recovery.RecoveryApplication
//import com.adentech.recovery.common.ArgumentKey
//import com.adentech.recovery.common.Constants
//import com.adentech.recovery.common.Constants.FROM_SUBSCRIPTION
//import com.adentech.recovery.common.Constants.IS_FREE_SCAN
//import com.adentech.recovery.core.activities.BaseActivity
//import com.adentech.recovery.core.common.Resource
//import com.adentech.recovery.core.common.Status
//import com.adentech.recovery.data.model.FileModel
//import com.adentech.recovery.databinding.ActivityFreeScanBinding
//import com.adentech.recovery.databinding.DialogPermissionBinding
//import com.adentech.recovery.databinding.FragmentDeletedImagesBinding
//import com.adentech.recovery.extensions.observe
//import com.adentech.recovery.extensions.withDelay
//import com.adentech.recovery.fbevent.FBEventManager
//import com.adentech.recovery.ui.home.MainViewModel
//import com.adentech.recovery.ui.result.ImagesActivity
//import com.adentech.recovery.ui.result.fullscreen.DeletedImageActivity
//import com.adentech.recovery.ui.subscription.SubscriptionActivity
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.MobileAds
//import com.google.android.gms.ads.interstitial.InterstitialAd
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
//import com.google.android.gms.ads.rewarded.RewardedAd
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kotlin.random.Random
//
//@AndroidEntryPoint
//class DeletedImagesFragment : BaseActivity<MainViewModel, FragmentDeletedImagesBinding>() {
//
//    private val deletedImagesAdapter by lazy {
//        DeletedImagesAdapter(
//            hasReward = false,
//            isSubsClosed = false,
//            context = this@DeletedImagesFragment,
//            onItemClicked = ::onItemClicked
//        )
//    }
//    private var rewardedCount = 0
//    private var mInterstitialAd: InterstitialAd? = null
//    private var mRewardedAd: RewardedAd? = null
//    private val currentList: ArrayList<FileModel> = ArrayList()
//    private var rewardedList: List<Int>? = null
//    private var mUserEarnedReward = false
//    private var elapsedTime: Long = 0
//    private var twentyFourHoursInMillis: Int = 0
//
//    override fun viewDataBindingClass() = FragmentDeletedImagesBinding::class.java
//    override fun viewModelClass() = MainViewModel::class.java
//
//    @SuppressLint("NotifyDataSetChanged")
//    override fun onInitDataBinding() {
//        handleBackPressed()
//        setupRecyclerView()
//        checkStoragePermissions()
//        observe(isStoragePermissionGranted, ::getStoragePermission)
//
//        rewardedCount = viewModel.preferences.getRewardCount()
//        if (rewardedCount > 0) {
//            deletedImagesAdapter.hasReward = true
//            deletedImagesAdapter.notifyDataSetChanged()
//        }
//    }
//
//    private fun getStoragePermission(isGranted: Boolean) {
//        if (isGranted) {
//            setupImages()
//        } else {
//            if (isStoragePermissionGranted.value == false) {
//                showPermissionDialog()
//            }
//        }
//    }
//
//    private fun changeColor(
//        string: String,
//        firstText: Int,
//        secondText: Int
//    ): SpannableStringBuilder {
//        val words = string.split(" ")
//        val firstWord = words.getOrElse(0) { "" }
//        val secondWord = words.getOrElse(1) { "" }
//
//        val builder = SpannableStringBuilder(string)
//        builder.setSpan(ForegroundColorSpan(firstText), 0, firstWord.length, 0)
//        builder.setSpan(
//            ForegroundColorSpan(secondText),
//            firstWord.length + 1,
//            firstWord.length + secondWord.length + 1,
//            0
//        )
//
//        return builder
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    private fun showDialog() {
//        viewBinding.cardScanProgress.visibility = View.GONE
//        viewBinding.clResultDialog.visibility = View.VISIBLE
//        deletedImagesAdapter.rewardedList = rewardedList
//        deletedImagesAdapter.notifyDataSetChanged()
//        val dialogText = getString(R.string.deleted_photos_were_found)
//        val color = ContextCompat.getColor(this@DeletedImagesFragment, R.color.dialog_red_text)
//        val coloredText = changeColor(dialogText, color, color)
//        viewBinding.dialogTitle.text = coloredText
//        viewBinding.buttonRestoreNow.setOnClickListener {
//            viewModel.isProgressDone = true
//            navigateToSubscription()
//            deletedImagesAdapter.isSubsClosed = true
//            deletedImagesAdapter.notifyDataSetChanged()
//        }
//
////        viewBinding.apply {
////            if (rewardedCount > 0) {
////                buttonWatchAds.background =
////                    ContextCompat.getDrawable(this@DeletedImagesFragment, R.drawable.bg_ads_button)
////                buttonWatchAds.setOnClickListener {
////                    viewModel.isProgressDone = true
////                    viewBinding.clResultDialog.visibility = View.GONE
////                    deletedImagesAdapter.isSubsClosed = true
////                    deletedImagesAdapter.notifyDataSetChanged()
////                }
////            } else {
////                recoverButtonTitleOne.text = getString(R.string.watch_ads_exhausted)
////                recoverButtonTitleOne.setTextColor(
////                    ContextCompat.getColor(
////                        this@DeletedImagesFragment,
////                        R.color.white
////                    )
////                )
////                premiumIconOne.visibility = View.GONE
////                buttonWatchAds.isClickable = false
////                buttonWatchAds.isEnabled = false
////                buttonWatchAds.background =
////                    ContextCompat.getDrawable(this@DeletedImagesFragment, R.drawable.bg_disable_button)
////            }
////        }
//
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun setupRecyclerView() {
//        deletedImagesAdapter.submitList(ArrayList())
//        viewBinding.rvDeletedImages.apply {
//            adapter = deletedImagesAdapter
//            itemAnimator = null
//            layoutManager = GridLayoutManager(this@DeletedImagesFragment, 3)
//            setHasFixedSize(true)
//            setOnTouchListener { _, _ -> !viewModel.isProgressDone }
//        }
//    }
//
//    private fun getImagesList(resource: Resource<ArrayList<FileModel>>) {
//        when (resource.status) {
//            Status.SUCCESS -> {
//                resource.data?.let { adjustImages(it) }
//            }
//
//            Status.ERROR -> {
//                viewBinding.cardScanProgress.visibility = View.GONE
//                if (resource.data.isNullOrEmpty()) {
//                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
//                }
//            }
//
//            Status.LOADING -> {
//                viewBinding.cardScanProgress.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    private fun adjustImages(imageList: ArrayList<FileModel>) {
//        FBEventManager().logEvent("image_list_size", "image_list_size", imageList.size.toString())
//        val min = imageList.size / 5
//        val max = imageList.size / 2
//        val count = if (imageList.size / 50 > 3) {
//            if (BuildConfig.DEBUG) 10 else 160
//        } else {
//            imageList.size / 10 + 1
//        }
//
//        val indexesArray: ArrayList<Int> = ArrayList()
//        lifecycleScope.launch {
//            if (imageList.isNotEmpty()) {
//                viewBinding.llEmptyFolder.visibility = View.GONE
//                viewBinding.tvProgressBar.text = getString(R.string.photos_recovered)
//
//                while (indexesArray.size < count) {
//                    val randomNumber = Random.nextInt(min, max + 1)
//                    if (randomNumber !in indexesArray) {
//                        indexesArray.add(randomNumber)
//                    }
//                }
//                for (img in 1..indexesArray.size) {
//                    val randomTime = Random.nextLong(120, 230)
//                    delay(randomTime)
//                    withContext(Dispatchers.Main) {
//                        val chosenImage: FileModel = imageList[indexesArray[img - 1]]
//                        currentList.add(0, chosenImage)
//                        val newList: ArrayList<FileModel> = ArrayList()
//                        newList.addAll(currentList)
//                        deletedImagesAdapter.submitList(newList)
//                        rewardedList = getThreeRandomPositions(newList)
//                        viewBinding.rvDeletedImages.scrollToPosition(0)
//                    }
//                }
//                withDelay(2000, ::showDialog)
//            } else {
//                withContext(Dispatchers.Main) {
//                    viewBinding.cardScanProgress.visibility = View.GONE
//                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
//                }
//            }
//        }
//    }
//
//    private fun navigateToSubscription() {
//        val intent = Intent(this@DeletedImagesFragment, SubscriptionActivity::class.java)
//            .putExtra(IS_FREE_SCAN, true)
//        startForResult.launch(intent)
//    }
//
//    private fun onItemClicked(fileModel: FileModel, i: Int) {
//        if (viewModel.isProgressDone) {
//            if (fileModel.isSelected == true) {
//                navigateToImageActivity(fileModel)
//            } else {
//                if (rewardedCount > 0) {
//                    if (fileModel.isRewarded == true) {
//                        fileModel.isSelected = true
//                        showRewardedAds(fileModel, i)
//                    } else {
//                        fileModel.isSelected = false
//                        navigateToSubscription()
//                    }
//                } else {
//                    navigateToSubscription()
//                }
//            }
//        }
//    }
//
//    private fun getThreeRandomPositions(list: List<Any>): List<Int> {
//        val random = Random
//        val positions = mutableListOf<Int>()
//        val count = viewModel.preferences.getRewardCount()
//        val adCount = list.size / 3
//        while (positions.size < adCount && list.size >= adCount && count > 0) {
//            val randomPosition = random.nextInt(list.size)
//            if (!positions.contains(randomPosition)) {
//                positions.add(randomPosition)
//            }
//        }
//
//        return positions
//    }
//
//    private fun navigateToImageActivity(image: FileModel) {
//        val intent = Intent(this@DeletedImagesFragment, DeletedImageActivity::class.java)
//        intent.putExtra(Constants.IMAGE_PATH, image)
//        intent.putExtra(Constants.IS_DELETED, true)
//        startActivity(intent)
//    }
//
//    private fun showPermissionDialog() {
//        viewBinding.cardScanProgress.visibility = View.GONE
//        val dialogBuilder = Dialog(this@DeletedImagesFragment, R.style.CustomDialog)
//        val dialogBinding = DialogPermissionBinding.inflate(layoutInflater)
//        dialogBuilder.setContentView(dialogBinding.root)
//        dialogBuilder.window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//
//        dialogBinding.apply {
//            buttonSettings.setOnClickListener {
//                requestForStoragePermissions()
//                dialogBuilder.cancel()
//            }
//
//            cancelButton.setOnClickListener {
//                showPermissionError()
//                dialogBuilder.cancel()
//            }
//        }
//        dialogBuilder.show()
//    }
//
//    private fun showPermissionError() {
//        viewBinding.llPermissionError.visibility = View.VISIBLE
//        viewBinding.buttonSettings.setOnClickListener {
//            requestForStoragePermissions()
//        }
//    }
//
//    private fun setupImages() {
//        viewBinding.llPermissionError.visibility = View.GONE
//        viewModel.getAllGalleryImages()
//        observe(viewModel.imageList, ::getImagesList)
//    }
//
////    private fun showInterAds() {
////        if (mInterstitialAd != null) {
////            mInterstitialAd?.show(this)
////        } else {
////            return
////        }
////    }
//
//    private fun loadInterAd() {
//        val adRequest = AdRequest.Builder().build()
//        InterstitialAd.load(
//            this,
//            BuildConfig.INTERSTITIAL_ID,
//            adRequest,
//            object : InterstitialAdLoadCallback() {
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    mInterstitialAd = null
//                }
//
//                override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                    mInterstitialAd = interstitialAd
//                }
//            })
//    }
//
//    private fun showRewardedAds(fileModel: FileModel, position: Int) {
//        if (mRewardedAd != null) {
//            mRewardedAd?.show(this@DeletedImagesFragment) { p0 ->
//                mUserEarnedReward = p0 != null
//            }
//
//            mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//                @SuppressLint("NotifyDataSetChanged")
//                override fun onAdDismissedFullScreenContent() {
//                    super.onAdDismissedFullScreenContent()
//                    mRewardedAd = null
//                    loadRewardedAd()
//                    if (mUserEarnedReward) {
//                        rewardedCount--
//                        viewModel.preferences.setRewardCount(rewardedCount)
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            fileModel.isSelected = true
//                            deletedImagesAdapter.notifyItemChanged(position)
//                        }, 1000)
//                        mUserEarnedReward = false
//
//                        if (rewardedCount <= 0) {
//                            deletedImagesAdapter.hasReward = false
//                            deletedImagesAdapter.notifyDataSetChanged()
//                        }
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(
//                this@DeletedImagesFragment,
//                "The rewarded ad wasn't ready yet.",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }
//
//    private fun loadRewardedAd() {
//        val adRequest: AdRequest = AdRequest.Builder().build()
//        if (mRewardedAd == null) {
//            RewardedAd.load(
//                this@DeletedImagesFragment,
//                BuildConfig.REWARDED_ID,
//                adRequest,
//                object : RewardedAdLoadCallback() {
//                    override fun onAdFailedToLoad(adError: LoadAdError) {
//                        super.onAdFailedToLoad(adError)
//                        mRewardedAd = null
//                    }
//
//                    override fun onAdLoaded(rewardedAd: RewardedAd) {
//                        super.onAdLoaded(rewardedAd)
//                        mRewardedAd = rewardedAd
//                    }
//                }
//            )
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        val lastSaveTime = viewModel.preferences.getLastResetDate()
//        val currentTime = System.currentTimeMillis()
//        elapsedTime = currentTime - lastSaveTime
//        twentyFourHoursInMillis = 24 * 60 * 60 * 1000
//
//        if (elapsedTime >= twentyFourHoursInMillis) {
//            viewModel.preferences.resetRewardData()
//            viewModel.preferences.setLastResetDate(System.currentTimeMillis())
//            viewModel.preferences.setRewardCount(3)
//            rewardedCount = viewModel.preferences.getRewardCount()
//        }
//    }
//
//    private var startForResult: ActivityResultLauncher<Intent> =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val data: Intent? = result.data
//                val parameterFromSecondActivity = data?.getBooleanExtra(FROM_SUBSCRIPTION, false)
//
//                if (parameterFromSecondActivity == true) {
//                    if (RecoveryApplication.hasSubscription) {
//                        startActivity(Intent(ImagesActivity.newIntent(this@DeletedImagesFragment))).also {
//                            finish()
//                        }
//                    }
//                }
//            }
//        }
//
//    private fun handleBackPressed() {
//        val callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                finishAffinity()
//            }
//        }
//        onBackPressedDispatcher.addCallback(this@DeletedImagesFragment, callback)
//    }
//
//    companion object {
//        fun newIntent(context: Context, returnScreen: String? = null) =
//            Intent(context, DeletedImagesFragment::class.java).apply {
//                putExtra(ArgumentKey.FREE_SCAN_SCREEN, returnScreen)
//            }
//    }
//}

@AndroidEntryPoint
class DeletedImagesFragment : BaseFragment<ScanViewModel, FragmentDeletedImagesBinding>() {

    private val deletedImagesAdapter by lazy {
        DeletedImagesAdapter(
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
    private var isStoragePermissionGranted = false
    var isProgressDone: Boolean = false
    private var rewardedCount = 0


    override fun getResourceLayoutId() = R.layout.fragment_deleted_images

    override fun viewModelClass() = ScanViewModel::class.java

    override fun onInitDataBinding() {
        if (RecoveryApplication.isPremium){
            viewBinding.cardScanProgress.visibility = View.GONE
            viewBinding.progressBarLL.visibility = View.GONE
            viewBinding.clResultDialog.visibility = View.GONE
            viewBinding.deletedImagesCl.visibility = View.GONE

        }else{
            viewBinding.cardScanProgress.visibility = View.VISIBLE
        }

        initRecyclerView()
        //handleBackPressed()
        setupBackButton()
//        if (checkStoragePermissions()) {
//            _isStoragePermissionGranted.postValue(true)
//
//        } else {
//            _isStoragePermissionGranted.postValue(false)
//        }

        if (RecoveryApplication.isPremium) {
            viewModel.getAllTrashedFiles()
            observe(viewModel.allTrashedFilesList, ::getTrashedList)
        } else {
            viewBinding.llPermissionError.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                viewModel.getAllGalleryImages()
                observe(viewModel.imageList, ::getImagesList)
            } else {
                getFileList()
            }
        }
    }

    private fun initRecyclerView() {
        viewBinding.rvDeletedImages.apply {
            adapter = deletedImagesAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.margin_6)
            addItemDecoration(SpacesItemDecoration(spacingInPixels))
            setHasFixedSize(true)
        }
    }

    private fun getFileList() {
        volums = FilesFetcher(requireContext()).getStorageVolumes()
        if (volums.isNotEmpty()) {
            myMotor = SearchAllFile(volums, requireActivity(), requireContext())
            myMotor?.execute(*arrayOfNulls(0))
            deletedImagesAdapter.submitList(ImagesFilesCollector.foundImagesList)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun showDialog() {
        if (RecoveryApplication.isPremium) {
            return
        }
        viewBinding.cardScanProgress.visibility = View.GONE
        viewBinding.clResultDialog.visibility = View.VISIBLE
        deletedImagesAdapter.rewardedList = rewardedList
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
    private fun onItemClicked(fileModel: FileModel, i: Int) {
        navigateToDeletedImageActivity(fileModel)
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
    private fun getImagesList(resource: Resource<ArrayList<FileModel>>) {
        when (resource.status) {
            Status.SUCCESS -> {
                resource.data?.let { if (!RecoveryApplication.isPremium){adjustImages(it)} }
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
    private fun adjustImages(imageList: ArrayList<FileModel>) {
        FBEventManager().logEvent("image_list_size", "image_list_size", imageList.size.toString())
        val min = imageList.size / 5
        val max = imageList.size / 2
        val count = if (imageList.size / 50 > 3) {
            if (BuildConfig.DEBUG) 10 else 160
        } else {
            imageList.size / 10 + 1
        }

        val indexesArray: ArrayList<Int> = ArrayList()

        val currentList: ArrayList<FileModel> = ArrayList()

        lifecycleScope.launch {

            if (imageList.isNotEmpty()) {
                viewBinding.llEmptyFolder.visibility = View.GONE
                viewBinding.tvProgressBar.text = getString(R.string.photos_recovered)

                while (indexesArray.size < count) {
                    val randomNumber = Random.nextInt(min, max + 1)
                    if (randomNumber !in indexesArray) {
                        indexesArray.add(randomNumber)
                    }
                }
                Log.d("salimmm", "indexesArray List ${indexesArray}")

                if (imageList.isNotEmpty()) {
                    for (img in 1..indexesArray.size) {
                        val randomTime = Random.nextLong(120, 230)
                        delay(randomTime)
                        withContext(Dispatchers.Main) {
                            val chosenImage: FileModel = imageList[indexesArray[img - 1]]
                            currentList.add(0, chosenImage)
                            currentList.add(chosenImage)
                            Log.d("salimmm", "Current List ${currentList.count()}")

                            deletedImagesAdapter.submitList(currentList.toList())

                            deletedImagesAdapter.notifyDataSetChanged()

                            viewBinding.rvDeletedImages.scrollToPosition(0)
                        }
                    }
                    Log.d("salimmm", "Gallery List ${imageList.count()}")

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
                if (resource.data == null) {
                    Log.d("salimmm", "Trashed images data is null")
                } else if (resource.data?.images.isNullOrEmpty()) {
                    viewBinding.llEmptyFolder.visibility = View.VISIBLE
                    Log.d("salimmm", "Trashed images list is null or empty")
                } else {
                    viewBinding.llEmptyFolder.visibility = View.GONE
                    Log.d("salimmm", "Trashed images size: ${resource.data?.images?.size}")

                    deletedImagesAdapter.submitList(resource.data?.images)
                    resource.data?.images?.let { checkProgressBar(it) }
                }
                viewBinding.progressBar.visibility = View.GONE
            }

            Status.ERROR -> {
                viewBinding.progressBar.visibility = View.GONE
                viewBinding.llEmptyFolder.visibility = View.VISIBLE
            }

            Status.LOADING -> {
                if (RecoveryApplication.isPremium) {
                    viewBinding.progressLl.visibility = View.GONE
                } else {
                    viewBinding.progressLl.visibility = View.VISIBLE
                }
            }
        }
    }

    //todo bunu sil
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

//    private fun onItemClicked(image: FileModel, position: Int) {
//        navigateToDeletedImageActivity(image)
//    }

    private fun navigateToSubscription() {
        val intent = Intent(requireContext(), SubscriptionActivity::class.java)
            .putExtra(Constants.IS_FREE_SCAN, true)
        startActivity(intent)
    }
    private fun navigateToDeletedImageActivity(image: FileModel) {
        val intent = Intent(requireContext().applicationContext, DeletedImageActivity::class.java)
        intent.putExtra("imageUri", image)
        intent.putExtra(Constants.IMAGE_PATH, image)
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

    fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            //Below android 11
            val write =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED
        }
    }
    private fun showPermissionError() {
        viewBinding.llPermissionError.visibility = View.VISIBLE
        viewBinding.buttonSettings.setOnClickListener {
            requestForStoragePermissions()
        }
    }

    fun requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.toString(), null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                BaseActivity.STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //Android is 11 (R) or above
                isStoragePermissionGranted = Environment.isExternalStorageManager()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BaseActivity.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                isStoragePermissionGranted = read && write
            }
        }
    }


}