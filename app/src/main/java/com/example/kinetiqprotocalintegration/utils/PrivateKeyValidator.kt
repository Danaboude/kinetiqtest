package com.example.kinetiqprotocalintegration.utils

sealed class ValidationResult {
    data class Valid(val cleanedKey: String) : ValidationResult()
    data class Invalid(val error: String) : ValidationResult()
}

object PrivateKeyValidator {
    
    /**
     * Validates a private key string
     * @param privateKey The private key to validate (with or without 0x prefix)
     * @return ValidationResult.Valid with cleaned key or ValidationResult.Invalid with error message
     */
    fun validate(privateKey: String): ValidationResult {
        val trimmed = privateKey.trim()
        
        // Check if empty
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("Private key cannot be empty")
        }
        
        // Remove 0x prefix if present
        val cleanKey = if (trimmed.startsWith("0x")) {
            trimmed.substring(2)
        } else {
            trimmed
        }
        
        // Check length (should be 64 hex characters)
        if (cleanKey.length != 64) {
            return ValidationResult.Invalid("Private key must be 64 characters long (currently ${cleanKey.length})")
        }
        
        // Check if all characters are valid hex
        if (!cleanKey.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            return ValidationResult.Invalid("Private key contains invalid characters. Only 0-9 and a-f are allowed")
        }
        
        // Check for common invalid patterns
        if (cleanKey.all { it == '0' }) {
            return ValidationResult.Invalid("Private key cannot be all zeros")
        }
        
        if (cleanKey.all { it == '1' }) {
            return ValidationResult.Invalid("Private key cannot be all ones")
        }
        
        return ValidationResult.Valid("0x$cleanKey")
    }
    
    /**
     * Quick validation check
     * @param privateKey The private key to check
     * @return true if valid, false otherwise
     */
    fun isValid(privateKey: String): Boolean {
        return validate(privateKey) is ValidationResult.Valid
    }
    
    /**
     * Clean a private key by removing 0x prefix and ensuring proper format
     * @param privateKey The private key to clean
     * @return Cleaned private key with 0x prefix
     */
    fun clean(privateKey: String): String {
        val trimmed = privateKey.trim()
        val cleanKey = if (trimmed.startsWith("0x")) {
            trimmed.substring(2)
        } else {
            trimmed
        }
        return "0x$cleanKey"
    }
    
    /**
     * Mask private key for display (show only first 6 and last 4 characters)
     * @param privateKey The private key to mask
     * @return Masked private key for safe display
     */
    fun mask(privateKey: String): String {
        if (privateKey.length < 10) return "****"
        return "${privateKey.take(6)}...${privateKey.takeLast(4)}"
    }
}
