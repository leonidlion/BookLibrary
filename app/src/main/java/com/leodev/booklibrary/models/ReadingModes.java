
package com.leodev.booklibrary.models;


public class ReadingModes {

    private Boolean text;
    private Boolean image;

    /**
     * 
     * @return
     *     The text
     */
    public Boolean getText() {
        return text;
    }

    /**
     * 
     * @param text
     *     The text
     */
    public void setText(Boolean text) {
        this.text = text;
    }

    /**
     * 
     * @return
     *     The image
     */
    public Boolean getImage() {
        return image;
    }

    /**
     * 
     * @param image
     *     The image
     */
    public void setImage(Boolean image) {
        this.image = image;
    }

}
