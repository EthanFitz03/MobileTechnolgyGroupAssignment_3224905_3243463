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
}
