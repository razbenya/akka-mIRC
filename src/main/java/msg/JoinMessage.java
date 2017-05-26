package msg;

import scala.Serializable;

/**
 * Created by RazB on 19/05/2017.
 */
public class JoinMessage implements Serializable {
    private String _channelName;
    //private String _name;


    public JoinMessage(String _channelName){//, String name) {
        this._channelName = _channelName;
        //_name = name;
    }

   /* public String getName(){
        return _name;
    }*/

    @Override
    public String toString() {
        return _channelName;
    }
}
