package team1887.soundrecorderlolipop;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SoundListActivity extends Activity {

    private static final String LOG_TAG = "SoundList";
    private static boolean isPlaying;
    private MediaPlayer mPlayer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPlaying = false;
        setContentView(R.layout.activity_sound_list);

        final ImageButton btnPlay = (ImageButton) findViewById(R.id.btn_play);
        final ImageButton btnStop = (ImageButton) findViewById(R.id.btn_stop);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        final List<String> soundList = new ArrayList<String>();

        final ListView soundListView = (ListView) findViewById(R.id.list_sound);

        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/");
        File[] files = f.listFiles();
        for (File inFile : files) {
            if (!inFile.isDirectory()) {
                String filenameArray[] = inFile.getName().split("\\.");
                String extension = filenameArray[filenameArray.length - 1];
                if (extension.equals("3gp"))
                    soundList.add(filenameArray[0]);
            }
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                soundList);

        soundListView.setAdapter(arrayAdapter);
        soundListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isPlaying)
                    return;
                isPlaying = true;
                String s = (String) soundListView.getItemAtPosition(position);
                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/" + s + ".3gp");
                    mPlayer.prepare();
                    mPlayer.start();
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    registerHeadsetPlugReceiver();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mPlayer.release();
                            isPlaying = false;
                        }
                    });
                } catch (IOException e) {
                    Log.e(LOG_TAG, "prepare() failed");
                }
            }
        });


        btnPlay.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if(isPlaying){
                    mPlayer.pause();
                    isPlaying = !isPlaying;
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                }else{
                    mPlayer.start();
                    isPlaying = !isPlaying;
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });


        btnStop.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                if(isPlaying){
                    mPlayer.pause();
                    mPlayer.release();
                    mPlayer = null;
                    isPlaying = !isPlaying;
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlaying = false;
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


    private void registerHeadsetPlugReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(headsetPlugReceiver, intentFilter);
    }

    private BroadcastReceiver headsetPlugReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                Toast.makeText(getApplicationContext(), "Earphones off!",
                        Toast.LENGTH_SHORT).show();
                mPlayer.pause();
            }
        }

    };
}
