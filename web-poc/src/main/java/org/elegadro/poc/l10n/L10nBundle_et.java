package org.elegadro.poc.l10n;

import org.araneaframework.http.support.FallbackResourceBundle;
import org.araneaframework.http.support.StringResourceBundle;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Taimo Peelo
 */
public class L10nBundle_et extends FallbackResourceBundle {
    public L10nBundle_et() {
        setLocale(new Locale("et"));

        addResourceBundle(new StringResourceBundle());
        addResourceBundle(ResourceBundle.getBundle("l10n/elegadro", getLocale()));
        addResourceBundle(ResourceBundle.getBundle("resource/uilib", getLocale()));
    }
}
