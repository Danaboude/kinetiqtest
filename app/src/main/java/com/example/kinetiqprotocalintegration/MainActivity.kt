
package com.example.kinetiqprotocalintegration

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kinetiqprotocalintegration.ui.StakingViewModel
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqProtocalIntegrationTheme
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import com.example.kinetiqprotocalintegration.ui.LoginScreen
import com.example.kinetiqprotocalintegration.ui.StakingViewModelFactory
import com.example.kinetiqprotocalintegration.utils.Constants
import com.example.kinetiqprotocalintegration.wallet.WalletConnectManager
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    private lateinit var walletManager: WalletConnectManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize WalletConnect manager
        walletManager = WalletConnectManager(this)

        setContent {
            KinetiqProtocalIntegrationTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var userCredentials by remember { mutableStateOf<Credentials?>(null) }
                var userAddress by remember { mutableStateOf<String?>(null) }

                if (isLoggedIn && userCredentials != null && userAddress != null) {
                    // Initialize Web3j and ViewModel after wallet connection
                    val web3j = Web3j.build(HttpService(Constants.HYPERLIQUID_RPC_URL))
                    val stakingViewModelFactory = StakingViewModelFactory(web3j, userCredentials!!)
                    val stakingViewModel: StakingViewModel = viewModel(factory = stakingViewModelFactory)
                    
                    // Load initial data after ViewModel is created
                    LaunchedEffect(userCredentials) {
                        stakingViewModel.loadInitialData()
                    }
                    
                    StakingScreen(
                        viewModel = stakingViewModel,
                        userAddress = userAddress!!,
                        onDisconnect = {
                            isLoggedIn = false
                            userCredentials = null
                            userAddress = null
                            walletManager.disconnect()
                        }
                    )
                } else {
                    LoginScreen(
                        walletManager = walletManager,
                        onLogin = { address, credentials ->
                            userAddress = address
                            userCredentials = credentials
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StakingScreen(
    viewModel: StakingViewModel,
    userAddress: String,
    onDisconnect: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val hypeBalance by viewModel.hypeBalance.collectAsState()
    val kHypeBalance by viewModel.kHypeBalance.collectAsState()
    val expectedKHype by viewModel.expectedKHype.collectAsState()
    val minStakeAmount by viewModel.minStakeAmount.collectAsState()
    val maxStakeAmount by viewModel.maxStakeAmount.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val slippageWarning by viewModel.slippageWarning.collectAsState()

    var amount by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show validation errors in Snackbar
    LaunchedEffect(validationState) {
        val currentState = validationState
        if (currentState is com.example.kinetiqprotocalintegration.ui.ValidationState.Invalid) {
            snackbarHostState.showSnackbar(
                message = "⚠️ ${currentState.message}",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Show slippage warning in Snackbar
    LaunchedEffect(slippageWarning) {
        slippageWarning?.let { warning ->
            snackbarHostState.showSnackbar(
                message = "⚠️ $warning",
                duration = SnackbarDuration.Long,
                actionLabel = "Dismiss"
            )
            viewModel.clearSlippageWarning()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent
            ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues)
            ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stake",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${userAddress.take(6)}...${userAddress.takeLast(4)}",
                        fontSize = 12.sp,
                        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                    )
                    
                    Button(
                        onClick = { viewModel.loadInitialData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            "Refresh",
                            fontSize = 12.sp,
                            color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary
                        )
                    }
                    
                    Button(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqCardBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            "Disconnect",
                            fontSize = 12.sp,
                            color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Staking Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqCardBackground
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder
                )
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // HYPE Input Section
                    Text(
                        text = "HYPE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            viewModel.onStakeAmountChanged(it)
                        },
                        label = { Text("Amount", color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentGreen,
                            unfocusedBorderColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder,
                            focusedLabelColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentGreen,
                            unfocusedLabelColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary,
                            cursorColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentGreen,
                            focusedTextColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                            unfocusedTextColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Conversion Display
                    val conversionRate = if (expectedKHype > BigDecimal.ZERO && amount.isNotEmpty()) {
                        try {
                            val amountBD = BigDecimal(amount)
                            if (amountBD > BigDecimal.ZERO) {
                                expectedKHype.divide(amountBD, 6, java.math.RoundingMode.HALF_UP)
                            } else {
                                BigDecimal.ZERO
                            }
                        } catch (e: Exception) {
                            BigDecimal.ZERO
                        }
                    } else {
                        BigDecimal.ZERO
                    }
                    
                    Text(
                        text = "1 HYPE = ${if (conversionRate > BigDecimal.ZERO) "${conversionRate} kHYPE" else "X kHYPE"}",
                        fontSize = 14.sp,
                        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // kHYPE Output Section
                    Text(
                        text = "kHYPE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = expectedKHype.toString(),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Expected", color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder,
                            unfocusedBorderColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder,
                            focusedLabelColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary,
                            unfocusedLabelColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary,
                            cursorColor = Color.Transparent,
                            focusedTextColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                            unfocusedTextColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stake Button
                    Button(
                        onClick = { viewModel.stake(amount) },
                        enabled = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO > BigDecimal.ZERO && uiState !is com.example.kinetiqprotocalintegration.ui.StakingUiState.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4BBFE2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        if (uiState is com.example.kinetiqprotocalintegration.ui.StakingUiState.Loading) {
                            CircularProgressIndicator(
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                                modifier = Modifier.width(20.dp).height(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            "Stake",
                            color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Balance Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqCardBackground
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "HYPE Balance",
                            fontSize = 12.sp,
                            color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                        )
                        Text(
                            text = hypeBalance.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary
                        )
                    }
                }

                // kHYPE Balance Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqCardBackground
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "kHYPE Balance",
                            fontSize = 12.sp,
                            color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                        )
                        Text(
                            text = kHypeBalance.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Limits Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqCardBackground
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Staking Limits",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Min: $minStakeAmount HYPE",
                        fontSize = 12.sp,
                        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                    )
                    Text(
                        text = "Max: $maxStakeAmount HYPE",
                        fontSize = 12.sp,
                        color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Display
            when (val state = uiState) {
                is com.example.kinetiqprotocalintegration.ui.StakingUiState.Loading -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue.copy(alpha = 0.1f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue,
                                modifier = Modifier.width(20.dp).height(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading data...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue
                            )
                        }
                    }
                }
                is com.example.kinetiqprotocalintegration.ui.StakingUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqSuccess.copy(alpha = 0.1f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            com.example.kinetiqprotocalintegration.ui.theme.KinetiqSuccess
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Stake Successful!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqSuccess
                            )
                            Text(
                                text = "Tx: ${state.txHash}",
                                fontSize = 12.sp,
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                            )
                            Text(
                                text = "Expected: ${state.expectedKHype} kHYPE",
                                fontSize = 12.sp,
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                            )
                            Text(
                                text = "Received: ${state.actualKHype} kHYPE",
                                fontSize = 12.sp,
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
                            )
                        }
                    }
                }
                is com.example.kinetiqprotocalintegration.ui.StakingUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqError.copy(alpha = 0.1f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            com.example.kinetiqprotocalintegration.ui.theme.KinetiqError
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Error",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqError
                            )
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Button(
                                onClick = { viewModel.loadInitialData() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Retry",
                                    color = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
            }
            
            // Custom top Snackbar overlay
            if (snackbarHostState.currentSnackbarData != null) {
                val snackbarData = snackbarHostState.currentSnackbarData
                
                Snackbar(
                    snackbarData = snackbarData!!,
                    containerColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqCardBackground,
                    contentColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary,
                    actionColor = com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .zIndex(1f)
                )
            }
        }
    }
}}