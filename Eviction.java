
public abstract class Eviction {

	public static enum Policy{
		LRU,
		FIFO,
		optimal
	}
	
	public static Eviction getEvictor(Eviction.Policy policy, int Sets, int Assoc, String TraceFile, String[][]  REPLICA_MEM_STRUCT){
		
		Eviction evictObj = null;
		
		switch (policy) {
			case LRU: {
				evictObj = new LeastRecentlyUsedPolicy(Sets, Assoc); 
				break;
			}
			case FIFO: {
				evictObj = new FirstInFirstOutPolicy(Sets, Assoc); 
				break;
			}
			case optimal: {
				evictObj = new OptimalPolicy(Sets, Assoc, TraceFile, REPLICA_MEM_STRUCT); 
				break;
			}
		}
		
		return evictObj;
	}

	public abstract int evict(int cacheIndex, int instructionCount);
	
	public abstract boolean performEviction(String blockAddress, String[][] cacheStructure, int row, int col);
	
	public abstract boolean updateEvictionTable(int cacheIndex, int Assoc);
	
	public abstract boolean performInclusiveEviction(String blockAddress, String[][] cacheStructure, int row, int col);
}
