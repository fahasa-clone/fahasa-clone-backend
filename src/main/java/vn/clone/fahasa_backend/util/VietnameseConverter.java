package vn.clone.fahasa_backend.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VietnameseConverter {

    /**
     * Converts a Vietnamese string to a lowercase, ASCII-compatible "slug" string.
     * This method removes diacritical marks and handles special characters.
     *
     * @param input The Vietnamese string to be converted.
     * @return A lowercase ASCII string.
     */
    public static String convertVietnameseToAscii(String input) {
        // Return an empty string if the input is null or empty to avoid errors.
        if (input == null || input.isEmpty()) {
            return "";
        }

        // 1. Normalize the string to NFD (Canonical Decomposition).
        // This separates the base characters from their diacritical marks.
        // For example, 'â' becomes 'a' + '^'.
        String normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);

        // 2. Use a regular expression to remove the diacritical marks.
        // The \p{InCombiningDiacriticalMarks} block matches all combining marks.
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutAccents = pattern.matcher(normalizedString)
                                       .replaceAll("");

        // 3. Specifically handle the Vietnamese letter 'đ' and 'Đ'.
        // The Normalizer does not decompose 'đ' into 'd', so it must be handled manually.
        String result = withoutAccents.replace('đ', 'd')
                                      .replace('Đ', 'D');

        // 4. Convert the final string to lowercase.
        return result.toLowerCase();
    }

    /**
     * Normalizes a raw name string by trimming leading/trailing whitespace and
     * normalizing internal whitespace to single spaces.
     *
     * @param name The raw, unformalized name string.
     * @return The formalized name string.
     */
    public static String normalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.trim()
                   .replaceAll("\\s+", " ");
    }

    /**
     * Generates a URL-safe slug from a normalized name string.
     * <p>
     * The string is first converted to lowercase and non-ASCII characters are typically
     * transliterated to their closest ASCII equivalents. All internal separators
     * (spaces and hyphen-surrounded spaces) are then consolidated into single hyphens.
     *
     * @param name The input name string (expected to be formalized).
     * @return The URL-safe slug string.
     */
    public static String convertNameToSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return convertVietnameseToAscii(name).replaceAll("\\s-\\s", "-")
                                             .replaceAll("\\s", "-");
    }

    /**
     * Main method to demonstrate the function's usage.
     */
    public static void main(String[] args) {
        System.out.println(normalizeName(" Toi la ai    ha  "));

        String vietnameseSentence = "Chào bạn, bạn có khỏe không? Đây là Tiếng Việt có dấu.";
        String asciiSentence = convertVietnameseToAscii(vietnameseSentence);

        System.out.println("Original: " + vietnameseSentence);
        System.out.println("Converted: " + asciiSentence);

        String expected = "chao ban, ban co khoe khong? day la tieng viet co dau.";
        System.out.println("Expected:   " + expected);
        System.out.println("Matches Expected: " + asciiSentence.equals(expected));

        System.out.println("\n--- Another Example ---");
        String bookTitle = "Văn Học - Tiểu Thuyết";
        String slug = convertVietnameseToAscii(bookTitle);
        System.out.println("Original Title: " + bookTitle);
        // To make a URL-friendly slug, you might also replace spaces and other characters.
        String urlSlug = slug.replace(" ", "-").replaceAll("[^a-z0-9-]", "");
        System.out.println("URL Slug: " + urlSlug);
    }
}

