package com.dtp.cosmemgt.core.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {
    
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        String slug = input.replaceAll("Đ", "D").replaceAll("đ", "d");
        
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(slug).replaceAll("");
        
        slug = slug.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        
        slug = slug.replaceAll("^-+|-+$", "");
        
        return slug;
    }
}