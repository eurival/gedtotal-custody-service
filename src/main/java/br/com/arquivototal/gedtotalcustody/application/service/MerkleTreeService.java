package br.com.arquivototal.gedtotalcustody.application.service;

import br.com.arquivototal.gedtotalcustody.application.service.support.HashUtils;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MerkleTreeService {

    public String buildRoot(List<String> leaves) {
        if (leaves == null || leaves.isEmpty()) {
            throw new IllegalArgumentException("Nao e possivel gerar Merkle root sem folhas");
        }
        List<String> currentLevel = new ArrayList<>(leaves);
        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = i + 1 < currentLevel.size() ? currentLevel.get(i + 1) : left;
                nextLevel.add(HashUtils.sha256Hex(left + right));
            }
            currentLevel = nextLevel;
        }
        return currentLevel.getFirst();
    }
}
