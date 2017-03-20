package data;

/**
 * Created by Michael on 2/25/2017.
 */

import java.util.List;

import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.Transaction;

public class BlockExplorerClass {
    public static String retrieveUnconfirmedTransactions() throws Exception {
        // Instantiate a block explorer
        BlockExplorer blockExplorer = new BlockExplorer();

        // Get a list of currently unconfirmed transactions and print the relay IP address for each
        List<Transaction> unconfirmedTxs = blockExplorer.getUnconfirmedTransactions();

        return unconfirmedTxs.get(1).getHash(); // Return the first hash in this block
    }
}
