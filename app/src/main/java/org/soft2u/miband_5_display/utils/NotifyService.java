package org.soft2u.miband_5_display.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import org.soft2u.miband_5_display.R;

import java.io.File;

public class NotifyService extends Service {

    private String TAG = "NotifyService";
    private ServiceHandler mServiceHandler;
    private final int NOTIFICATION_ID = 2001;
    private ResultReceiver receiver;

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service done");
        Toast.makeText(getBaseContext(), R.string.toast_success, Toast.LENGTH_SHORT).show();
        cancelNotify(getBaseContext(), NOTIFICATION_ID);
        Bundle b = new Bundle();
        receiver.send(STATUS_FINISHED, b);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Looper mServiceLooper;
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // crop thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments");
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        receiver = intent.getParcelableExtra("receiver");

        if(intent != null && intent.getAction() != null) {
            byte[] byteInput;
            MiBand5FirmwareInfo firmware;
            String BLEAddress = intent.getExtras().getString("BLE_ADDRESS");
            InstallViaBluetooth installViaBluetooth;

            switch (intent.getAction()) {
                case "updateFirmwareFromUser":
                    String filePath = intent.getExtras().getString("FILE_PATH");
                    byteInput = new General().readFile(new File(Environment.getExternalStorageDirectory(), new General().getRealPath(filePath)));
                    firmware = new MiBand5FirmwareInfo(byteInput);
                    installViaBluetooth = new InstallViaBluetooth();
                    installViaBluetooth.init(this, BLEAddress, firmware);
                    break;
                case "updateFirmwareFromSys":
                    String fileName = intent.getExtras().getString("FILE_PATH");
                    byteInput = new General().readFile(new File(getExternalFilesDir(null), fileName));
                    firmware = new MiBand5FirmwareInfo(byteInput);
                    installViaBluetooth = new InstallViaBluetooth();
                    installViaBluetooth.init(this, BLEAddress, firmware);
                    break;
            }
            showNotify(this, NOTIFICATION_ID);
        }

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    private void showNotify(Context context, int NOTIFICATION_ID) {
        String CHANNEL_ID = "channel_01";// The id of the channel.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            CharSequence CHANNEL_NAME = context.getString(R.string.notify_title_flashing);// The user-visible name of the channel.
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);

            //builder
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(getResources().getString(R.string.notify_title_flashing))
                    .setContentText(getResources().getString(R.string.notify_desc_flashing))
                    .setSmallIcon(R.drawable.ic_nav_install_offline)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setPriority(Notification.DEFAULT_ALL);

            //peding intent
//            Intent intent = new Intent(context, PasswordActivity.class);
//            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.setContentIntent(pi);

            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            //builder
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "");
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                mBuilder.setSmallIcon(R.drawable.ic_launcher_background);
            } else {
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            }

            mBuilder.setAutoCancel(false)
                    .setOngoing(true)
                    .setWhen(0)
                    .setShowWhen(false);

            //peding intent
//            Intent intent = new Intent(context, PasswordActivity.class);
//            PendingIntent pi = PendingIntent.getActivity(context,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.setContentIntent(pi);

            Notification notification = mBuilder.build();
//            notification.flags |= Notification.FLAG_AUTO_CANCEL;
//            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.flags = Notification.FLAG_ONGOING_EVENT;
//            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public void cancelNotify(Context context, int notifyId) {
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(notifyId);
    }
}
