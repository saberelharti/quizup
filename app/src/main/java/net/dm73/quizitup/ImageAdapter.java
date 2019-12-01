package net.dm73.quizitup;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dm73.quizitup.model.Category;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageAdapter extends BaseAdapter {

    private List<Category> CategoryList;
    private Context context;
    private LayoutInflater inflater;

    public ImageAdapter(Context context, List<Category> CategoryList) {
        this.context = context;
        this.CategoryList = CategoryList;
    }

    @Override
    public int getCount() {
        return CategoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return CategoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View gridView = convertView;

        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.grid_view, null);
        }

        TextView txt = (TextView) gridView.findViewById(R.id.grid_item_label);
        CircleImageView img = (CircleImageView) gridView.findViewById(R.id.grid_item_card);

        Typeface adventProExl = Typeface.createFromAsset(context.getAssets(), "fonts/AdventPro-ExtraLight.ttf");
        txt.setTypeface(adventProExl);

        Picasso.with(context)
                .load(CategoryList.get(position).getImage())
                .resize(250, 250)
                .placeholder(R.drawable.image_holder)
                .centerInside()
                .into(img);

        txt.setText(CategoryList.get(position).getName());


        return gridView;
    }


}