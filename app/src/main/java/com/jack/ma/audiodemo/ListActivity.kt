package com.jack.ma.audiodemo

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jack.ma.audiodemo.adapter.AdapterList
import com.jack.ma.audiodemo.base.BaseActivity
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : BaseActivity() {


    var adapter: AdapterList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        //如果无效  自己找一个测试
//        val url = "https://96.f.1ting.com/5db45066/5140a9d28d2e5005d91915caacc3014b/2019/10/09/09e_Shao/01.mp3"


    }


    override fun onResume() {
        super.onResume()
        if (adapter == null) {
            val url1 = "你听得到-周杰伦.mp3"
            val url2 = "韩国恐龙.mp3"


            val list = listOf(
                    AudioBean(1, url1, null, "60:60", "00:00"),
                    AudioBean(2, url2, null, "60:60", "00:00"),
                    AudioBean(3, url1, null, "60:60", "00:00"),
                    AudioBean(4, url2, null, "60:60", "00:00")

            )

            adapter = AdapterList(this, list)
            rcv_list.layoutManager = LinearLayoutManager(this)
            rcv_list.adapter = adapter
        }
    }


    override fun onDestroy() {
        adapter?.mAudioUtil?.onDestory()
        super.onDestroy()
    }
}
