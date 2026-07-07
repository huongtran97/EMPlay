package emplay.entertainment.emplay;

import org.junit.Test;

import emplay.entertainment.emplay.tool.LanguageMapper;

import static org.junit.Assert.*;

public class LanguageMapperTest {

    @Test
    public void en_mapsToEnglish() {
        assertEquals("English", LanguageMapper.getLanguageName("en"));
    }

    @Test
    public void ja_mapsToJapanese() {
        assertEquals("Japanese", LanguageMapper.getLanguageName("ja"));
    }

    @Test
    public void es_mapsToSpanish() {
        assertEquals("Spanish", LanguageMapper.getLanguageName("es"));
    }

    @Test
    public void fr_mapsToFrench() {
        assertEquals("French", LanguageMapper.getLanguageName("fr"));
    }

    @Test
    public void vi_mapsToVietnamese() {
        assertEquals("Vietnamese", LanguageMapper.getLanguageName("vi"));
    }

    @Test
    public void zh_mapsToChinese() {
        assertEquals("Chinese", LanguageMapper.getLanguageName("zh"));
    }

    @Test
    public void de_mapsToGerman() {
        assertEquals("German", LanguageMapper.getLanguageName("de"));
    }

    @Test
    public void ko_mapsToKorean() {
        assertEquals("Korean", LanguageMapper.getLanguageName("ko"));
    }

    @Test
    public void it_mapsToItalian() {
        assertEquals("Italian", LanguageMapper.getLanguageName("it"));
    }

    @Test
    public void nl_mapsToDutch() {
        assertEquals("Dutch", LanguageMapper.getLanguageName("nl"));
    }

    @Test
    public void ru_mapsToRussian() {
        assertEquals("Russian", LanguageMapper.getLanguageName("ru"));
    }

    @Test
    public void pt_mapsToPortuguese() {
        assertEquals("Portuguese", LanguageMapper.getLanguageName("pt"));
    }

    @Test
    public void unknownCode_returnsUnknown() {
        assertEquals("Unknown", LanguageMapper.getLanguageName("xx"));
    }

    @Test
    public void emptyCode_returnsUnknown() {
        assertEquals("Unknown", LanguageMapper.getLanguageName(""));
    }

    @Test
    public void uppercaseCode_returnsUnknown() {
        // Map keys are lowercase — uppercase should not match
        assertEquals("Unknown", LanguageMapper.getLanguageName("EN"));
    }
}