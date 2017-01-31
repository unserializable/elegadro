package org.elegadro.poc.l10n;

import org.araneaframework.framework.filter.StandardLocalizationFilterService;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Implementation of {@link org.araneaframework.framework.LocalizationContext} that provides
 * <i>Estonian language messages</i> from the l10n resources bundled inside the application.
 *
 * @author Taimo Peelo
 */
public class SMML10nFilterService extends StandardLocalizationFilterService {
    private static final L10nBundle_et BUNDLE_ET = new L10nBundle_et();

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        return BUNDLE_ET;
    }
}
