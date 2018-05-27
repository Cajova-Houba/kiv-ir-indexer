package cz.zcu.kiv.nlp.trec.core;

import cz.zcu.kiv.nlp.ir.trec.core.ConfigurableSimilarityCalculator;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConfigurableSimilarityCalculatorTest {

    /**
     * Try all possible configurations.
     */
    @Test
    public void testCheckConfiguration() {
        int confCount =
                ConfigurableSimilarityCalculator.FIRST_LETTERS.length *
                ConfigurableSimilarityCalculator.SECOND_LETTERS.length *
                ConfigurableSimilarityCalculator.THIRD_LETTERS.length;
        String[] confs = new String[confCount];

        int cCounter = 0;
        for(char c1 : ConfigurableSimilarityCalculator.FIRST_LETTERS) {
             for (char c2 : ConfigurableSimilarityCalculator.SECOND_LETTERS) {
                 for (char c3 : ConfigurableSimilarityCalculator.THIRD_LETTERS) {
                     confs[cCounter] = new String(new char[] {c1,c2,c3});
                 }
             }
        }

        for(String ddd : confs) {
            for (String qqq : confs) {
                String conf = ddd+"."+qqq;
                assertTrue("Configuration check failed for configuration "+conf, ConfigurableSimilarityCalculator.checkConfiguration(conf));
            }
        }
    }
}
