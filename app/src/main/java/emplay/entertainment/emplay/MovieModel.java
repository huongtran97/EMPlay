package emplay.entertainment.emplay;

public class MovieModel {

    String id;
    String name;
    String vote;
    String img;


    public MovieModel(String id, String name, String vote, String img) {
        this.id = id;
        this.name = name;
        this.vote = vote;
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVote() {
        return vote;
    }

    public void setVote(String vote) {
        this.vote = vote;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public MovieModel() {




    }
}
