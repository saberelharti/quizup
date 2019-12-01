package net.dm73.quizitup.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {

    private int Id;
    private String name;
    private String image;

    public Category(int id, String name, String image) {
        this.Id = id;
        this.name = name;
        this.image = image;
    }


    public int getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        Id = id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    protected Category(Parcel in) {
        Id = in.readInt();
        name = in.readString();
    }

    public String getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}