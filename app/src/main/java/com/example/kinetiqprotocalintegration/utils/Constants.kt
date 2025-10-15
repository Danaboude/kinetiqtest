package com.example.kinetiqprotocalintegration.utils

object Constants {
    // Network Configuration
    const val HYPERLIQUID_RPC_URL = "https://rpc.hyperliquid.xyz/evm"
    const val HYPERLIQUID_CHAIN_ID = 999L // Hyperliquid mainnet chain ID
    
    // Kinetiq Contract Addresses (Hyperliquid Mainnet)
    const val STAKING_MANAGER_ADDRESS = "0x393D0B87Ed38fc779FD9611144aE649BA6082109"
    const val KHYPE_TOKEN_ADDRESS = "0xfD739d4e423301CE9385c1fb8850539D657C296D"
    const val STAKING_ACCOUNTANT_ADDRESS = "0x9209648Ec9D448EF57116B73A2f081835643dc7A"
    const val VALIDATOR_MANAGER_ADDRESS = "0x4b797A93DfC3D18Cf98B7322a2b142FA8007508f"
    
    // WalletConnect Configuration
    const val WALLET_CONNECT_PROJECT_ID = "your-project-id" // Replace with actual project ID
    const val WALLET_CONNECT_METADATA_NAME = "Kinetiq Staking"
    const val WALLET_CONNECT_METADATA_DESCRIPTION = "Stake HYPE tokens for kHYPE rewards"
    const val WALLET_CONNECT_METADATA_URL = "https://kinetiq.xyz"
    const val WALLET_CONNECT_METADATA_ICON = "https://kinetiq.xyz/icon.png"
    
    // Error Messages
    const val ERROR_WALLET_NOT_CONNECTED = "Please connect your wallet first"
    const val ERROR_NETWORK_CONNECTION = "Network connection failed. Please check your internet connection."
    const val ERROR_INSUFFICIENT_BALANCE = "Insufficient HYPE balance"
    const val ERROR_BELOW_MIN_STAKE = "Amount below minimum stake requirement"
    const val ERROR_ABOVE_MAX_STAKE = "Amount exceeds maximum stake limit"
    const val ERROR_EXCEEDS_STAKING_LIMIT = "This stake would exceed the protocol's total staking limit"
    const val ERROR_CONTRACT_CALL_FAILED = "Contract call failed. Please try again."
    
    // Validation Messages
    const val VALIDATION_AMOUNT_ZERO = "Amount must be greater than 0"
    const val VALIDATION_AMOUNT_BELOW_MIN = "Amount below minimum stake of"
    const val VALIDATION_AMOUNT_ABOVE_MAX = "Amount exceeds maximum stake of"
    const val VALIDATION_INSUFFICIENT_BALANCE = "Insufficient HYPE balance"
    const val VALIDATION_EXCEEDS_LIMIT = "Would exceed protocol staking limit"
    
    // Gas and Network Errors
    const val ERROR_INSUFFICIENT_GAS = "Insufficient gas funds for transaction"
    const val ERROR_NETWORK_TIMEOUT = "Network timeout. Please try again."
    const val ERROR_RPC_CONNECTION = "Unable to connect to network. Please check your connection."
    const val ERROR_TRANSACTION_REVERT = "Transaction failed. Please check your input and try again."
    
    // Slippage Warning
    const val WARNING_SLIPPAGE = "Received less kHYPE than expected"
}
