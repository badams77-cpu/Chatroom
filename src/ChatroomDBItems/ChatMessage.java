package ChatroomDBItems;

import java.io.Serializable;

public class ChatMessage implements Serializable {


	static final long SerialVersionUID = 0xBD0A0CAB0010000L;
	
	public String messId; // Pri auto inc?
	public String text; // 
	public String username; // Secondary
    public long date; //
	public String url=""; // Secondary
	public int modup=0;
	public int moddown=0;
	public double matchScore = 0;
	public String prevMess = null;
	public String nextMess = null;
	
	
    public ChatMessage(String messId,String text, String username){
    	date = System.currentTimeMillis();
    	this.text = text;
    	this.username = username;
    	this.messId = messId;
    }
    	
    public long getDate(){ return date; }
    public String getURL(){ return url; }
    public String getUser(){ return username; }
    public String getMessId(){ return messId; }
    public int getModsup(){ return modup; }
    public int getModsdown(){ return moddown; }
    public double getScore(){ return matchScore; }
    public double getModValue(){
      if (modup+moddown==0){ return 0.0; }
      return (modup-moddown)/(modup+moddown);
    }
    public String getPrevious(){ return prevMess; }
    public String getNext(){ return nextMess; }
    
    public void addModPoint(){ modup++;}
    public void subModPoint(){ moddown++;}
    public void setMatchScore(double score){
    	this.matchScore = score;
    }

}

