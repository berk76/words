/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

/**
 *
 * @author jaroslav_b
 */
public class WordDto {
    private String cz;
    private String en;
    private int order;

    /**
     * @return the cz
     */
    public String getCz() {
        return cz;
    }

    /**
     * @param cz the cz to set
     */
    public void setCz(String cz) {
        this.cz = cz;
    }

    /**
     * @return the en
     */
    public String getEn() {
        return en;
    }

    /**
     * @param en the en to set
     */
    public void setEn(String en) {
        this.en = en;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }
}
