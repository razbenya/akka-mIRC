package msg;

import scala.Serializable;

/**
 * Created by RazB on 24/05/2017.
 */
public class TitleMessage implements Serializable {
    private String title;

    public TitleMessage(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
