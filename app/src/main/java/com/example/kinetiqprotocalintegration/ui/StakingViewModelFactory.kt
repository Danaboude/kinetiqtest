
package com.example.kinetiqprotocalintegration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j

class StakingViewModelFactory(private val web3j: Web3j, private val credentials: Credentials) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StakingViewModel::class.java)) {
            return StakingViewModel(web3j, credentials) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
