package com.desktop.ultraman;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;

public class CloseButtonView extends View {
    private View closeButton;
    public CloseButtonView(final Context context){
        super(context);
    }
    private void touch(){
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
