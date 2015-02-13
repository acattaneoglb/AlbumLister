package co.mobilemakers.albumlister;

/**
 * Created by ariel.cattaneo on 13/02/2015.
 */
public class Album {
    private String title;
    private String length;

    public String getLength() {
        return (length.isEmpty() ? "Unknown length" : length);
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Album: " + title + " (" + length + ")";
    }
}
