import java.util.ArrayList;
import java.util.List;

public class Cache {

	private enum Cache_Op{
		Read,
		Write
	}
	
	public enum Inclusion_Policy{
		non_inclusive,
		inclusive
	}
	
	private final int NUM_TOTAL_BITS = 32; 
	private int NUM_BLOCK_OFFSET_BITS;
	private int NUM_INDEX_BITS;
	private int NUM_TAG_BITS;
	private int ASSOC;
	
	private int NUM_SETS;
	private String[][] MEM_STRUCT;
	private Eviction EvictionManager;
	private Inclusion_Policy InclusionPolicyType;
	private BitsProcessor BitsManager;
	public Cache NEXT_CACHE;
	public Cache PREV_CACHE;
	private String CURRENT_INSTRCUTION;
	
	private String[][] INSTRUCTION_POSITION_TRACKER;
	private boolean hasParent;
	
	public int reads;
	public int read_misses;
	public int writes;
	public int write_misses;
	public int write_backs;
	private int L1DirectWriteback;
	
	public Cache(boolean hasParent, int BLOCKSIZE, int SIZE, int ASSOC, int NextLevel_SIZE, int NextLevel_ASSOC, Eviction.Policy ReplacementPolicyType, Cache.Inclusion_Policy InclusionPolicyType) {
		
		this.hasParent = hasParent;
		
		this.BitsManager = new BitsProcessor(SIZE, BLOCKSIZE, ASSOC);
		
		this.NUM_SETS = this.BitsManager.NUM_SETS;
		
		//Initialize memory structure
		this.MEM_STRUCT = new String[NUM_SETS][ASSOC];
		
		//Associativity
		this.ASSOC = ASSOC;
		this.reads = 0;
		this.read_misses = 0;
		this.writes = 0;
		this.write_misses = 0;
		this.write_backs = 0;
		this.L1DirectWriteback = 0;
		
		this.EvictionManager = Eviction.getEvictor(ReplacementPolicyType, NUM_SETS, ASSOC, InputManager.trace_file, this.MEM_STRUCT);
		
		this.InclusionPolicyType = InclusionPolicyType;
		
		//initialize L2
		if (NextLevel_SIZE > 0) {
			NEXT_CACHE = new Cache(true, BLOCKSIZE, NextLevel_SIZE, NextLevel_ASSOC, 0, 0, ReplacementPolicyType, InclusionPolicyType);
			NEXT_CACHE.PREV_CACHE = this;
		}
		
		this.INSTRUCTION_POSITION_TRACKER = new String[NUM_SETS][ASSOC];
	}
	
	public void readFromCache(String Address) {
		
		this.CURRENT_INSTRCUTION = Address;
		
		this.reads++;
		
		int block = this.BitsManager.getBlockOffset(Address);
		String blockAddress = this.BitsManager.getBlockAddress(Address);
		int index = this.BitsManager.getIndex(Address);
		
		//check if block address is present in this level
		int presentBlockColIndex = this.isBlockPresent(blockAddress, index, Cache_Op.Read);
		
		//if present, update eviction table
		if( presentBlockColIndex != -1) {
			
			this.EvictionManager.updateEvictionTable(index, presentBlockColIndex);
		}
		else {
			
			//Load block to cache from main memory
			
			this.read_misses++;	
			this.loadBlockToCache(blockAddress, index, block, Cache_Op.Read);
			
			//send read request of same block to lower level if present
			if(this.NEXT_CACHE != null) {
				
				this.NEXT_CACHE.readFromCache(Address);
				
			}
		}	
	}
	
	public void writeToCache(String Address) {
		
		this.CURRENT_INSTRCUTION = Address;
	
		this.writes++;
		
		int block = this.BitsManager.getBlockOffset(Address);
		String blockAddress = this.BitsManager.getBlockAddress(Address);
		int index = this.BitsManager.getIndex(Address);
		
		//check if block address is present in this level
		int presentBlockColIndex = this.isBlockPresent(blockAddress, index, Cache_Op.Write);
		
		//if present, update eviction table
		if( presentBlockColIndex != -1) {
			
			//get row and column and just update dirty write to block
			this.updateBlockForWrite(blockAddress, index, presentBlockColIndex);
		}
		else {
			
			//Load block to cache from main memory			
			this.write_misses++;
			this.loadBlockToCache(blockAddress, index, block, Cache_Op.Write);
			
			//send read request of same block to lower level if present
			if(this.NEXT_CACHE != null) {
				
				this.NEXT_CACHE.readFromCache(Address);
			}
		}
	}
	
