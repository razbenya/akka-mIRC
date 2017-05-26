package msg;

import scala.Serializable;

/**
 * Created by RazB on 19/05/2017.
 */
public class LoginMessage implements Serializable {
    private String _nickName;

    public LoginMessage(String _nickName) {
        this._nickName = _nickName;
    }

    public String toString(){
        return _nickName;
    }

}
