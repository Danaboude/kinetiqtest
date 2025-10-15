package com.example.kinetiqprotocalintegration.wallet

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials
import com.example.kinetiqprotocalintegration.utils.Constants
import com.example.kinetiqprotocalintegration.utils.PrivateKeyValidator
import com.example.kinetiqprotocalintegration.utils.ValidationResult

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val address: String, val credentials: Credentials) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class WalletConnectManager(private val context: Context) : ViewModel() {
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    init {
        // Simplified initialization for demo purposes
        // In production, this would initialize WalletConnect v2
    }
    
    fun connectWallet() {
        // WalletConnect is not implemented yet
        _connectionState.value = ConnectionState.Error("WalletConnect not implemented. Please use 'Connect with Private Key' for testing.")
    }
    
    fun connectWithPrivateKey(privateKey: String) {
        viewModelScope.launch {
            try {
                _connectionState.value = ConnectionState.Connecting
                
                // Validate private key format
                val validation = PrivateKeyValidator.validate(privateKey)
                if (validation is ValidationResult.Invalid) {
                    _connectionState.value = ConnectionState.Error(validation.error)
                    return@launch
                }
                
                val cleanPrivateKey = (validation as ValidationResult.Valid).cleanedKey
                
                // Create real credentials from the private key
                val credentials = Credentials.create(cleanPrivateKey)
                
                _connectionState.value = ConnectionState.Connected(
                    address = credentials.address,
                    credentials = credentials
                )
                
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Invalid private key: ${e.message}")
            }
        }
    }
    
    fun connectWithMetaMask() {
        // Deep link to MetaMask mobile app
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("metamask://dapp/")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If MetaMask is not installed, open Play Store
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=io.metamask")
            }
            try {
                context.startActivity(playStoreIntent)
            } catch (e2: Exception) {
                _connectionState.value = ConnectionState.Error("MetaMask not available. Please install MetaMask from Play Store.")
            }
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            try {
                // Simplified disconnect for demo purposes
                // In production, this would terminate WalletConnect session
                _connectionState.value = ConnectionState.Disconnected
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Disconnect failed: ${e.message}")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up resources
        disconnect()
    }
}
