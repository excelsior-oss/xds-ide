package com.excelsior.xds.test.core.utils;

import org.junit.Assert;
import org.junit.Test;

import com.excelsior.xds.core.resource.ResourceUtils;

public class ResourceUtilsTest {
	@Test
	public void test() {
		Assert.assertEquals("com.excelsior.xds.core\\src", ResourceUtils.getRelativePath("c:\\Lapukhov\\MyProjects\\k26\\repo\\git\\K26-IDE\\product", "c:\\Lapukhov\\MyProjects\\k26\\repo\\git\\K26-IDE\\product\\com.excelsior.xds.core\\src").toString());
		Assert.assertEquals("..\\..\\com.excelsior.xds.help", ResourceUtils.getRelativePath("c:\\Lapukhov\\MyProjects\\k26\\repo\\git\\K26-IDE\\product\\com.excelsior.xds.core\\src", "c:\\Lapukhov\\MyProjects\\k26\\repo\\git\\K26-IDE\\product\\com.excelsior.xds.help").toString());
		
		System.out.println(ResourceUtils.getRelativePath("d:\\Lapukhov\\MyProjects\\k26\\repo\\git\\K26-IDE\\product\\com.excelsior.xds.core\\src", "c:\\Lapukhov\\MyProjects\\k26\\repo\\git\\K26-IDE\\product\\com.excelsior.xds.help"));
	}
}
