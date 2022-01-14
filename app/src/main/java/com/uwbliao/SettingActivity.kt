package com.uwbliao

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.uwbliao.databinding.ActivitySettingBinding
import com.uwbliao.db.Gender
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {
    private lateinit var settingViewModel: SettingViewModel
    private var _binding: ActivitySettingBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//menu action
        settingViewModel = ViewModelProvider(
            this, SettingViewModelFactory(application)
        )[SettingViewModel::class.java]
        //config scan numbers
        configScanNums()
        //read from db
        settingViewModel.mynickname.observe(this, {
            binding.txtMyNickname.setText(it)
        })
        settingViewModel.mygender.observe(this, {
            when (it) {
                Gender.MALE -> {
                    binding.rdbMyGenderMale.isChecked = true
                }
                Gender.FEMALE -> {
                    binding.rdbMyGenderFemale.isChecked = true
                }
                else -> {
                    binding.rdbMyGenderOther.isChecked = true
                }
            }
        })
        settingViewModel.remoteGender.observe(this, {
            when (it) {
                Gender.MALE -> {
                    binding.rdbRemoteGenderMale.isChecked = true
                }
                Gender.FEMALE -> {
                    binding.rdbRemoteGenderFemale.isChecked = true
                }
                else -> {
                    binding.rdbRemoteGenderOther.isChecked = true
                }
            }
        })
        //write to db
        binding.txtMyNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                lifecycleScope.launch { storeMyNickname(s.toString()) }
            }
        })
        binding.rdbMyGenderMale.setOnClickListener {
            lifecycleScope.launch { storeMyGender(Gender.MALE) }
        }
        binding.rdbMyGenderFemale.setOnClickListener {
            lifecycleScope.launch { storeMyGender(Gender.FEMALE) }
        }
        binding.rdbMyGenderOther.setOnClickListener {
            lifecycleScope.launch { storeMyGender(Gender.OTHER) }
        }
        binding.rdbRemoteGenderMale.setOnClickListener {
            lifecycleScope.launch { storeRemoteGender(Gender.MALE) }
        }
        binding.rdbRemoteGenderFemale.setOnClickListener {
            lifecycleScope.launch { storeRemoteGender(Gender.FEMALE) }
        }
        binding.rdbRemoteGenderOther.setOnClickListener{
            lifecycleScope.launch { storeRemoteGender(Gender.OTHER) }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val data = Intent()
            setResult(Activity.RESULT_OK, data)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }
    //config scan num number picker
    private fun configScanNums() {
        binding.scanNums.setMinValue(0)
        binding.scanNums.setMaxValue(SCAN_NUMS_MAX)
        //read from db
        settingViewModel.scanNums.observe(this, {
            binding.scanNums.value = it
            scanRemoteNums = it
        })
        //write to db
        binding.scanNums.setOnValueChangedListener { picker, oldVal, newVal ->
            lifecycleScope.launch { storeScanNums(newVal) }
            scanRemoteNums = newVal
        }
    }

    //write to db
    private suspend fun storeMyNickname(mynickname: String) {
        settingViewModel.entitySetting.nickName = mynickname
        settingViewModel.updateCurrent(settingViewModel.entitySetting)
    }
    private suspend fun storeMyGender(mygender: Int) {
        settingViewModel.entitySetting.myGender = mygender
        settingViewModel.updateCurrent(settingViewModel.entitySetting)
    }
    private suspend fun storeRemoteGender(remotegender: Int) {
        settingViewModel.entitySetting.remoteGender = remotegender
        settingViewModel.updateCurrent(settingViewModel.entitySetting)
    }
    private suspend fun storeScanNums(scannums: Int) {
        settingViewModel.entitySetting.scanNums = scannums
        settingViewModel.updateCurrent(settingViewModel.entitySetting)
    }

    companion object {
        private val TAG = SettingActivity::class.java.simpleName
        var scanRemoteNums: Int = 1
    }
}