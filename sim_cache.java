
public class sim_cache {

public static void main(String[] args) {
		
		InputManager.processArguments(args); //processes simulator arguments
		
		//initialize L1 cache
		Cache cacheObject = new Cache(false , InputManager.BLOCKSIZE, InputManager.L1_SIZE, InputManager.L1_ASSOC, 
										InputManager.L2_SIZE, InputManager.L2_ASSOC, InputManager.REPLACE_POLICY_TYPE, InputManager.INCLUSION_POLICY_TYPE);
		
		
		
		for (int i = 0; i < InputManager.traceList.size(); i++) {
			
			String[] instruction = InputManager.traceList.get(i).split(" ");
			
			if(instruction[0].compareTo("r") == 0) {
				
				cacheObject.readFromCache(instruction[1]);

			}
			else {
				
				cacheObject.writeToCache(instruction[1]);
			}
				  
		}
		
		cacheObject.print();	
	}
}
