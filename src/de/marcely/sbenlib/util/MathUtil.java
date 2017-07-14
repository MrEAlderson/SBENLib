package de.marcely.sbenlib.util;

public class MathUtil {
	
	public static int getSubtractable(int number, int subtractBy){
		while(number%subtractBy != 0)
			number++;
		
		return number;
	}
}
