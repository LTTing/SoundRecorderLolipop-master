package team1887.soundrecorderlolipop;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SoundRecordActivity extends Activity {

    private static final String LOG_TAG = "SoundRecord";
    private static String mFileName = null;
    boolean mIsPlaying = false;
    boolean mIsRecording = false;
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;

    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder");
                ret = false;
            }
        }
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/SoundRecorder/tmp/buffer.3gp";
        createDirIfNotExists("/SoundRecorder/tmp/");
        setContentView(R.layout.activity_sound_record);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        final ImageButton recordButton = (ImageButton) findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mIsRecording && !mIsPlaying) {
                    mIsRecording = true;
                    recordButton.setBackgroundResource(R.drawable.ic_stop);
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mRecorder.setOutputFile(mFileName);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                    mRecorder.start();
                    showNotification();
                } else if (mIsRecording) {
                    mIsRecording = false;
                    recordButton.setBackgroundResource(R.drawable.ic_record);
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    NotificationManager mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNM.cancel(1001);
                }
            }
        });

        final ImageButton playButton = (ImageButton) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mIsPlaying && !mIsRecording) {
                    mIsPlaying = true;
                    playButton.setBackgroundResource(R.drawable.ic_stop);
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(mFileName);
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                } else if (mIsPlaying) {
                    mIsPlaying = false;
                    playButton.setBackgroundResource(R.drawable.ic_play);
                    mPlayer.release();
                    mPlayer = null;
                }
            }
        });

        final EditText text = (EditText) findViewById(R.id.record_name);

        ImageButton saveButton = (ImageButton) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "File creation failed : no name", Toast.LENGTH_SHORT).show();
                    return;
                }

                File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/");
                File[] files = f.listFiles();
                for (File inFile : files) {
                    if (!inFile.isDirectory()) {
                        if ((text.getText().toString() + ".3gp").equals(inFile.getName())) {
                            Toast.makeText(getApplicationContext(), "File creation failed : file already exist", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }


                File bufferFile = new File(mFileName);
                File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/" + text.getText() + ".3gp");
                try {
                    copy(bufferFile, newFile);
                    Toast.makeText(getApplicationContext(), "File creation success", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "File creation failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        if (mPlayer != null) {
            try {
                mPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    private void showNotification(){
        Notification n = new Notification(R.drawable.ic_recording, null, System.currentTimeMillis());
        n.flags = Notification.FLAG_ONGOING_EVENT;
        Intent intent = new Intent(SoundRecordActivity.this,SoundRecordActivity.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        n.setLatestEventInfo(this, "SoundRecorder", "It's recording", contentIntent);
        NotificationManager mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNM.notify(1001, n);
    }
}
