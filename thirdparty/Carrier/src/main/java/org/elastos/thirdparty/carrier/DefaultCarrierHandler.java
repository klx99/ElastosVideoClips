package org.elastos.thirdparty.carrier;

import org.elastos.carrier.AbstractCarrierHandler;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.carrier.UserInfo;

import java.util.List;

public class DefaultCarrierHandler extends AbstractCarrierHandler {
    @Override
    public void onConnection(Carrier carrier, ConnectionStatus status) {
        Logger.info("Carrier connection status: " + status);

        if(status == ConnectionStatus.Connected) {
            String msg = "Friend List:";
            List<FriendInfo> friendList = CarrierHelper.getFriendList();
            if(friendList != null) {
                for(FriendInfo info: friendList) {
                    msg += "\n  " + info.getUserId();
                }
            }
            Logger.info(msg);
        }

        if(eventListener != null) {
            eventListener.onConnection(carrier, status);
        }
    }

    @Override
    public void onFriendRequest(Carrier carrier, String userId, UserInfo info, String hello) {
        Logger.info("Carrier received friend request. peer UserId: " + userId);
        CarrierHelper.acceptFriend(userId, hello);

        if(eventListener != null) {
            eventListener.onFriendRequest(carrier, userId, info, hello);
        }
    }

    @Override
    public void onFriendAdded(Carrier carrier, FriendInfo info) {
        Logger.info("Carrier friend added. peer UserId: " + info.getUserId());

        if(eventListener != null) {
            eventListener.onFriendAdded(carrier, info);
        }
    }

    @Override
    public void onFriendConnection(Carrier carrier, String friendId, ConnectionStatus status) {
        Logger.info("Carrier friend connect. peer UserId: " + friendId + " status:" + status);
        if(status == ConnectionStatus.Connected) {
            CarrierHelper.setPeerUserId(friendId);
        } else {
            CarrierHelper.setPeerUserId(null);
        }

        if(eventListener != null) {
            eventListener.onFriendConnection(carrier, friendId, status);
        }
    }

    @Override
    public void onFriendMessage(Carrier carrier, String from, byte[] message) {
        Logger.info("Carrier receiver message from UserId: " + from
                + "\nmessage: " + new String(message));

        if(eventListener != null) {
            eventListener.onFriendMessage(carrier, from, message);
        }
    }

    public void setEventListener(AbstractCarrierHandler listener) {
        eventListener = listener;
    }
    private AbstractCarrierHandler eventListener = null;
}

