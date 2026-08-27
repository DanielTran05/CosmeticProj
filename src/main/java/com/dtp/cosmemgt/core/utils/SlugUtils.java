package com.dtp.cosmemgt.core.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {
    
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        
        String slug = input.replaceAll("Đ", "D").replaceAll("đ", "d");
        
        // 2. Tách dấu ra khỏi chữ cái (Normalizer.Form.NFD)
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        
        // 3. Dùng Regex xóa bỏ các dấu (Combining Diacritical Marks)
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(slug).replaceAll("");
        
        // 4. Chuyển thành chữ thường và thay thế mọi ký tự không phải chữ/số thành dấu gạch ngang (-)
        slug = slug.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        
        // 5. Cắt bỏ dấu gạch ngang bị thừa ở đầu và cuối chuỗi (nếu có)
        slug = slug.replaceAll("^-+|-+$", "");
        
        return slug;
    }
}