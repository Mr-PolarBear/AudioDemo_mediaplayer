package com.jack.ma.audiodemo.base

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    var mIsPause = false

    override fun onResume() {
        super.onResume()
        mIsPause = false
    }


    override fun onPause() {
        super.onPause()
        mIsPause = true
    }

}
