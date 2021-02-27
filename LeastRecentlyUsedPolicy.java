import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LeastRecentlyUsedPolicy extends Eviction {
	 
	private int[][] LRU_Eviction_Table;  
	private int Assoc;
	
	private static int increment;

	public LeastRecentlyUsedPolicy(int Sets, int Assoc) {
		
		LRU_Eviction_Table = new int[Sets][Assoc];
		this.Assoc = Assoc;
		increment = 0;
	}

	@Override
	public int evict(int cacheIndex,  int instructionCount) {
		
		List<Integer> listTimestamp = new ArrayList<Integer>();
		
		for (int i = 0; i < this.Assoc; i++) {
			
			listTimestamp.add(LRU_Eviction_Table[cacheIndex][i]);
		}
		
		return listTimestamp.indexOf(Collections.min(listTimestamp));
	}
	
	@Override
	public boolean updateEvictionTable(int cacheIndex, int Assoc) {
		
		LRU_Eviction_Table[cacheIndex][Assoc] = increment++;
		
		return false;
	}
	
	public boolean resetRowEvictionTable(int cacheIndex, int Assoc) {
		
		LRU_Eviction_Table[cacheIndex][Assoc] = 0;
		
		return false;
	}

	@Override
	public boolean performEviction(String blockAddress, String[][] cacheStructure , int row, int col) {
		
		cacheStructure[row][col] = blockAddress;
		this.updateEvictionTable(row, col);
		
		return true;
	}

	@Override
	public boolean performInclusiveEviction(String blockAddress, String[][] cacheStructure, int row, int col) {
		
		cacheStructure[row][col] = "";
		this.resetRowEvictionTable(row, col);
		
		return true;

	}

}

