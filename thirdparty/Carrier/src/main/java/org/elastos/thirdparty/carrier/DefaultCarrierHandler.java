package org.elastos.thirdparty.carrier;

import org.elastos.carrier.AbstractCarrierHandler;
import org.elastos.carrier.Carrier;
import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.carrier.UserInfo;

import java.util.HashMap;
import java.util.List;

public class DefaultCarrierHandler extends AbstractCarrierHandler {
    @Override
    public void onConnection(Carrier carrier, ConnectionStatus status) {
        Logger.info("Carrier connection status: " + status);
        mConnectionStatus = status;

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

        if(mEventListener != null) {
            mEventListener.onConnection(carrier, status);
        }
    }

    @Override
    public void onFriendRequest(Carrier carrier, String userId, UserInfo info, String hello) {
        Logger.info("Carrier received friend request. peer UserId: " + userId);
        CarrierHelper.acceptFriend(userId, hello);

        if(mEventListener != null) {
            mEventListener.onFriendRequest(carrier, userId, info, hello);
        }
    }

    @Override
    public void onFriendAdded(Carrier carrier, FriendInfo info) {
        Logger.info("Carrier friend added. peer UserId: " + info.getUserId());

        if(mEventListener != null) {
            mEventListener.onFriendAdded(carrier, info);
        }
    }

    @Override
    public void onFriendConnection(Carrier carrier, String friendId, ConnectionStatus status) {
        Logger.info("Carrier friend connect. peer UserId: " + friendId + " status:" + status);
        mFriendConnectionStatus.put(friendId, status);

        if(status == ConnectionStatus.Connected) {
            CarrierHelper.setPeerUserId(friendId);
        } else {
            CarrierHelper.setPeerUserId(null);
        }

        if(mEventListener != null) {
            mEventListener.onFriendConnection(carrier, friendId, status);
        }
    }

    @Override
    public void onFriendMessage(Carrier carrier, String from, byte[] message) {
        Logger.info("Carrier receiver message from UserId: " + from
                + "\nmessage: " + new String(message));
        Logger.info("DefaultCarrierHandler.onFriendMessage() listener=" + mEventListener);

        if(mEventListener != null) {
            mEventListener.onFriendMessage(carrier, from, message);
        }
    }

    public void setEventListener(AbstractCarrierHandler listener) {
        Logger.info("DefaultCarrierHandler.setEventListener() listener=" + listener);
        mEventListener = listener;

        if(mEventListener != null) {
            if (mConnectionStatus != null) {
                mEventListener.onConnection(Carrier.getInstance(), mConnectionStatus);
            }

            for (HashMap.Entry<String, ConnectionStatus> entry : mFriendConnectionStatus.entrySet()) {
                mEventListener.onFriendConnection(Carrier.getInstance(), entry.getKey(), entry.getValue());
            }
        }
    }
    private AbstractCarrierHandler mEventListener = null;
    ConnectionStatus mConnectionStatus = null;
    HashMap<String, ConnectionStatus> mFriendConnectionStatus = new HashMap<>();
}

