
public class Utility {

	public static boolean isPowerOfTwo(int number) {
		
		if(number<=0){
		   return false;
		}
		
		while(number > 1){
			
			if(number % 2 != 0){
				return false;
			}
			
			number = number / 2;
		}
		
		return true;
	 }

	public static int logToBaseTwo(int number) {
		
        int value = (int)Math.ceil(Math.log(number) / Math.log(2)); 
        return value; 
	}
	
	public static String toHexaDecimal(String number) {
		
		StringBuilder hexaDecimal = new StringBuilder("");
		
		char[] numberArray = number.toCharArray();
		int loopCount = 0;
		StringBuilder fourDigits = new StringBuilder("");
		
		
        for (int i = numberArray.length-1 ; i >= 0 ; i--) {
        	loopCount++;
        	fourDigits.append(numberArray[i]);
        	String temp;
        	
        	if(loopCount == 4) {
        		fourDigits.reverse();
        		
        		temp = hexaDecimal.toString();
        		hexaDecimal.setLength(0);
        		hexaDecimal.append(toHexaCore(fourDigits.toString()));
        		hexaDecimal.append(temp);
        		
        	}
        	else if(i == 0) {
    			fourDigits.reverse();
    			StringBuilder newfourDigits = new StringBuilder("");
    			for (int j = 0; j < 4 - fourDigits.length(); j++) {
    				newfourDigits.append("0");
				}
    			newfourDigits.append(fourDigits);
    			
    			temp = hexaDecimal.toString();
        		hexaDecimal.setLength(0);
        		hexaDecimal.append(toHexaCore(newfourDigits.toString()));
        		hexaDecimal.append(temp);
        	}
        	else {
        		continue;
        	}
        	
        	loopCount = 0;
        	fourDigits.setLength(0);
		}
        
        return hexaDecimal.toString().replaceFirst("^0+(?!$)", ""); 
	}
	
	private static String toHexaCore(String digits) {
		
		int power = 0;
		int sum = 0;
		for (int j = 3; j >= 0; j--) {
			int a =   Character.getNumericValue(digits.charAt(j));
        	sum +=  (int) (a * Math.pow(2, power));
        	power++;
		}
		
		if(sum > 9) {

			switch (sum) {
			case 10:
				return "a";
			case 11:
				return "b";
			case 12:
				return "c";
			case 13:
				return "d";
			case 14:
				return "e";
			case 15:
				return "f";
			}
		
		}
		
		return String.valueOf(sum);
	}
	
    public static int binaryToDecimal(String number) {
        
    	int value = 0;
    	int power = 0;
        char[] binaryBits = number.toCharArray();
        
        for (int i = binaryBits.length-1; i >= 0; i--) {
        	
        	if (String.valueOf(binaryBits[i]).equals("1")) {
            	value += Math.pow(2, power);
            }
            power++;
			
		} 
        
        return value;
    }

}
