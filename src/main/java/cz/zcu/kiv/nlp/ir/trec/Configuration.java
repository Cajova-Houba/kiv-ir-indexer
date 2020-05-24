package cz.zcu.kiv.nlp.ir.trec;

public class Configuration {

    public static String getDateFormat() {
        return "dd.MM.yyyy HH:mm";
    }

    public static int getMinTopKResults() {
        return 10;
    }

    public static int getMaxTopKResults() {
        return 100;
    }

    public static int getMaxProgress() {return 100;}
}
