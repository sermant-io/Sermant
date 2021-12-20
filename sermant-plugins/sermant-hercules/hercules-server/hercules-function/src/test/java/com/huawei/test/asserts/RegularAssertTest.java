package com.huawei.test.asserts;

import org.junit.Assert;
import org.junit.Test;

public class RegularAssertTest {
	@Test
	public void test_assertRegular_when_content_is_null() {
		Assert.assertFalse(RegularAssert.assertRegular(null, "regular"));
	}

	@Test
	public void test_assertRegular_when_content_is_empty() {
		Assert.assertFalse(RegularAssert.assertRegular("", "regular"));
	}

	@Test
	public void test_assertRegular_when_regularExpression_is_null() {
		Assert.assertFalse(RegularAssert.assertRegular("content", null));
	}

	@Test
	public void test_assertRegular_when_regularExpression_is_empty() {
		Assert.assertFalse(RegularAssert.assertRegular("content", ""));
	}
}
