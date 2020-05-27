package cz.zcu.kiv.nlp.ir.trec.preprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer. Will tokenize text. Expects lowercased text without accents.
 */
public class AdvancedTokenizer implements Tokenizer {

    public static final String defaultRegex = "(\\d+[.,](\\d+)?)|([a-zA-Z0-9]+[*]*[a-zA-Z0-9]*)|([\\p{L}\\d]+)|(<.*?>)|([\\p{Punct}])";
    public static final String wordsOnlyRegex = "([a-z][a-z0-9]*)";


    public List<String> tokenize(String text) {


        Pattern pattern = Pattern.compile(wordsOnlyRegex);
        Matcher matcher = pattern.matcher(text);
        List<String> words = new ArrayList<String>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String token = text.substring(start, end);
            words.add(token);
        }

        return words;
    }
}
