package cz.zcu.kiv.nlp.ir.trec.preprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer. Will convert input text to lover case and remove all accents.
 */
public class AdvancedTokenizer implements Tokenizer {

    public static final String defaultRegex = "(\\d+[.,](\\d+)?)|([a-zA-Z0-9]+[*]*[a-zA-Z0-9]*)|([\\p{L}\\d]+)|(<.*?>)|([\\p{Punct}])";
    public static final String wordsOnlyRegex = "([a-z][a-z0-9]*)";
    public static final String withDiacritics = "áÁčČďĎéÉěĚíÍňŇóÓřŘšŠťŤúÚůŮýÝžŽĆć";
    public static final String withoutDiacritics = "aAcCdDeEeEiInNoOrRsStTuUuUyYzZCc";


    public String[] tokenize(String text) {
        // lower case, remove accents
        text = text.toLowerCase();
        text = removeAccents(text);


        Pattern pattern = Pattern.compile(wordsOnlyRegex);
        Matcher matcher = pattern.matcher(text);
        List<String> words = new ArrayList<String>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String token = text.substring(start, end);
            words.add(token);
        }

        return words.toArray(new String[0]);
    }

    private String removeAccents(String text) {
        for (int i = 0; i < withDiacritics.length(); i++) {
            text = text.replaceAll("" + withDiacritics.charAt(i), "" + withoutDiacritics.charAt(i));
        }
        return text;
    }
}
