package com.uwbliao

import android.app.Application
import androidx.lifecycle.*
import com.uwbliao.db.EntitySetting
import com.uwbliao.db.RepSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
class SettingViewModelFactory(private val application: Application):
    ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingViewModel(
            application
        ) as T
    }
}
class SettingViewModel(application: Application) : AndroidViewModel(application) {
    val repsetting = RepSetting()
    var entitySetting = repsetting.entitySetting!!
    suspend fun updateCurrent(entitySetting: EntitySetting?) {
        repsetting.updateCurrent(entitySetting)
    }

    private val _mynickname = MutableLiveData<String>().apply {
        value = entitySetting.nickName
    }
    val mynickname: LiveData<String> = _mynickname

    private val _mygender = MutableLiveData<Int>().apply {
        value = entitySetting.myGender
    }
    val mygender: LiveData<Int> = _mygender

    private val _remoteGender = MutableLiveData<Int>().apply {
        value = entitySetting.remoteGender
    }
    val remoteGender: LiveData<Int> = _remoteGender
}