
package com.example.kinetiqprotocalintegration.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentBlue
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentGreen
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqAccentPink
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqBackground
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqBorder
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqCardBackground
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqError
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextPrimary
import com.example.kinetiqprotocalintegration.ui.theme.KinetiqTextSecondary
import com.example.kinetiqprotocalintegration.utils.PrivateKeyValidator
import com.example.kinetiqprotocalintegration.wallet.ConnectionState
import com.example.kinetiqprotocalintegration.wallet.WalletConnectManager

@Composable
fun LoginScreen(
    walletManager: WalletConnectManager,
    onLogin: (String, org.web3j.crypto.Credentials) -> Unit
) {
    val connectionState by walletManager.connectionState.collectAsState()
    var privateKey by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(true) }
    var validationError by remember { mutableStateOf("") }
    
    // Handle connection state changes
    LaunchedEffect(connectionState) {
        when (val currentState = connectionState) {
            is ConnectionState.Connected -> {
                onLogin(currentState.address, currentState.credentials)
            }
            else -> {}
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KinetiqBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Kinetiq Title
        Text(
            text = "Kinetiq",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = KinetiqTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Stake HYPE & other tokens for kHYPE",
            fontSize = 16.sp,
            color = KinetiqTextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Security Warning Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = KinetiqError.copy(alpha = 0.1f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                KinetiqError
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ FOR TESTING ONLY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = KinetiqError
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Never use wallets with large amounts\n• Use test wallets only\n• Private keys are stored in memory only\n• Never share your private key",
                    fontSize = 12.sp,
                    color = KinetiqTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Private Key Input Field
        OutlinedTextField(
            value = privateKey,
            onValueChange = { 
                privateKey = it
                validationError = ""
            },
            label = { 
                Text(
                    "Private Key", 
                    color = KinetiqTextSecondary
                ) 
            },
            placeholder = { 
                Text(
                    "Enter your wallet's private key...", 
                    color = KinetiqTextSecondary.copy(alpha = 0.6f)
                ) 
            },
            visualTransformation = if (showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KinetiqAccentBlue,
                unfocusedBorderColor = KinetiqBorder,
                focusedLabelColor = KinetiqAccentBlue,
                unfocusedLabelColor = KinetiqTextSecondary,
                cursorColor = KinetiqAccentBlue,
                focusedTextColor = KinetiqTextPrimary,
                unfocusedTextColor = KinetiqTextPrimary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            isError = validationError.isNotEmpty()
        )
        
        // Validation Error
        if (validationError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = validationError,
                color = KinetiqError,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Connect Button
        Button(
            onClick = {
                val validation = PrivateKeyValidator.validate(privateKey)
                if (validation is com.example.kinetiqprotocalintegration.utils.ValidationResult.Valid) {
                    walletManager.connectWithPrivateKey(privateKey)
                } else {
                    validationError = (validation as com.example.kinetiqprotocalintegration.utils.ValidationResult.Invalid).error
                }
            },
            enabled = connectionState !is ConnectionState.Connecting && privateKey.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(KinetiqAccentGreen, KinetiqAccentBlue, KinetiqAccentPink)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            when (connectionState) {
                is ConnectionState.Connecting -> {
                    CircularProgressIndicator(
                        color = KinetiqTextPrimary,
                        modifier = Modifier.width(20.dp).height(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Connecting...",
                        color = KinetiqTextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                else -> {
                    Text(
                        "Connect with Private Key",
                        color = KinetiqTextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        // Connection Error Message
        when (val currentState = connectionState) {
            is ConnectionState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = KinetiqError.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        KinetiqError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Connection Error",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = KinetiqError
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentState.message,
                            color = KinetiqTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            else -> {}
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Alternative connection methods
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    walletManager.connectWallet()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KinetiqCardBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "WalletConnect",
                    color = KinetiqTextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Button(
                onClick = {
                    walletManager.connectWithMetaMask()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KinetiqCardBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "MetaMask",
                    color = KinetiqTextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
