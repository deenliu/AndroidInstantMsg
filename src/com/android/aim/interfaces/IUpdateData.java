package com.android.aim.interfaces;
import com.android.aim.types.FriendInfo;
import com.android.aim.types.MessageInfo;


public interface IUpdateData {
	public void updateData(MessageInfo[] messages, FriendInfo[] friends, FriendInfo[] unApprovedFriends, String userKey);

}
