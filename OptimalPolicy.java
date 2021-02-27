import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptimalPolicy extends Eviction {

	private String[][] REPLICA_MEM_STRUCT;
	private List<String> traceList = new ArrayList<String>(); 
	private int Sets;
	private int Assoc;

	public OptimalPolicy(int Sets, int Assoc, String TraceFile, String[][] MEM_STRUCT) {
		
		this.traceList = InputManager.readTraceFile_Index_TagAddress_Format(TraceFile);
		this.REPLICA_MEM_STRUCT = MEM_STRUCT;
		this.Assoc = Assoc;
		this.Sets = Sets;
	}

	@Override
	public int evict(int cacheIndex,  int instructionCount) {
		
		List<Integer> futureAccessList = new ArrayList<Integer>();
		
		for (int i = 0; i < this.Assoc; i++) {
			
			//get the next access count of this block address
			futureAccessList.add(getNextAccess(REPLICA_MEM_STRUCT[cacheIndex][i], cacheIndex, instructionCount));
		}
		
		return futureAccessList.indexOf(Collections.max(futureAccessList));
	}
	
	@Override
	public boolean updateEvictionTable(int cacheIndex, int Assoc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean performEviction(String blockAddress, String[][] cacheStructure, int row, int col) {
		
		
		cacheStructure[row][col] = blockAddress;
		
		return true;
	}

	private int getNextAccess(String blockAddress, int cacheIndex, int instructionCount) {
		
		int after = 0;
		//String address = "";
		//BitsProcessor bitsManag = new BitsProcessor(InputManager.L1_SIZE, InputManager.BLOCKSIZE, InputManager.L1_ASSOC);
		
		if(blockAddress.contains(" D"))
			blockAddress = blockAddress.split(" ")[0];
		
		
		/*for (int i = instructionCount; i < traceList.size(); i++) {
			
			address = traceList.get(i).split(" ")[1];

			int index = bitsManag.getIndex(address);
			String tagAddress = bitsManag.getBlockAddress(address);
			
			
			if(index==cacheIndex)  {
				after++;
				if(tagAddress.equals(blockAddress))
					break;
			}
				
		}*/
		
		for (int i = instructionCount; i < traceList.size(); i++) {
			
			after++;
			
			if (traceList.get(i).equals(cacheIndex + " " + blockAddress)) {
				break;
			}
		}
		
		return after;
	}

	@Override
	public boolean performInclusiveEviction(String blockAddress, String[][] cacheStructure, int row, int col) {
		
		cacheStructure[row][col] = "";
		
		return true;

	}
}
