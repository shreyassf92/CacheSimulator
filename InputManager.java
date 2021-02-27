import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InputManager {
	
	//Simulator variables from command line
		static int BLOCKSIZE;
		static int L1_SIZE; 
		static int L1_ASSOC;
		static int L2_SIZE;
		static int L2_ASSOC;
		static Eviction.Policy REPLACE_POLICY_TYPE;
		static Cache.Inclusion_Policy INCLUSION_POLICY_TYPE;
		static String trace_file;
		static List<String> traceList = new ArrayList<String>(); 
		
		private static int REPLACEMENT_POLICY;
		private static int INCLUSION_PROPERTY;
		private static BitsProcessor BitsProcessManager;
		
		public static void processArguments(String[] args) {
			
			//process simulator arguments 
			if(args.length == 8) {
				BLOCKSIZE = Integer.parseInt(args[0]);
				//Constraint : BLOCKSIZE should be power of two
				if(!Utility.isPowerOfTwo(BLOCKSIZE)) {
					System.out.print("Arguments constraint validation failed");
					System.exit(0);
				}
				
				L1_SIZE = Integer.parseInt(args[1]);
				L1_ASSOC = Integer.parseInt(args[2]);
				L2_SIZE = Integer.parseInt(args[3]);
				L2_ASSOC = Integer.parseInt(args[4]);
				REPLACEMENT_POLICY = Integer.parseInt(args[5]);
				switch (REPLACEMENT_POLICY) {
				case 0: {
					REPLACE_POLICY_TYPE = Eviction.Policy.LRU;
					break;
				}
				case 1: {
					REPLACE_POLICY_TYPE = Eviction.Policy.FIFO;
					break;
				}
				case 2: {
					REPLACE_POLICY_TYPE = Eviction.Policy.optimal;
					break;
				}
				}
				
				INCLUSION_PROPERTY = Integer.parseInt(args[6]);
				switch (INCLUSION_PROPERTY) {
				case 0: {
					INCLUSION_POLICY_TYPE = Cache.Inclusion_Policy.non_inclusive;
					break;
				}
				case 1: {
					INCLUSION_POLICY_TYPE = Cache.Inclusion_Policy.inclusive;
					break;
				}			
				}
				
				trace_file = args[7];
				
				//load trace file
				traceList = readTraceFile(trace_file);
				
				//Print simulator configuration 
				System.out.println("===== Simulator configuration =====");
				System.out.println("BLOCKSIZE:\t" + BLOCKSIZE);
				System.out.println("L1_SIZE:\t" + L1_SIZE);
				System.out.println("L1_ASSOC:\t" + L1_ASSOC);
				System.out.println("L2_SIZE:\t" + L2_SIZE);
				System.out.println("L2_ASSOC:\t" + L2_ASSOC);
				System.out.println("REPLACEMENT POLICY:\t" + REPLACE_POLICY_TYPE.toString());
				
				String inc = INCLUSION_POLICY_TYPE.equals(INCLUSION_POLICY_TYPE.inclusive) ? "inclusive" : "non-inclusive";
				System.out.println("INCLUSION PROPERTY:\t" + inc);
				System.out.println("trace_file:\t" +  Paths.get(trace_file).getFileName());
			}
			else
			{
				System.out.print("Number of arguments must be equal to 8");
				System.exit(0);
			}
		}
		
		public static List<String> readTraceFile(String filename) {
			
			List<String> traceList = new ArrayList<String>(); 
			
			try {
			      File traceFile = new File(filename);
			      Scanner scReader = new Scanner(traceFile);
			      while (scReader.hasNextLine()) {
			    	  traceList.add(scReader.nextLine());
			      }
			      scReader.close();
			    } 
			catch (FileNotFoundException e) {
			      System.out.println("An error occurred while reading trace file");
			      System.exit(0);
			    }
			
			return traceList;
		}
		
		public static List<String> readTraceFile_Index_TagAddress_Format(String filename) {
			
			BitsProcessManager = new BitsProcessor(L1_SIZE, BLOCKSIZE, L1_ASSOC);
			List<String> traceList = new ArrayList<String>(); 
			
			
			 
			try {
			      File traceFile = new File(filename);
			      Scanner scReader = new Scanner(traceFile);
			      while (scReader.hasNextLine()) {
			    	  
			    	  String address = scReader.nextLine().split(" ")[1];
			    	  int index = BitsProcessManager.getIndex(address);
			    	  String tagAddress = BitsProcessManager.getBlockAddress(address);
			    	 
			    	  traceList.add(index + " " + tagAddress);		    	  
			      }
			      scReader.close();
			    } 
			catch (FileNotFoundException e) {
			      System.out.println("An error occurred while reading trace file");
			      System.exit(0);
			}
			
			return traceList;
		}

}
