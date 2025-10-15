
package com.example.kinetiqprotocalintegration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinetiqprotocalintegration.contracts.KHypeToken
import com.example.kinetiqprotocalintegration.contracts.StakingAccountant
import com.example.kinetiqprotocalintegration.contracts.StakingManager
import com.example.kinetiqprotocalintegration.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

class StakingViewModel(private val web3j: Web3j, private val credentials: Credentials) : ViewModel() {

    private val _uiState = MutableStateFlow<StakingUiState>(StakingUiState.Idle)
    val uiState: StateFlow<StakingUiState> = _uiState

    private val _hypeBalance = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val hypeBalance: StateFlow<BigDecimal> = _hypeBalance

    private val _kHypeBalance = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val kHypeBalance: StateFlow<BigDecimal> = _kHypeBalance

    private val _expectedKHype = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val expectedKHype: StateFlow<BigDecimal> = _expectedKHype

    private val _minStakeAmount = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val minStakeAmount: StateFlow<BigDecimal> = _minStakeAmount

    private val _maxStakeAmount = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val maxStakeAmount: StateFlow<BigDecimal> = _maxStakeAmount

    private val _stakingLimit = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val stakingLimit: StateFlow<BigDecimal> = _stakingLimit

    private val _totalStaked = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val totalStaked: StateFlow<BigDecimal> = _totalStaked

    private val _validationState = MutableStateFlow<ValidationState>(ValidationState.Valid)
    val validationState: StateFlow<ValidationState> = _validationState

    private val _slippageWarning = MutableStateFlow<String?>(null)
    val slippageWarning: StateFlow<String?> = _slippageWarning

    private val gasProvider = DefaultGasProvider()

    private val stakingManager = StakingManager.load(
        Constants.STAKING_MANAGER_ADDRESS,
        web3j,
        credentials,
        gasProvider
    )

    private val kHypeToken = KHypeToken.load(
        Constants.KHYPE_TOKEN_ADDRESS,
        web3j,
        credentials,
        gasProvider
    )

    private val stakingAccountant = StakingAccountant.load(
        Constants.STAKING_ACCOUNTANT_ADDRESS,
        web3j,
        credentials,
        gasProvider
    )

    init {
        // Don't load data immediately - wait for wallet connection
    }

