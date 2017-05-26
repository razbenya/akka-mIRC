package msg;

import actors.LocalUser;
import scala.Serializable;

/**
 * Created by RazB on 25/05/2017.
 */
public class ChangeModeMessage implements Serializable {
    private String _channelName;
    private LocalUser.Modes _mod;
    private int _premote;

    public ChangeModeMessage(String channelName, LocalUser.Modes mod,int premote){
        this._channelName = channelName;
        this._mod = mod;
        this._premote = premote;
    }

    public String getChannelName() {
        return _channelName;
    }

    public LocalUser.Modes getMod() {
        return _mod;
    }

    public int getPremoteFlag() {
        return _premote;
    }
}
