package com.hit.model;

import com.hit.algorithm.IAlgoCache;
import com.hit.algorithm.LRUAlgoCacheImpl;
import com.hit.algorithm.MFUAlgoCacheImpl;
import com.hit.algorithm.SecondChanceAlgoCacheImpl;

/*
 * This class implements Factory Pattern to dynamically create an object of the same interface.
 */
public class AlgoFactory 
{
	/*
	 * Use getAlgo method to get object of type IAlgoCache
	 */
	public static IAlgoCache<Long, Long> getAlgo(String algoType, Integer ramCapacity)
	{
		switch (algoType.toUpperCase()) 
		{
		case "SECOND CHANCE":
			return new SecondChanceAlgoCacheImpl<>(ramCapacity);
		case "LRU":
			return new LRUAlgoCacheImpl<>(ramCapacity);
		case "MFU":
			return new MFUAlgoCacheImpl<>(ramCapacity);
		default:
			return null;
      }
   }
}
