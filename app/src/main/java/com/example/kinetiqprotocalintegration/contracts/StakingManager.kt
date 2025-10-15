
package com.example.kinetiqprotocalintegration.contracts

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

class StakingManager(
    contractAddress: String,
    web3j: Web3j,
    credentials: Credentials,
    gasProvider: ContractGasProvider
) : Contract("", contractAddress, web3j, credentials, gasProvider) {

    fun stake(value: BigInteger): RemoteCall<TransactionReceipt> {
        val function = Function(
            "stake",
            emptyList(),
            emptyList()
        )
        return executeRemoteCallTransaction(function, value)
    }

    fun minStakeAmount(): RemoteCall<BigInteger> {
        val function = Function(
            "minStakeAmount",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun maxStakeAmount(): RemoteCall<BigInteger> {
        val function = Function(
            "maxStakeAmount",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun stakingLimit(): RemoteCall<BigInteger> {
        val function = Function(
            "stakingLimit",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun totalStaked(): RemoteCall<BigInteger> {
        val function = Function(
            "totalStaked",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    companion object {
        fun load(
            contractAddress: String,
            web3j: Web3j,
            credentials: Credentials,
            gasProvider: ContractGasProvider
        ): StakingManager {
            return StakingManager(contractAddress, web3j, credentials, gasProvider)
        }
    }
}
