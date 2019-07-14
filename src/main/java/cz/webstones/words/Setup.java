/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.webstones.words;

/**
 *
 * @author jaroslav_b
 */
public class Setup {
    private String dataDir;
    private String mp3Dir;
    private String directoryFile;
    private String categoryFile;
    private String dictionarySeparator;
    private String dictionaryDateFormat;
    private String language;

    
    /**
     * @return the dataDir
     */
    public String getDataDir() {
        return dataDir;
    }

    /**
     * @param dataDir the dataDir to set
     */
    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    /**
     * @return the mp3Dir
     */
    public String getMp3Dir() {
        return mp3Dir;
    }

    /**
     * @param mp3Dir the mp3Dir to set
     */
    public void setMp3Dir(String mp3Dir) {
        this.mp3Dir = mp3Dir;
    }

    /**
     * @return the directoryFile
     */
    public String getDirectoryFile() {
        return directoryFile;
    }

    /**
     * @param directoryFile the directoryFile to set
     */
    public void setDirectoryFile(String directoryFile) {
        this.directoryFile = directoryFile;
    }

    /**
     * @return the categoryFile
     */
    public String getCategoryFile() {
        return categoryFile;
    }

    /**
     * @param categoryFile the categoryFile to set
     */
    public void setCategoryFile(String categoryFile) {
        this.categoryFile = categoryFile;
    }

    /**
     * @return the dictionarySeparator
     */
    public String getDictionarySeparator() {
        return dictionarySeparator;
    }

    /**
     * @param dictionarySeparator the dictionarySeparator to set
     */
    public void setDictionarySeparator(String dictionarySeparator) {
        this.dictionarySeparator = dictionarySeparator;
    }
    
    public String getFullDictionaryFilePath() {
        String result = String.format("%s/%s", getDataDir(), getDirectoryFile());
        try {
            Service.checkOrCreateDirectory(getDataDir());
            Service.checkOrCreateFile(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public String getFullCategoryFilePath() {
        String result = String.format("%s/%s", getDataDir(), getCategoryFile());
        try {
            Service.checkOrCreateDirectory(getDataDir());
            Service.checkOrCreateFile(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public String getFullMp3Path() {
        String result = String.format("%s/%s", getDataDir(), getMp3Dir());
        try {
            Service.checkOrCreateDirectory(getDataDir());
            Service.checkOrCreateDirectory(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @return the dictionaryDateFormat
     */
    public String getDictionaryDateFormat() {
        return dictionaryDateFormat;
    }

    /**
     * @param dictionaryDateFormat the dictionaryDateFormat to set
     */
    public void setDictionaryDateFormat(String dictionaryDateFormat) {
        this.dictionaryDateFormat = dictionaryDateFormat;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

}
