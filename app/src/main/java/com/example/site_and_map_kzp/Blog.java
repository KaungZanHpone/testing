package com.example.site_and_map_kzp;

public class Blog {
    private String title;
    private int image;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Blog(String title, int image, String url){
        this.title=title;
        this.image=image;
        this.url=url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
