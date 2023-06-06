package emplay.entertainment.emplay;

public class MovieSlide {

    private int Image;
    private String Title;
    private String Description;

    public MovieSlide(int image, String title, String description) {
        Image = image;
        Title = title;
        Description = description;
    }

    public int getImage() {
        return Image;
    }

    public String getTitle() {
        return Title;
    }

    public String getDescription() {
        return Description;
    }

    public void setImage(int image) {
        Image = image;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
