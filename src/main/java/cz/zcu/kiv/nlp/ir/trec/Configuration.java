package cz.zcu.kiv.nlp.ir.trec;

public class Configuration {

    public static String getDateFormat() {
        return "dd.MM.yyyy";
    }

    public static int getMinTopKResults() {
        return 10;
    }

    public static int getMaxTopKResults() {
        return 100;
    }
}
