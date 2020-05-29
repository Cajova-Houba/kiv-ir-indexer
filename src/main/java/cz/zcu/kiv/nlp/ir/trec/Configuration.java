package cz.zcu.kiv.nlp.ir.trec;

public class Configuration {

    public static String getDateFormat() {
        return "dd.MM.yyyy HH:mm";
    }

    public static int getMinTopKResults() {
        return 1;
    }

    public static int getMaxTopKResults() {
        return Integer.MAX_VALUE -1;
    }

    public static int getMaxProgress() {return 100;}
}
