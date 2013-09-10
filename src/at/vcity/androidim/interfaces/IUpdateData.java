package at.vcity.androidim.interfaces;
import android.content.Context;
import at.vcity.androidim.types.FriendInfo;
import at.vcity.androidim.types.MessageInfo;


public interface IUpdateData {
	public void updateData(MessageInfo[] messages, FriendInfo[] friends, FriendInfo[] unApprovedFriends, String userKey);

}
