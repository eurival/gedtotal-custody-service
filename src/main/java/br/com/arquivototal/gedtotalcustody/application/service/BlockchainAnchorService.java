package br.com.arquivototal.gedtotalcustody.application.service;

import br.com.arquivototal.gedtotalcustody.application.service.support.HashUtils;
import br.com.arquivototal.gedtotalcustody.config.BlockchainProperties;
import java.math.BigInteger;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainAnchorService {

    private final BlockchainProperties blockchainProperties;

    public AnchorResult anchor(String merkleRoot) {
        if (!blockchainProperties.enabled()) {
            return new AnchorResult(
                "simulated-" + HashUtils.sha256Hex("tx:" + merkleRoot),
                blockchainProperties.network() != null ? blockchainProperties.network() : "disabled",
                blockchainProperties.anchorAddress(),
                true
            );
        }
        validateConfiguration();

        try (Web3j web3j = Web3j.build(new HttpService(blockchainProperties.rpcUrl()))) {
            Credentials credentials = Credentials.create(blockchainProperties.privateKey());
            RawTransactionManager transactionManager = new RawTransactionManager(
                web3j,
                credentials,
                blockchainProperties.chainId() != null ? blockchainProperties.chainId() : -1L
            );
            String methodName = blockchainProperties.methodName() != null && !blockchainProperties.methodName().isBlank()
                ? blockchainProperties.methodName()
                : "anchorMerkleRoot";
            Function function = new Function(methodName, List.of(new Bytes32(Numeric.hexStringToByteArray(Numeric.prependHexPrefix(merkleRoot)))), List.of());
            String encodedFunction = FunctionEncoder.encode(function);
            EthSendTransaction response = transactionManager.sendTransaction(
                blockchainProperties.gasPriceWei() != null ? blockchainProperties.gasPriceWei() : BigInteger.valueOf(3_000_000_000L),
                blockchainProperties.gasLimit() != null ? blockchainProperties.gasLimit() : BigInteger.valueOf(150_000L),
                blockchainProperties.anchorAddress(),
                encodedFunction,
                BigInteger.ZERO
            );

            if (response.hasError()) {
                throw new IllegalStateException("Erro no envio da transacao blockchain: " + response.getError().getMessage());
            }

            String txHash = response.getTransactionHash();
            log.info("Merkle root ancorada na blockchain network={} txHash={}", blockchainProperties.network(), txHash);
            return new AnchorResult(txHash, blockchainProperties.network(), blockchainProperties.anchorAddress(), false);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao ancorar Merkle root na blockchain", ex);
        }
    }

    private void validateConfiguration() {
        if (blockchainProperties.rpcUrl() == null || blockchainProperties.rpcUrl().isBlank()) {
            throw new IllegalStateException("app.blockchain.rpc-url nao configurado");
        }
        if (blockchainProperties.privateKey() == null || blockchainProperties.privateKey().isBlank()) {
            throw new IllegalStateException("app.blockchain.private-key nao configurado");
        }
        if (blockchainProperties.anchorAddress() == null || blockchainProperties.anchorAddress().isBlank()) {
            throw new IllegalStateException("app.blockchain.anchor-address nao configurado");
        }
    }
}
