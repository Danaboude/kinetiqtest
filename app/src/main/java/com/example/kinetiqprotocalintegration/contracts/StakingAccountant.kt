
package com.example.kinetiqprotocalintegration.contracts

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteCall
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

class StakingAccountant(
    contractAddress: String,
    web3j: Web3j,
    credentials: Credentials,
    gasProvider: ContractGasProvider
) : Contract("", contractAddress, web3j, credentials, gasProvider) {

    fun HYPEToKHYPE(hypeAmount: BigInteger): RemoteCall<BigInteger> {
        val function = Function(
            "HYPEToKHYPE",
            listOf(Uint256(hypeAmount)),
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
        ): StakingAccountant {
            return StakingAccountant(contractAddress, web3j, credentials, gasProvider)
        }
    }
}
