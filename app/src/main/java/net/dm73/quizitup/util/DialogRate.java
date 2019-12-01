package net.dm73.quizitup.util;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.dm73.quizitup.R;


public class DialogRate extends DialogFragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.rate_dialogue, null);

        //adding animation to logo play
        ImageView logoPlay = (ImageView) root.findViewById(R.id.logoPlay);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_logo_play);
        logoPlay.startAnimation(animation);

        //adding functionality to the buttons
        Button goToPlya = (Button) root.findViewById(R.id.goToPlay);
        goToPlya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RateQuizApp.ModifyPreferencesParametersOtherLunch(getContext());
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + RateQuizApp.APP_PNAME)));
                dismiss();
            }
        });

        Button later = (Button) root.findViewById(R.id.laterButton);
        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RateQuizApp.ModifyPreferencesParametersLater(getContext());
                dismiss();
            }
        });

        return root;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;

    }
}
