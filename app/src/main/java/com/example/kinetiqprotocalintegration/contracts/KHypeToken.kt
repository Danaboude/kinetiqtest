
package com.example.kinetiqprotocalintegration.contracts

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteCall
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

class KHypeToken(
    contractAddress: String,
    web3j: Web3j,
    credentials: Credentials,
    gasProvider: ContractGasProvider
) : Contract("", contractAddress, web3j, credentials, gasProvider) {

    fun balanceOf(account: String): RemoteCall<BigInteger> {
        val function = Function(
            "balanceOf",
            listOf(Address(account)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    companion object {
        fun load(
            contractAddress: String,
            web3j: Web3j,
            credentials: Credentials,
            gasProvider: ContractGasProvider
        ): KHypeToken {
            return KHypeToken(contractAddress, web3j, credentials, gasProvider)
        }
    }
}