	public void print() {
		
		System.out.println("===== L1 contents =====");
		
		for (int i = 0; i < this.NUM_SETS; i++) {
			System.out.printf("Set\t%d:\t", i);
			
			for (int j = 0; j < this.ASSOC; j++) {
				System.out.printf("%s\t", this.MEM_STRUCT[i][j]);
			}
			
			System.out.println("");
		}
		
		if(this.NEXT_CACHE != null) {
			
			System.out.println("===== L2 contents =====");
			
			for (int i = 0; i < this.NEXT_CACHE.NUM_SETS; i++) {
				System.out.printf("Set\t%d:\t", i);
				
				for (int j = 0; j < this.NEXT_CACHE.ASSOC; j++) {
					System.out.printf("%s\t", this.NEXT_CACHE.MEM_STRUCT[i][j]);
				}
				
				System.out.println("");
			}
			
		}
		
	
		System.out.println("===== Simulation results (raw) =====");
		System.out.println("a. number of L1 reads: \t" + this.reads);
		System.out.println("b. number of L1 read misses: \t" + this.read_misses);
		System.out.println("c. number of L1 writes: \t" + this.writes);
		System.out.println("d. number of L1 write misses: \t" + this.write_misses);
		double missRate = ((double)(this.read_misses+this.write_misses)/(double)(this.reads+this.writes));
		System.out.println("e. L1 miss rate: \t" + String.format("%.6f", missRate));
		System.out.println("f. number of L1 writebacks: \t" + this.write_backs);
		int totalTraffic = 0;
			
		
		if(this.NEXT_CACHE != null) {
			System.out.println("g. number of L2 reads: \t" + this.NEXT_CACHE.reads);
			System.out.println("h. number of L2 read misses: \t" + this.NEXT_CACHE.read_misses);
			System.out.println("i. number of L2 writes: \t" + this.NEXT_CACHE.writes);
			System.out.println("j. number of L2 write misses: \t" + this.NEXT_CACHE.write_misses);
			missRate = ((double)(this.NEXT_CACHE.read_misses)/(double)(this.NEXT_CACHE.reads));
			System.out.println("k. L2 miss rate: \t" + String.format("%.6f", missRate));
			System.out.println("l. number of L2 writebacks: \t" + this.NEXT_CACHE.write_backs);		
			totalTraffic = this.NEXT_CACHE.read_misses+this.NEXT_CACHE.write_misses+this.NEXT_CACHE.write_backs;
			
			if(this.InclusionPolicyType.equals(InclusionPolicyType.inclusive))
				totalTraffic = totalTraffic+L1DirectWriteback;
		}	
		else {
			System.out.println("g. number of L2 reads: \t" + 0);
			System.out.println("h. number of L2 read misses: \t" + 0);
			System.out.println("i. number of L2 writes: \t" + 0);
			System.out.println("j. number of L2 write misses: \t" + 0);
			System.out.println("k. L2 miss rate: \t" + 0);
			System.out.println("l. number of L2 writebacks: \t" + 0);
			totalTraffic = this.read_misses+this.write_misses+this.write_backs;
		}
		
		
		System.out.println("m. total memory traffic: \t" + totalTraffic);
	}

	private int isBlockPresent(String blockAddress, int cacheIndex,  Cache_Op Operartion) {
		
		int blockindex = 0;

		List<String> blockAddresses = new ArrayList<String>();
		
			for (int j = 0; j < this.ASSOC; j++) {
				
				if (this.MEM_STRUCT[cacheIndex][j] != null && this.MEM_STRUCT[cacheIndex][j].contains(" ")) {
					blockAddresses.add(this.MEM_STRUCT[cacheIndex][j].split(" ")[0]); //removing D from address
				}
				else {
				blockAddresses.add(this.MEM_STRUCT[cacheIndex][j]);
				}
			}
		
			blockindex = blockAddresses.indexOf(blockAddress);
		
		return blockindex;
		
	}
	
