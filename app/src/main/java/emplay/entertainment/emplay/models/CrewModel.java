package emplay.entertainment.emplay.models;

public class CrewModel {
    private int id;
    private String name;
    private String profile_path;
    private String job;
    private String department;
    private String known_for_department;
    private boolean adult;
    private int gender;
    private double popularity;
    private String credit_id;

    public CrewModel(int id, String name, String profile_path) {
        this.id = id;
        this.name = name;
        this.profile_path = profile_path;
        this.job = job;
        this.department = department;
        this.known_for_department = known_for_department;
        this.adult = adult;
        this.gender = gender;
        this.popularity = popularity;
        this.credit_id = credit_id;
    }

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

    public String getProfile_path() {
        return profile_path;
    }

    public void setProfile_path(String profile_path) {
        this.profile_path = profile_path;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getKnown_for_department() {
        return known_for_department;
    }

    public void setKnown_for_department(String known_for_department) {
        this.known_for_department = known_for_department;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getCredit_id() {
        return credit_id;
    }

    public void setCredit_id(String credit_id) {
        this.credit_id = credit_id;
    }


}



