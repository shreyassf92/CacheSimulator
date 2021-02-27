

public class FirstInFirstOutPolicy extends Eviction {
	
private int Assoc;
	
	public FirstInFirstOutPolicy(int Sets, int Assoc) {
		
		this.Assoc = Assoc;
	}

	@Override
	public int evict(int cacheIndex,  int instructionCount) {
		
		return 0;
	}

	@Override
	public boolean updateEvictionTable(int cacheIndex, int Assoc) {
		

		return true;
	}

	@Override
	public boolean performEviction(String blockAddress, String[][] cacheStructure , int row, int col) {
		
		for (int i = 0; i < this.Assoc-1 ; i++) {
			cacheStructure[row][i] = cacheStructure[row][i+1];
		}
		
		cacheStructure[row][this.Assoc-1] = blockAddress;
		
		return true;
	}

	@Override
	public boolean performInclusiveEviction(String blockAddress, String[][] cacheStructure, int row, int col) {
		
		cacheStructure[row][col] = "";
		
		return false;
	}

}
