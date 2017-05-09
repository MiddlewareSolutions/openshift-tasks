package org.jboss.as.quickstarts.tasksrs.service;

import static org.junit.Assert.*;

import org.jboss.as.quickstarts.tasksrs.category.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class SmokeIntegrationIT {

	@Test
	public void smoke() {
		assertTrue(true);
	}
}
