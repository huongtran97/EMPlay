package emplay.entertainment.emplay.models.common;

public class OriginModel {
    private final String countryCode;
    private final String name;
    private final String glyph;

    public OriginModel(String countryCode, String name, String glyph) {
        this.countryCode = countryCode;
        this.name = name;
        this.glyph = glyph;
    }

    public String getCountryCode() { return countryCode; }
    public String getName() { return name; }
    public String getGlyph() { return glyph; }
}
