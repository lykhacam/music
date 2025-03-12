package com.example.myapplication.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.UserAction

class S1ViewModel : ViewModel(){
    private val _userAction = MutableLiveData<UserAction>()
    val userAction: LiveData<UserAction> get() = _userAction

    fun onContinueClicked() {
        _userAction.value = UserAction("Continue Clicked")
    }
}