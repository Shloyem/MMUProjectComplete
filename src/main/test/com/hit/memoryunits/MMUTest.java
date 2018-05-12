package com.hit.memoryunits;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.hit.algorithm.IAlgoCache;
import com.hit.algorithm.SecondChanceAlgoCacheImpl;

public class MMUTest
{
	@Test
	public void HDTest()
	{
		IAlgoCache<Long, Long> secondChanceImpelentation  = new SecondChanceAlgoCacheImpl<>();
		MemoryManagementUnit mmu = new MemoryManagementUnit(5, secondChanceImpelentation);
		
		HardDisk.CreateHardDisk();
		
		List<Long> pageIDs = Arrays.asList(1L,2L,3L,4L,5L,2L,1L,8L,4L,9L,10L);
		List<Boolean> writeOrNot = Arrays.asList(true, true, true, true, true, true, true, true, true, true, true);
		List<Page<byte[]>> returnedPagesFromMMU = mmu.getPages(pageIDs, writeOrNot);
		
		
		List<Long> expectedReturnedPageIDs = pageIDs;
		List<Long> actualReturnedPageIDs = new ArrayList<>();
		
		for (Page<byte[]> returnedPage : returnedPagesFromMMU)
		{
			if (returnedPage != null)
			{
				actualReturnedPageIDs.add(returnedPage.getPageId());
			}
		}
		
		assertThat(actualReturnedPageIDs, is(expectedReturnedPageIDs));
		
		Set<Long> expectedPageIDsInRAM = new HashSet<Long>(Arrays.asList(2L, 8L, 4L, 9L, 10L));
		
		assertTrue(mmu.getRam().getPages().keySet().containsAll(expectedPageIDsInRAM));
	}
}
