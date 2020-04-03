package Chatroom;

import com.sleepycat.bind.EntityBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import java.io.File;
import java.nio.charset.*;

import ChatroomDBItems.*;

public class RepliesHandler implements RepliesHandle {

    DbAccess myDab = null;	
    Database myDatabase = null;
    Cursor myCursor = null;
    DatabaseEntry theKey;
    DatabaseEntry theMessage;
    String nextMessageKey;
    String messageKey;
    LockMode lockMode;
    String myBlog;
    

    private RepliesHandler(){
    	lockMode = LockMode.DEFAULT;
    }

    public void closeHandler(){
    	if (myCursor!=null){ myCursor.close(); }
    	if (myDatabase!=null){ myDatabase.close(); }
    	if (myDab!=null){ myDab.closeAccess(myBlog,"messages") ; }
    }
    
    public RepliesHandler openHandler(String blog, String messageID){
        myDab = new DbAccess();
        RepliesHandler rh = new RepliesHandler();
    	myDatabase = myDab.getAccess(blog,"messages",false);
        myCursor = myDatabase.openCursor(null,null);
        myDab.registerCursor(myCursor);
        messageKey = messageID;
        return rh;     
    }
    
    public boolean moreReplies(String messageID){
      theKey = new DatabaseEntry(messageID.getBytes(myDab.charset));
      OperationStatus retVal = myCursor.getNext(theKey, theMessage, lockMode);
      if (retVal!=OperationStatus.SUCCESS){ return false; }
      String messageKey = new String(theKey.getData(),myDab.charset);
      int ml = messageKey.length();
      if (!messageKey.substring(0,ml).equals(messageKey)){
    	  return false; // Different Item ID hex
      }
      return true;
    }
    
    public ChatMessage getMessage(){
    	if (theMessage==null){
    		return null;
    	}
    	EntryBinding eb = 
    		myDab.getObjectBinding(ChatroomDBItems.ChatMessage.class);
    	ChatMessage cm = (ChatMessage)
    		eb.entryToObject(theMessage);
    	return cm;
    }
    
    public String getNextMessageId(){
    	if (theKey==null) return "";
    	return new String(theKey.getData(),myDab.charset);
    }
    
    public String firstFreeMessageID(String prevMess){
    	boolean found = false;
    	String messageID = prevMess;
    	while (found){ 
    		found = moreReplies(messageID);
    		messageID = new String(theKey.getData(),myDab.charset);
    	}
    	int level = MessNameLogic.getLevel(prevMess);
    	String ff=MessNameLogic.incrementAt(messageID, level);
    	return ff;
    }
    
}

