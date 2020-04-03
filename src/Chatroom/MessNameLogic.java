package Chatroom;

public class MessNameLogic {

// Each String identifies a possible entry in
// the chat room
// Orignal Items are Identified by a 16 digit hex
// String, the MD5 of the message or item replied to
	
   private static final int minLength = 16;	
	
   public static int getMinLength(){ return minLength; }
   
   public static String getFirstAppend(){ return "00"; }
	
   public static String incrementAt(String s,int level){
	 int start = minLength+level*2;
	 if (s.length()<start){ return s+getFirstAppend(); }
	 String part = s.substring(minLength,s.length()-minLength);  
	 String keep = s.substring(0,minLength);
	 int i = (part.charAt(0)-48) &0x3f;
	 int j = (part.charAt(1)-48) &0x3f;
	 int tot = i*64+j+1;
     char rep[] = new char[2];
     rep[0] = (char) (((tot&0x3fc0)>>6)+48);
     rep[1] = (char) ((tot&0x3f)+48);
     return keep+new String(rep);
   }
   
   public static int  getLevel(String s){
	   int si = s.length()-minLength;
	   return si/2;
   }
   
   
   
}

