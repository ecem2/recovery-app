package com.adentech.recovery.viewPager

import com.adentech.recovery.R

class SetPagerDummy {
    companion object{
        fun setDataPager(): ArrayList<PagerModel>{
            val list = ArrayList<PagerModel>()
            list.add(
                PagerModel("Remove Duplicates", "You can delete duplicated\nphotos and videos", R.mipmap.ic_launcher_supscription)
            )
            list.add(
                PagerModel("AAAAAAA","BBBBBB", R.mipmap.ic_onboard_image)
            )
            list.add(
                PagerModel("CCCCCC", "DDDDDD", R.mipmap.ic_flame)
            )
            return list
        }
    }
}