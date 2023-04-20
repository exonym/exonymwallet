package io.exonym.lib.helpers;

public class Timing {


	public static long currentTime(){
		return System.currentTimeMillis();
		
	} 
	
	public static boolean hasBeen(long start, long ms){
		long error = (long) (ms * 0.002);
		if ((System.currentTimeMillis() - (start-error)) >= ms){
			return true; 
			
		} else {
			return false;
			
		}
	}
	public static long hasBeenMs(long start){
		return (System.currentTimeMillis() - start); 
		
	}
}