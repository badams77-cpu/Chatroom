package Chatroom;

public interface RepliesHandle {

	
//	
    public RepliesHandle openHandler(String blog,String messageID);
// Thus the message have any replies	
	public boolean moreReplies(String messageID); // Does the step
// Next readable message repling to the article ID
	public String getNextMessageId(); // Next readable with same
// Return a ChatRoom Item
	public ChatroomDBItems.ChatMessage getMessage();
// Next writable message ID
	public String firstFreeMessageID(String messageID);
// Handlers must be closed
	void closeHandler();
	
}