    fun loadInitialData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    retryWithBackoff(
                        operation = {
                            _uiState.value = StakingUiState.Loading
                        
                        // Load balances
                        val balance = getHypeBalance()
                        _hypeBalance.value = balance
                        
                        val kHype = getKHypeBalance()
                        _kHypeBalance.value = kHype
                        
                        // Load staking limits using proper await()
                        val minStake = stakingManager.minStakeAmount().sendAsync().await()
                        _minStakeAmount.value = Convert.fromWei(minStake.toBigDecimal(), Convert.Unit.ETHER)
                        
                        val maxStake = stakingManager.maxStakeAmount().sendAsync().await()
                        _maxStakeAmount.value = Convert.fromWei(maxStake.toBigDecimal(), Convert.Unit.ETHER)

                        val stakingLimit = stakingManager.stakingLimit().sendAsync().await()
                        _stakingLimit.value = Convert.fromWei(stakingLimit.toBigDecimal(), Convert.Unit.ETHER)

                        val totalStaked = stakingManager.totalStaked().sendAsync().await()
                        _totalStaked.value = Convert.fromWei(totalStaked.toBigDecimal(), Convert.Unit.ETHER)
                        
                        _uiState.value = StakingUiState.Idle
                    },
                    maxRetries = 3
                )
                } catch (e: Exception) {
                    // The retryWithBackoff already sets the error state, but we catch here to prevent crash
                    // Log.e("StakingViewModel", "Error loading initial data: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun <T> retryWithBackoff(
        operation: suspend () -> T,
        maxRetries: Int = 3,
        baseDelay: Long = 1000
    ): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                println("Retry attempt ${attempt + 1} failed: ${e.message}") // Added for debugging
                if (attempt < maxRetries - 1) {
                    kotlinx.coroutines.delay(baseDelay * (1L shl attempt)) // Exponential backoff
                }
            }
        }
        
        // If all retries failed, show appropriate error message
        val errorMessage = when {
            lastException?.message?.contains("404") == true -> "Network endpoint not found. Please check your connection."
            lastException?.message?.contains("timeout") == true -> "Request timeout. Please try again."
            lastException?.message?.contains("connection") == true -> "Network connection failed. Please check your internet."
            lastException?.message?.contains("execution reverted") == true -> "Contract execution failed. Please check your wallet connection."
            lastException?.message?.contains("insufficient funds") == true -> "Insufficient HYPE balance for transaction"
            lastException?.message?.contains("Below minimum stake") == true -> "Stake amount is below the minimum required"
            lastException?.message?.contains("Above maximum stake") == true -> "Stake amount exceeds the maximum allowed"
            lastException?.message?.contains("Would exceed staking limit") == true -> "This stake would exceed the protocol limit"
            else -> "Failed to load initial data: ${lastException?.message ?: "Unknown error"}"
        }
        _uiState.value = StakingUiState.Error(errorMessage)
        throw lastException ?: Exception("Unknown error")
    }

    fun onStakeAmountChanged(amount: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val amountInHype = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    
                    // Perform real-time validation
                    validateStakeAmount(amountInHype)
                    
                    if (amountInHype > BigDecimal.ZERO) {
                        val amountInWei = Convert.toWei(amountInHype, Convert.Unit.ETHER).toBigInteger()
                        val kHypeAmount = stakingAccountant.HYPEToKHYPE(amountInWei).sendAsync().await()
                        _expectedKHype.value = Convert.fromWei(kHypeAmount.toBigDecimal(), Convert.Unit.ETHER)
                    } else {
                        _expectedKHype.value = BigDecimal.ZERO
                    }
                } catch (e: Exception) {
                    // Don't show error for calculation failures, just reset to zero
                    _expectedKHype.value = BigDecimal.ZERO
                }
            }
        }
    }

    private fun validateStakeAmount(amount: BigDecimal) {
        when {
            amount <= BigDecimal.ZERO -> {
                _validationState.value = ValidationState.Invalid(Constants.VALIDATION_AMOUNT_ZERO)
            }
            amount < _minStakeAmount.value -> {
                _validationState.value = ValidationState.Invalid("${Constants.VALIDATION_AMOUNT_BELOW_MIN} ${_minStakeAmount.value} HYPE")
            }
            amount > _maxStakeAmount.value -> {
                _validationState.value = ValidationState.Invalid("${Constants.VALIDATION_AMOUNT_ABOVE_MAX} ${_maxStakeAmount.value} HYPE")
            }
            amount > _hypeBalance.value -> {
                _validationState.value = ValidationState.Invalid(Constants.VALIDATION_INSUFFICIENT_BALANCE)
            }
            _totalStaked.value.plus(amount) > _stakingLimit.value -> {
                _validationState.value = ValidationState.Invalid(Constants.VALIDATION_EXCEEDS_LIMIT)
            }
            else -> {
                _validationState.value = ValidationState.Valid
            }
        }
    }

    fun stake(amount: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _uiState.value = StakingUiState.Loading

                    val stakeAmount = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    
                    // Validation checks
                    when {
                        stakeAmount <= BigDecimal.ZERO -> {
                            _uiState.value = StakingUiState.Error(Constants.ERROR_INSUFFICIENT_BALANCE)
                            return@withContext
                        }
                        stakeAmount > _hypeBalance.value -> {
                            _uiState.value = StakingUiState.Error(Constants.ERROR_INSUFFICIENT_BALANCE)
                            return@withContext
                        }
                        stakeAmount < _minStakeAmount.value -> {
                            _uiState.value = StakingUiState.Error(Constants.ERROR_BELOW_MIN_STAKE)
                            return@withContext
                        }
                        stakeAmount > _maxStakeAmount.value -> {
                            _uiState.value = StakingUiState.Error(Constants.ERROR_ABOVE_MAX_STAKE)
                            return@withContext
                        }
                        _totalStaked.value.plus(stakeAmount) > _stakingLimit.value -> {
                            _uiState.value = StakingUiState.Error(Constants.ERROR_EXCEEDS_STAKING_LIMIT)
                            return@withContext
                        }
                    }

                    val amountInWei = Convert.toWei(stakeAmount, Convert.Unit.ETHER).toBigInteger()
                    val receipt = stakingManager.stake(amountInWei).sendAsync().await()
                    
                    if (receipt.isStatusOK) {
                        val newKHypeBalance = getKHypeBalance()
                        val actualKHypeReceived = newKHypeBalance.minus(_kHypeBalance.value)
                        
                        // Check for slippage
                        checkSlippage(_expectedKHype.value, actualKHypeReceived)
                        
                        _uiState.value = StakingUiState.Success(receipt.transactionHash, _expectedKHype.value, actualKHypeReceived)
                        // Reload balances after successful stake
                        loadInitialData()
                    } else {
                        val errorMessage = receipt.revertReason ?: "Transaction reverted"
                        _uiState.value = StakingUiState.Error("Staking failed: $errorMessage")
                    }

                } catch (e: Exception) {
                    val errorMessage = when {
                        e.message?.contains("insufficient funds") == true -> Constants.ERROR_INSUFFICIENT_BALANCE
                        e.message?.contains("Below minimum stake") == true -> Constants.ERROR_BELOW_MIN_STAKE
                        e.message?.contains("Above maximum stake") == true -> Constants.ERROR_ABOVE_MAX_STAKE
                        e.message?.contains("Would exceed staking limit") == true -> Constants.ERROR_EXCEEDS_STAKING_LIMIT
                        e.message?.contains("insufficient gas") == true -> Constants.ERROR_INSUFFICIENT_GAS
                        e.message?.contains("timeout") == true -> Constants.ERROR_NETWORK_TIMEOUT
                        e.message?.contains("connection") == true -> Constants.ERROR_RPC_CONNECTION
                        e.message?.contains("execution reverted") == true -> Constants.ERROR_TRANSACTION_REVERT
                        e.message?.contains("404") == true -> Constants.ERROR_NETWORK_CONNECTION
                        else -> "Staking failed: ${e.message ?: "Unknown error"}"
                    }
                    _uiState.value = StakingUiState.Error(errorMessage)
                }
            }
        }
    }

    private suspend fun getHypeBalance(): BigDecimal {
        try {
            val balanceInWei = web3j.ethGetBalance(credentials.address, org.web3j.protocol.core.DefaultBlockParameterName.LATEST).sendAsync().await()
            return Convert.fromWei(balanceInWei.balance.toBigDecimal(), Convert.Unit.ETHER)
        } catch (e: Exception) {
            throw Exception("Failed to fetch HYPE balance: ${e.message}")
        }
    }

    private suspend fun getKHypeBalance(): BigDecimal {
        try {
            val balanceInWei = kHypeToken.balanceOf(credentials.address).sendAsync().await()
            return Convert.fromWei(balanceInWei.toBigDecimal(), Convert.Unit.ETHER)
        } catch (e: Exception) {
            throw Exception("Failed to fetch kHYPE balance: ${e.message}")
        }
    }

    private fun checkSlippage(expected: BigDecimal, actual: BigDecimal) {
        if (expected > BigDecimal.ZERO && actual > BigDecimal.ZERO) {
            val difference = expected.minus(actual)
            val percentage = difference.divide(expected, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
            
            if (percentage > BigDecimal("0.5")) {
                _slippageWarning.value = "${Constants.WARNING_SLIPPAGE} (${percentage}% difference)"
            } else {
                _slippageWarning.value = null
            }
        }
    }

    fun clearSlippageWarning() {
        _slippageWarning.value = null
    }
}

sealed class StakingUiState {
    object Idle : StakingUiState()
    object Loading : StakingUiState()
    data class Success(val txHash: String, val expectedKHype: BigDecimal, val actualKHype: BigDecimal) : StakingUiState()
    data class Error(val message: String) : StakingUiState()
}

sealed class ValidationState {
    object Valid : ValidationState()
    data class Invalid(val message: String) : ValidationState()
}
