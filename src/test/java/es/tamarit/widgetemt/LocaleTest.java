package es.tamarit.widgetemt;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;

import es.tamarit.widgetemt.core.LanguageManager;

public class LocaleTest {
    
    @Test
    public void getLocaleFromString() {
        
        String str = "es-ES";
        Locale locale1 = Locale.forLanguageTag(str);
        
        Locale locale2 = new Locale(locale1.getLanguage(), locale1.getCountry());
        
        Assert.assertEquals("es", locale1.getLanguage());
        Assert.assertEquals("ES", locale1.getCountry());
        
        Assert.assertEquals(locale1.getLanguage(), locale2.getLanguage());
        Assert.assertEquals(locale1.getCountry(), locale2.getCountry());
    }
    
    @Test
    public void getLocaleFromStringWithoutCountry() {
        
        String str = "es";
        Locale locale1 = Locale.forLanguageTag(str);
        
        Locale locale2 = new Locale(locale1.getLanguage(), locale1.getCountry());
        
        Assert.assertEquals("es", locale1.getLanguage());
        Assert.assertNotEquals("ES", locale1.getCountry());
        
        Assert.assertEquals(locale1.getLanguage(), locale2.getLanguage());
        Assert.assertEquals(locale1.getCountry(), locale2.getCountry());
    }
    
    @Test
    public void languageManager() {
        
        String srt = "es-ES";
        Locale defaultLocale = Locale.forLanguageTag(srt);
        
        LanguageManager manager = new LanguageManager(defaultLocale);
        
        ResourceBundle bundle = manager.getResourceBundle();
        Assert.assertNotNull(bundle);
        
        Locale locale = manager.getLocale();
        Assert.assertNotNull(locale);
        
        Assert.assertEquals(bundle.getLocale(), locale);
        Assert.assertEquals(locale, defaultLocale);
    }
    
    @Test
    public void getStringFromBundle() {
        
        String srt = "es-ES";
        Locale defaultLocale = Locale.forLanguageTag(srt);
        
        LanguageManager manager = new LanguageManager(defaultLocale);
        
        ResourceBundle bundle = manager.getResourceBundle();
        Assert.assertNotNull(bundle);
        
        String string1 = bundle.getString("application.name");
        Assert.assertEquals("EMT-Widget-es_ES", string1);
    }
    
    @Test
    public void getStringChangingLocale() {
        
        LanguageManager manager = new LanguageManager(new Locale("es", "ES"));
        ResourceBundle bundle = manager.getResourceBundle();
        
        String string1 = bundle.getString("application.name");
        Assert.assertEquals("EMT-Widget-es_ES", string1);
        
        manager = new LanguageManager(new Locale("en", "EN"));
        bundle = manager.getResourceBundle();
        String string2 = bundle.getString("application.name");
        Assert.assertEquals("EMT-Widget-en", string2);
        
        manager = new LanguageManager(new Locale("en"));
        bundle = manager.getResourceBundle();
        String string3 = bundle.getString("application.name");
        Assert.assertEquals("EMT-Widget-en", string3);
        
        manager = new LanguageManager(new Locale("ca", "ES"));
        bundle = manager.getResourceBundle();
        String string4 = bundle.getString("application.name");
        Assert.assertEquals("EMT-Widget", string4);
        Assert.assertNotEquals("EMT-Widget-ca_ES", string4);
    }
}
