package com.uwbliao

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.uwbliao.db.RepSetting

class MainActivity : AppCompatActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setttings -> {
                val intent = Intent(applicationContext, SettingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startSettingActivity.launch(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //scanNums
        val repsetting = RepSetting()
        SettingActivity.scanRemoteNums = repsetting.entitySetting!!.scanNums
        mainCanvasView = MainCanvasView(this)
        setContentView(mainCanvasView)
    }

    override fun onResume() {
        super.onResume()
//        //observe scanNums
//        _scanRemoteNums = MutableLiveData<Int>().apply {
//            value = SettingActivity.scanRemoteNums
//        }
//        _scanRemoteNums.observe(this,{
//            mainCanvasView.initRemoteDevs()
//        })
    }

    private lateinit var mainCanvasView: MainCanvasView
//    private lateinit var _scanRemoteNums: LiveData<Int>
    //start setting activity
    private val startSettingActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                //do something
            }
        }
}