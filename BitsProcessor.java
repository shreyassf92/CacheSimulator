
public class BitsProcessor {

	public static enum Bits{
		Block_offset,
		Index,
		Tag
	}
	
	int NUM_TOTAL_BITS = 32;
	int NUM_BLOCK_OFFSET_BITS;
	int NUM_INDEX_BITS;
	int NUM_TAG_BITS;
	public int NUM_SETS;
	
	public BitsProcessor(int SIZE, int BLOCKSIZE, int ASSOC) {
		
		//Number of BLOCK OFFSET BITS
		NUM_BLOCK_OFFSET_BITS = getNoOfBits(BLOCKSIZE);
		
		//Number of Index BITS
		NUM_SETS = (SIZE/BLOCKSIZE)/(ASSOC);
		//Constraint : NUMber OF SETS should be power of two
		if(!Utility.isPowerOfTwo(BLOCKSIZE)) {
			System.out.print("Arguments constraint validation failed");
			System.exit(0);
		}
		NUM_INDEX_BITS = getNoOfBits(NUM_SETS);
		
		//Number of TAG BITS
		NUM_TAG_BITS = NUM_TOTAL_BITS - NUM_INDEX_BITS - NUM_BLOCK_OFFSET_BITS; 
		
	}
	
	public int getIndex(String Address) {
		
		int index = 0;

		//String block_offset_bits = getBits(Address, BitsProcessor.Bits.Block_offset);
		String index_bits = getBits(Address, BitsProcessor.Bits.Index);
	    index = Utility.binaryToDecimal(index_bits);
		
		return index;
	}
	
	public String getBlockAddress(String Address) {
		
		String blockAddress = "";

		String tag_bits = getBits(Address, BitsProcessor.Bits.Tag);
	    blockAddress = Utility.toHexaDecimal(tag_bits);
		
		return blockAddress;
	}
	
	public int getBlockOffset(String Address) {
		int blockOffset= 0;

		String offset_bits = getBits(Address, BitsProcessor.Bits.Block_offset);
		blockOffset = Utility.binaryToDecimal(offset_bits);
		
		return blockOffset;
	}
	
	public String getBits(String address, Bits category) {
		
		String bits = "";

		//Convert to binary
        String binaryAddress = Integer.toBinaryString(Integer.parseInt(address, 16));
		
        //Make address length equal to 32 bits
      		if(binaryAddress.length() != 32) {
      			StringBuilder tempString = new StringBuilder("");
      			for (int i = 0; i < (32 - binaryAddress.length()); i++) {
      				tempString.append("0");
      			}
      			binaryAddress = tempString.append(binaryAddress).toString();
      		}
        
		if (category == Bits.Block_offset) {
			bits = binaryAddress.substring(NUM_TOTAL_BITS - NUM_BLOCK_OFFSET_BITS);
		}
		if (category == Bits.Index) {
			bits = binaryAddress.substring(NUM_TOTAL_BITS - NUM_BLOCK_OFFSET_BITS - NUM_INDEX_BITS, NUM_TOTAL_BITS - NUM_BLOCK_OFFSET_BITS);
		}
		if (category == Bits.Tag) {
			bits = binaryAddress.substring(0, NUM_TAG_BITS);
		}
		
		return bits;
	}
	
	private static int getNoOfBits(int size) {
		
		int b = 0;
		b = Utility.logToBaseTwo(size);
		return b;
	}
}
