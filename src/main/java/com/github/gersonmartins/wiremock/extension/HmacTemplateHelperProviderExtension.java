package com.github.gersonmartins.wiremock.extension;

import com.github.jknack.handlebars.Helper;
import com.github.tomakehurst.wiremock.extension.TemplateHelperProviderExtension;

import java.util.HashMap;
import java.util.Map;

public class HmacTemplateHelperProviderExtension implements TemplateHelperProviderExtension {

    @Override
    public String getName() {
        return "hmac-template-helper";
    }

    @Override
    public Map<String, Helper<?>> provideTemplateHelpers() {
        Map<String, Helper<?>> helpers = new HashMap<>();
        helpers.put("hmac", new HmacHelper());
        return helpers;
    }
}
