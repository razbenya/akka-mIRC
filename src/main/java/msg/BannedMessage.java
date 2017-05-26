package msg;


import scala.Serializable;

/**
 * Created by RazB on 18/05/2017.
 */
public class BannedMessage implements Serializable {
    private String _bannedBy;
    private String _channelName;

    public BannedMessage(String bannedBy,String channelName) {
        _bannedBy = bannedBy;
        _channelName = channelName;
    }

    public String getBanner(){
        return _bannedBy;
    }

    public String getChannelName() {
        return _channelName;
    }
}