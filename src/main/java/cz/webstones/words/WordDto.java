/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.util.Date;

/**
 *
 * @author jaroslav_b
 */
public class WordDto {
    private String cz;
    private String en;
    private int order;
    private int wrongHits;
    private Date lastWrongHit;
    private int goodHits;
    private Date lastGoodHit;
    private int originalOrder;

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

    /**
     * @return the lastWrongHit
     */
    public Date getLastWrongHit() {
        return lastWrongHit;
    }

    /**
     * @param lastWrongHit the lastWrongHit to set
     */
    public void setLastWrongHit(Date lastWrongHit) {
        this.lastWrongHit = lastWrongHit;
    }

    /**
     * @param wrongHits the wrongHits to set
     */
    public void setWrongHits(int wrongHits) {
        this.wrongHits = wrongHits;
    }

    /**
     * @return the goodHits
     */
    public int getGoodHits() {
        return goodHits;
    }

    /**
     * @param goodHits the goodHits to set
     */
    public void setGoodHits(int goodHits) {
        this.goodHits = goodHits;
    }

    /**
     * @return the lastGoodHit
     */
    public Date getLastGoodHit() {
        return lastGoodHit;
    }

    /**
     * @param lastGoodHit the lastGoodHit to set
     */
    public void setLastGoodHit(Date lastGoodHit) {
        this.lastGoodHit = lastGoodHit;
    }

    /**
     * @return the wrongHits
     */
    public int getWrongHits() {
        return wrongHits;
    }

    /**
     * @return the originalOrder
     */
    public int getOriginalOrder() {
        return originalOrder;
    }

    /**
     * @param originalOrder the originalOrder to set
     */
    public void setOriginalOrder(int originalOrder) {
        this.originalOrder = originalOrder;
    }
}
