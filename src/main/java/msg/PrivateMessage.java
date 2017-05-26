package msg;

import scala.Serializable;

/**
 * Created by RazB on 19/05/2017.
 */
public class PrivateMessage implements Serializable {
    private String _message;



    public PrivateMessage(String _message) {
        this._message = _message;
    }

    public String toString(){
        return _message;
    }
}
