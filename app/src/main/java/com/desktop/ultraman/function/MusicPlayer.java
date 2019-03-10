package com.desktop.ultraman.function;

import android.content.Context;
import android.media.MediaPlayer;

import com.desktop.ultraman.R;

import java.io.IOException;

public class MusicPlayer {
    private static MediaPlayer mediaPlayer;
    private static int[] musics=new int[]{R.raw.music01,R.raw.music02,R.raw.music03};

    public static void playVoice(Context context, final int flag){
        if(flag==1)
            mediaPlayer=MediaPlayer.create(context,musics[(int) (Math.random()*3)]);
        else if(flag==2)
            mediaPlayer=MediaPlayer.create(context,R.raw.neko);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(flag==1)
                    mediaPlayer.start();
                else if(flag==2)
                    mediaPlayer.stop();
            }
        });
    }

    public static void stopVoice(){
        if(null!=mediaPlayer){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
}
