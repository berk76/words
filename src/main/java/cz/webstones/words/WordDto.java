/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

import java.io.File;
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
    private String category;

    
    public String getMp3FilenameEn() {
        return String.format("%s" + File.separator + "%s.mp3", Service.getSetup(false).getFullMp3Path(), removeBadChars(this.getEn()));
    }
    
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
    
    public int getLastWrongHitInMinutes() {
        int result;
        if (lastWrongHit == null) {
            result = 365 * 24 * 60;
        } else {
            result = (int) ((new Date().getTime() - lastWrongHit.getTime()) / 60);
        }
        return result;
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
    
    public void incWrongHits() {
        this.wrongHits++;
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
    
    public void incGoodHits() {
        this.goodHits++;
    }

    /**
     * @return the lastGoodHit
     */
    public Date getLastGoodHit() {
        return lastGoodHit;
    }
    
    public int getLastGoodHitInMinutes() {
        int result;
        if (lastGoodHit == null) {
            result = 365 * 24 * 60;
        } else {
            result = (int) ((new Date().getTime() - lastGoodHit.getTime()) / 60);
        }
        return result;
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
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    private String removeBadChars(String w) {
        return w.replaceAll("\\?", "").replaceAll("\\.", "").replaceAll("'", "").replaceAll(",", "").replaceAll("\"", "");
    }
}