	private boolean loadBlockToCache(String blockAddress, int cacheIndex, int blockOffset, Cache_Op Operation) {
		
		boolean loaded = false;
		
		if (Operation == Cache_Op.Write) {
			
			blockAddress = blockAddress + " D";
		}
		
		for (int j = 0; j < this.ASSOC; j++) {
			
			if(this.MEM_STRUCT[cacheIndex][j] == null || this.MEM_STRUCT[cacheIndex][j].isEmpty()) {
				
				this.MEM_STRUCT[cacheIndex][j] = blockAddress;
				this.EvictionManager.updateEvictionTable(cacheIndex, j);
				this.INSTRUCTION_POSITION_TRACKER[cacheIndex][j] = this.CURRENT_INSTRCUTION;
				loaded = true;
				break;
			}				
		}
		
		if (!loaded) {
			int evictedBlockColIndex = EvictionManager.evict(cacheIndex, this.reads+this.writes); //returns which columns block should be evicted
		
			//check if policy is inclusive
			if (this.InclusionPolicyType == InclusionPolicyType.inclusive) {
				
				//check if this is L2 or lower level cache
				if (this.hasParent) {
					
					String evictingAddress = this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex];
					
					//Check if block is dirty before evicting
					if(isEvictBlockDirty(this.MEM_STRUCT[cacheIndex][evictedBlockColIndex])) {
						
						this.write_backs++;
						
						if(this.NEXT_CACHE != null) {
							
							String writeBackBlockAddress = this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex];
							this.NEXT_CACHE.writeToCache(writeBackBlockAddress);
						}
						else
						{
							loaded = this.EvictionManager.performEviction(blockAddress, this.MEM_STRUCT, cacheIndex, evictedBlockColIndex);
							this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex] = this.CURRENT_INSTRCUTION;
							
							//invalidate block in parent cache if exists
							String evictingBlockAddress = this.PREV_CACHE.BitsManager.getBlockAddress(evictingAddress);
							int evictingIndex = this.PREV_CACHE.BitsManager.getIndex(evictingAddress);
							int evictingCol = this.PREV_CACHE.isBlockPresent(evictingBlockAddress, evictingIndex, Cache_Op.Read);
							
							if(evictingCol != -1) {
								
								//if block is dirty write to main memory - should we anything more here ?
								if(this.PREV_CACHE.MEM_STRUCT[evictingIndex][evictingCol].contains(" D")) {
									
									this.PREV_CACHE.L1DirectWriteback++;
								}
								
								this.PREV_CACHE.EvictionManager.performInclusiveEviction("", this.PREV_CACHE.MEM_STRUCT, evictingIndex, evictingCol);
								this.PREV_CACHE.INSTRUCTION_POSITION_TRACKER[evictingIndex][evictingCol] = "";
								
								//this.PREV_CACHE.MEM_STRUCT[evictingIndex][evictingCol] = "";
								//this.PREV_CACHE.write_backs++;
								
								
							}
						}
					}
					else {
						
						loaded = this.EvictionManager.performEviction(blockAddress, this.MEM_STRUCT, cacheIndex, evictedBlockColIndex);
						this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex] = this.CURRENT_INSTRCUTION;
						
						//invalidate block in parent cache if exists
						String evictingBlockAddress = this.PREV_CACHE.BitsManager.getBlockAddress(evictingAddress);
						int evictingIndex = this.PREV_CACHE.BitsManager.getIndex(evictingAddress);
						int evictingCol = this.PREV_CACHE.isBlockPresent(evictingBlockAddress, evictingIndex, Cache_Op.Read);
						
						if(evictingCol != -1) {
							
							//if block is dirty write to main memory - should we anything more here ?
							if(this.PREV_CACHE.MEM_STRUCT[evictingIndex][evictingCol].contains(" D")) {
								
								this.PREV_CACHE.L1DirectWriteback++;
							}
							
							this.PREV_CACHE.EvictionManager.performInclusiveEviction("", this.PREV_CACHE.MEM_STRUCT, evictingIndex, evictingCol);
							this.PREV_CACHE.INSTRUCTION_POSITION_TRACKER[evictingIndex][evictingCol] = "";
							
							//this.PREV_CACHE.MEM_STRUCT[evictingIndex][evictingCol] = "";
							//this.PREV_CACHE.write_backs++;
							//if block is dirty write to main memory - should we anything more here ?
						}
					}
						
				}
				else {
					
					//Check if block is dirty before evicting
					if(isEvictBlockDirty(this.MEM_STRUCT[cacheIndex][evictedBlockColIndex])) {
						
						this.write_backs++;
							
						try {
							if(this.NEXT_CACHE != null) {
								
								String writeBackBlockAddress = this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex];
								this.NEXT_CACHE.writeToCache(writeBackBlockAddress);
							}						
						}
						catch(Exception e) {
							System.out.println("===== Exception =====");
							System.out.println("InstrcutionCount: "+ (this.reads+this.writes));
						}
						
					}
					
					loaded = this.EvictionManager.performEviction(blockAddress, this.MEM_STRUCT, cacheIndex, evictedBlockColIndex);
					this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex] = this.CURRENT_INSTRCUTION;
				}
					
			}
			else {
				
				//Check if block is dirty before evicting
				if(isEvictBlockDirty(this.MEM_STRUCT[cacheIndex][evictedBlockColIndex])) {
					
					this.write_backs++;
						
					try {
						if(this.NEXT_CACHE != null) {
							
							String writeBackBlockAddress = this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex];
							this.NEXT_CACHE.writeToCache(writeBackBlockAddress);
						}						
					}
					catch(Exception e) {
						System.out.println("===== Exception =====");
						System.out.println("InstrcutionCount: "+ (this.reads+this.writes));
					}
					
				}
				
				loaded = this.EvictionManager.performEviction(blockAddress, this.MEM_STRUCT, cacheIndex, evictedBlockColIndex);
				this.INSTRUCTION_POSITION_TRACKER[cacheIndex][evictedBlockColIndex] = this.CURRENT_INSTRCUTION;
			}
			
		}
		
		return loaded;
	}
	
	private void updateBlockForWrite(String blockAddress, int cacheIndex, int colIndex) {
		
		blockAddress = blockAddress + " D";
		
		this.MEM_STRUCT[cacheIndex][colIndex] = blockAddress;
		this.EvictionManager.updateEvictionTable(cacheIndex, colIndex);
	}

	private static boolean isEvictBlockDirty(String blockAddress) {
		boolean dirty = false;
		
		dirty = blockAddress.contains(" D") ? true : false;
		
		return dirty;
	}
}
