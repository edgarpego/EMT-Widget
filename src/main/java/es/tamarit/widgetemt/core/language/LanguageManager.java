package es.tamarit.widgetemt.core.language;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    private static final String URL_LANGUAGE_BASE_DIR = "languages.strings";
    private ResourceBundle resourceBundle;

    public LanguageManager(Locale locale) {

        this.resourceBundle = ResourceBundle.getBundle(URL_LANGUAGE_BASE_DIR, locale, new Utf8Controller());
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public Locale getLocale() {
        return resourceBundle.getLocale();
    }
}
