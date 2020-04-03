package Chatroom;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;


public class DbAccess {
//  Hold DbEnv for Berkeley DB, all transactions
// through here
	public Charset charset;
	private Environment env;
	
	private String chatBase = null;
	private String blogName = null;
	
	private HashMap<String,Database> openDbs;
	private HashMap<String,Environment> openEnvs;
	private HashMap<Cursor,Cursor> openCursors;
	
	public DbAccess(){
	  openDbs = new HashMap<String,Database>();
	  openEnvs = new HashMap<String,Environment>();
	  openCursors = new HashMap<Cursor,Cursor>();
	  chatBase = getChatBase();  
	}
	
	/* READ the config to find dbmBase 
	 * also sets the charset for the DB
	 */		
		
	
	public String getChatBase(){
		ChatConfig cconf = ChatConfig.getConfig();
		try {
			charset = Charset.forName(ChatConfig.dbmCharset);
		} catch (Exception e){
			Logging.severe( "Charset config failed",e);
		}
		return ChatConfig.dbmBase;
	}
	
	
    public Database getAccess(String blogName, String table, boolean readOnly){
	  if (chatBase==null){
	    chatBase = getChatBase();
	  }
	  File home = new File(chatBase+'/'+Utilities.escape(blogName));
	     if (!home.exists()){
	    	 boolean suc = home.mkdirs();
	    	 if (!suc){
	    		 Logging.severe("Can't make database at"+home);
	             throw new RuntimeException("Can't make db at"+home)
	             ; // Need to send exception really
	    	 } else{
	    		 // New Db
	    	   Logging.info("Creating new db at "+home);
	           EnvironmentConfig envConfig = new EnvironmentConfig();
	           envConfig.setTransactional(true);
	           envConfig.setAllowCreate(true);
	           envConfig.setReadOnly(false);
	           envConfig.setCacheSize(1000000);
               Environment env = new Environment(home,envConfig);
               openEnvs.put(home.toString()+":"+table,env);
	           DatabaseConfig dbConfig = new DatabaseConfig();
	           dbConfig.getAllowCreate();
 	           Database db = env.openDatabase(null,table,dbConfig);
 	           openDbs.put(home.toString()+":"+table,db);
	           return db;
	    	 }
	     }	 else {
	    	 EnvironmentConfig envConfig = new EnvironmentConfig();
	           envConfig.setTransactional(true);
	           envConfig.setAllowCreate(true);
	           envConfig.setReadOnly(readOnly);
	           envConfig.setCacheSize(1000000);
		     Environment env = new Environment(home,envConfig);
		     openEnvs.put(home.toString()+":"+table,env);
		     DatabaseConfig dbConfig = new DatabaseConfig();
		     dbConfig.setAllowCreate(true);
		     dbConfig.setReadOnly(false);
		     Database db = env.openDatabase(null,table,dbConfig);
		     openDbs.put(home.toString()+":"+table,db);
		     return db;
	     }
    }

    public void closeAccess(String blogName,String table){
  	  File home = new File(chatBase+'/'+Utilities.escape(blogName));
  	  String where = home.toString()+":"+table;
  	  Database db = openDbs.get(where);
  	  if (db!=null){ db.close(); openDbs.remove(where); }
  	  Environment env = openEnvs.get(where);
  	  if (env!=null){ env.close(); openEnvs.remove(where); }
    }
	     
	public void registerCursor(Cursor cursor){
		openCursors.put(cursor, cursor);
	}
	
	public void closeCursor( Cursor cursor){
		openCursors.remove(cursor);
		cursor.close();
	}
	
	public void closeAll(){
		Set<String> ks = openDbs.keySet();
		for(Iterator<String> ksi = ks.iterator(); ksi.hasNext();){
			String key = ksi.next();
			Database db = openDbs.get(key);
			db.close();
			openDbs.remove(key);
		}
		ks = openEnvs.keySet();
		for(Iterator<String> ksi = ks.iterator(); ksi.hasNext();){
			String key = ksi.next();
			Environment env = openEnvs.get(key);
			env.close();
			openEnvs.remove(key);
		}
		Set<Cursor> cs = openCursors.keySet();
		for(Iterator<Cursor> csi = cs.iterator(); csi.hasNext();){
			Cursor cursor = csi.next();
			cursor.close();
			openCursors.remove(cursor);
		}
	}
	
// Check everything is closed at finalize time	
	
    public void finalize(){
    	// Should close All dbs at shutdown time
    	closeAll();
    }
	public EntryBinding getObjectBinding(Class myClass){
		EntryBinding dataBinding = null;
		try {
			 EnvironmentConfig envConfig = new EnvironmentConfig();
			 envConfig.setTransactional(true);
	         envConfig.setAllowCreate(true);
	         envConfig.setReadOnly(false);
	         envConfig.setCacheSize(1000000);
	         File configlives = new File(ChatConfig.dbmBase+
	        		 File.separator+"classconfig");
             Environment myDbEnv = new Environment(configlives,envConfig);
             
		    // Open the database that you will use to store your data
		    DatabaseConfig myDbConfig = new DatabaseConfig();
		    myDbConfig.setAllowCreate(true);
		    myDbConfig.setSortedDuplicates(true);
		    // The db used to store class information does not require duplicates
		    // support.
		    myDbConfig.setSortedDuplicates(false);
		    Database myClassDb = myDbEnv.openDatabase(null, "classDb",
		                                              myDbConfig);
		    // Instantiate the class catalog
		    StoredClassCatalog classCatalog = new StoredClassCatalog(myClassDb);
		    // Create the binding
		    dataBinding = new SerialBinding(classCatalog,myClass);
		    myClassDb.close();
		    myDbEnv.close();
		    // Create the DatabaseEntry for the key
		    // Database and environment close omitted for brevity
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
        return dataBinding;
	}
    
    
	 
}
