package utilities;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Michael on 2/26/2017.
 */

public class BlockchainSyncIntentService extends IntentService{

    public BlockchainSyncIntentService() {
        super("BlockchainSyncIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BlockchainSyncTask.syncTransaction(this);
    }
}
