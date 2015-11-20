package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class Server {
	private String 								_url = "https://lrpusher.firebaseio.com";
	private String 								_apiKey = "AIzaSyCX0DPP_-JEo-xq9B6WznOAEWxkoiJKVOs";
	
	private Semaphore 							_lock = new Semaphore(0);
	private ArrayList<Pair<String, String> >	_wordFound;
	
	public Server() throws InterruptedException
	{
	    System.out.println( "Sending POST to GCM" );
	    
	    Firebase dataRef = new Firebase(_url).child("users"); 
	    
	    _wordFound = new ArrayList<Pair<String, String> >();
	    
	    // Create thread here
	    
	    ServerLoop(dataRef);
	}

	public void ServerLoop(Firebase dataRef) throws InterruptedException {
		 while(true) {  
		    	_lock.acquire();
		    	
		    	while (_wordFound.size() > 0) {
		    	    Pair<String, String> entry = _wordFound.get(0);
		    	      
		    	    String key = entry.getLeft();
		    	    String value = entry.getRight();

		    	    dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
		    		    @Override
		    		    public void onDataChange(DataSnapshot snapshot) {
		    		    	for (DataSnapshot user : snapshot.getChildren()) {
		    		    		for (DataSnapshot snap : user.getChildren()) {
		    		    			System.out.println(snap.getValue().toString());
			    		    		if (snap.getValue().toString().contains(key)) {
			    		    			Content content = createContent(user.getKey(), key, value);
			    	    	    		POST2GCM.post(_apiKey, content);
			    		    		}
			    		    	}
		    		    	}
		    		    }
		    		    @Override
		    		    public void onCancelled(FirebaseError firebaseError) {
		    		    }
		    		});
		    	    _wordFound.remove(0);
		    	}
		    	_lock.release();
		    	Thread.sleep(100);
		    }
	}
	
    public Content createContent(String regId, String word, String channel){

        Content c = new Content();

        c.addRegId(regId);
        c.createData(word + " à été entendu sur : " + channel, "");

        return c;
    }
}
