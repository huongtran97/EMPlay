package emplay.entertainment.emplay.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PersonDetailsResponse {

    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("biography")
    private String biography;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("place_of_birth")
    private String placeOfBirth;
    @SerializedName("profile_path")
    private String profilePath;
    @SerializedName("known_for_department")
    private String knownForDepartment;
    @SerializedName("popularity")
    private double popularity;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBiography() { return biography; }
    public String getBirthday() { return birthday; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public String getProfilePath() { return profilePath; }
    public String getKnownForDepartment() { return knownForDepartment; }
    public double getPopularity() { return popularity; }
}