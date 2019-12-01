package net.dm73.quizitup;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.NativeExpressAdView;
import com.squareup.picasso.Picasso;

import net.dm73.quizitup.dao.DaoQuiz;
import net.dm73.quizitup.model.Quiz;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ItemClickListenner itemClickListenner;
    private Context context;
    private List<Object> Quizs;
    private String timeStamp;
    private int lastPosition = -1;
    private static final int ITEM_QUIZ_VIEW = 1;
    private static final int NATIVE_EXPRESS_AD_VIEW_TYPE = 0;

    public CustomAdapter(Context context, List<Object> Quizs, String timeStamp) {
        this.context = context;
        this.Quizs = Quizs;
        this.timeStamp = timeStamp;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {

            case ITEM_QUIZ_VIEW:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
                return new QuizViewHolder(itemView);

            case NATIVE_EXPRESS_AD_VIEW_TYPE:

            default:
                View nativeAdsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ads_native, parent, false);
                return new NativeAdsHolder(nativeAdsView);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {
            case ITEM_QUIZ_VIEW:
                Quiz quiz = (Quiz) Quizs.get(position);

                QuizViewHolder quizHolder = (QuizViewHolder) holder;

                quizHolder.description.setText(quiz.getTitle());
                Typeface adventProExl = Typeface.createFromAsset(context.getAssets(), "fonts/AdventPro-ExtraLight.ttf");
                quizHolder.description.setTypeface(adventProExl);
                Picasso.with(context)
                        .load(quiz.getImage())
                        .placeholder(null)
                        .resize(200, 200)
                        .centerInside()
                        .placeholder(R.drawable.image_holder)
                        .into(quizHolder.imageView);

                quizHolder.checked.setVisibility(View.GONE);
                quizHolder.badgeNew.setVisibility(View.GONE);

                DaoQuiz daoQuiz = new DaoQuiz(context);
                if (daoQuiz.isPassed(quiz.getId())) {
                    quizHolder.checked.setVisibility(View.VISIBLE);
                    quizHolder.badgeNew.setVisibility(View.GONE);
                } else {
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date parsedDate = dateFormat.parse(quiz.getPublishedDate().substring(0, 10));
                        Timestamp timeLocal = new java.sql.Timestamp(parsedDate.getTime());
                        if (timeStamp != null && timeLocal != null && (Integer.parseInt(timeStamp) - (int) (timeLocal.getTime() / 1000)) <= 604807) {
                            quizHolder.badgeNew.setVisibility(View.VISIBLE);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                setAnimation(holder.itemView, position);

                break;

            case NATIVE_EXPRESS_AD_VIEW_TYPE:

            default:

                NativeAdsHolder nativeAdsHolder = (NativeAdsHolder) holder;

                NativeExpressAdView adView = (NativeExpressAdView) Quizs.get(position);

                ViewGroup adCardView = (ViewGroup) nativeAdsHolder.itemView;

                if (adCardView.getChildCount() > 0) {
                    adCardView.removeAllViews();
                }
                if (adView.getParent() != null) {

                    Log.e("parentview removed", "true");
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                // Add the Native Express ad to the native express ad view.
                adCardView.addView(adView);
                Log.e("view added", "true");

                break;
        }

    }

    @Override
    public int getItemCount() {
        return Quizs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (Quizs.get(position) instanceof NativeExpressAdView) ? NATIVE_EXPRESS_AD_VIEW_TYPE
                : ITEM_QUIZ_VIEW;
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        if (holder.getItemViewType() == ITEM_QUIZ_VIEW)
            ((QuizViewHolder) holder).clearAnimation();

    }


    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void setItemClickListenner(ItemClickListenner itemClickListenner) {
        this.itemClickListenner = itemClickListenner;
    }

    public void deleteAdsNative() {

        if (Quizs != null) {
            for (int i = 0; i < Quizs.size(); i++) {
                if (Quizs.get(i) instanceof NativeExpressAdView)
                    Quizs.remove(i);
            }
            notifyDataSetChanged();
        }
    }


    public class QuizViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView description;
        private ImageView imageView;
        private RelativeLayout card;
        private ImageView checked;
        private ImageView badgeNew;

        public QuizViewHolder(View itemView) {
            super(itemView);
            card = (RelativeLayout) itemView.findViewById(R.id.card);
            description = (TextView) itemView.findViewById(R.id.title_quiz);
            imageView = (ImageView) itemView.findViewById((R.id.img_quiz));
            checked = (ImageView) itemView.findViewById((R.id.checked));
            badgeNew = (ImageView) itemView.findViewById((R.id.badgeNew));


            itemView.setOnClickListener(this);
        }

        public void clearAnimation() {
            card.clearAnimation();
        }


        @Override
        public void onClick(View v) {
            if (itemClickListenner != null) {
                itemClickListenner.onClick(v, getAdapterPosition());
            }
        }
    }

    public class NativeAdsHolder extends RecyclerView.ViewHolder {

        public NativeAdsHolder(View itemView) {
            super(itemView);
        }

    }
}

