package com.example.mobiletechnolgygroupassignment;

public class SavedItem {
    public String id;
    public String filename;
    public String reader;
    public String text;
    public String imageUri;
    public long timestamp;

    public SavedItem() {
        //empty constructor for firebase
    }

    public SavedItem(String id, String filename, String reader, String text, String imageUri, long timestamp) {
     this.id = id;
     this.filename = filename;
     this.reader = reader;
     this.text = text;
     this.imageUri = imageUri;
     this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public String getText() {
        return  text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUri() {
        return  imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
