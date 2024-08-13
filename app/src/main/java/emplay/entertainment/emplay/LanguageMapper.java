package emplay.entertainment.emplay;

import android.os.Build.VERSION_CODES;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

public class LanguageMapper {

    private static final Map<String, String> languageMap = new HashMap<>();

    static {
        languageMap.put("en", "English");
        languageMap.put("ja", "Japanese");
        languageMap.put("es", "Spanish");
        languageMap.put("fr", "French");
        languageMap.put("vi", "Vietnamese");
        languageMap.put("zh", "Chinese");
        languageMap.put("de", "German");
        languageMap.put("ko", "Korean");
        languageMap.put("it", "Italian");
        languageMap.put("nl", "Dutch");
        languageMap.put("ru", "Russian");
        languageMap.put("cs", "Czech");
        languageMap.put("ro", "Romanian");
        languageMap.put("sv", "Swedish");
        languageMap.put("hu", "Hungarian");
        languageMap.put("bg", "Bulgarian");
        languageMap.put("da", "Danish");
        languageMap.put("br", "Brazilian");
        languageMap.put("pt", "Portuguese");

    }

    /**
     * Gets the full language name for the given language code.
     *
     * @param code The language code.
     * @return The full language name, or "Unknown" if the code is not found.
     */
    @RequiresApi(api = VERSION_CODES.N)
    public static String getLanguageName(String code) {
        return languageMap.getOrDefault(code, "Unknown");
    }
}
