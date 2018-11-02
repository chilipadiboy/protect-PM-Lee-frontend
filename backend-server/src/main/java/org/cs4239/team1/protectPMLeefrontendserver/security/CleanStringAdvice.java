package org.cs4239.team1.protectPMLeefrontendserver.security;

import java.beans.PropertyEditorSupport;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class CleanStringAdvice {
    static public class StringCleaner extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) {
            if (text == null) {
                setValue(null);
            } else {
                String safe = Jsoup.clean(text, Whitelist.simpleText());
                setValue(safe);
            }
        }

    }

    @InitBinder
    public void bindStringCleaner(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(String.class, new StringCleaner());
    }
}
