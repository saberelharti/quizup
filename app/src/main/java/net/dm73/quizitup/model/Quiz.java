package net.dm73.quizitup.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Quiz implements Parcelable {

    private String id;
    private String type;
    private String hash;
    private String title;
    private String categorie;
    private String image;
    private String description;
    private String publishedDate;
    private boolean newuiz = false;

    public Quiz(String id, String type, String hash, String title, String categorie, String image, String description, String publishedDate) {
        this.id= id;
        this.type = type;
        this.hash = hash;
        this.title = title;
        this.categorie = categorie;
        this.image = image;
        this.description = description;
        this.publishedDate = publishedDate;
    }


    //getters and setters

    public String getType() {
        return type;
    }

    public String getHash() {
        return hash;
    }

    public String getTitle() {
        return title;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        image = image;
    }

    public void setDescrition(String descrition) {
        this.description = descrition;
    }

    public String getDescrition() {
        return description;
    }

    public void setNewuiz(boolean newuiz) {
        this.newuiz = newuiz;
    }

    public boolean isNewuiz() {

        return newuiz;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    protected Quiz(Parcel in) {
        id = in.readString();
        type = in.readString();
        hash = in.readString();
        title = in.readString();
        categorie = in.readString();
        image = in.readString();
        description = in.readString();
        publishedDate = in.readString();
        newuiz = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(hash);
        dest.writeString(title);
        dest.writeString(categorie);
        dest.writeString(image);
        dest.writeString(description);
        dest.writeString(publishedDate);
        dest.writeByte((byte) (newuiz ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Quiz> CREATOR = new Parcelable.Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel in) {
            return new Quiz(in);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };
}