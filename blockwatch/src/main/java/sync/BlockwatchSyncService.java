package sync;

/**
 * Created by Michael on 3/11/2017.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BlockwatchSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static BlockwatchSyncAdapter sBlockwatchSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("BlockwatchSyncService", "onCreate - BlockwatchSyncService");
        synchronized (sSyncAdapterLock) {
            if (sBlockwatchSyncAdapter == null) {
                sBlockwatchSyncAdapter = new BlockwatchSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sBlockwatchSyncAdapter.getSyncAdapterBinder();
    }
}
