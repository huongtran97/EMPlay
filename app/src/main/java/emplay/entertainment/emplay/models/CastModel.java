package emplay.entertainment.emplay.models;

public class CastModel {

    private int id;
    private String name;
    private String profilePath;
    private String character;

    public CastModel(int id, String name, String profilePath, String character) {
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
        this.character = character;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getProfilePath() {
        return profilePath;
    }
    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
    public String getCharacter() {
        return character;
    }
    public void setCharacter(String character) {
        this.character = character;
    }

}
