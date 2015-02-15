package co.mobilemakers.albumlister;

/**
 * Album info class
 *
 * Created by ariel.cattaneo on 13/02/2015.
 */
public class Album {
    private String title;
    private String date;

    public String getDate() {
        return (date.isEmpty() ? "Unknown date" : date);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Album: " + title + " (" + date + ")";
    }
}
