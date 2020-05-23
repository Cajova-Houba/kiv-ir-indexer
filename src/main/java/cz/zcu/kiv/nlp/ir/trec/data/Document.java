package cz.zcu.kiv.nlp.ir.trec.data;

import java.util.Date;

/**
 * Created by Tigi on 8.1.2015.
 *
 * Rozhraní reprezentuje dokument, který je možné indexovat a vyhledávat.
 *
 * Implementujte toto rozhranní.
 *
 * Pokud potřebujete můžete do rozhranní přidat metody, ale signaturu stávajících metod neměnte.
 *
 */
public interface Document {

    /**
     * Text dokumentu
     * @return text
     */
    String getText();

    /**
     * Unikátní id dokumentu
     * @return id dokumentu
     */
    String getId();

    /**
     * Titulek dokumentu
     * @return titulek dokumentu
     */
    String getTitle();

    /**
     * Datum přidání dokumentu (tj. např. indexace nebo stažení nebo publikování
     *
     * @return datum vztažené k dokumentu
     */
    Date getDate();

    /**
     * Returns the value of Reddit score. 0 by default.
     * @return Reddit score.
     */
    int getRedditScore();

    /**
     * Returns the Reddit username of the creator of this document.
     * @return Reddit username.
     */
    String getUsername();

}
