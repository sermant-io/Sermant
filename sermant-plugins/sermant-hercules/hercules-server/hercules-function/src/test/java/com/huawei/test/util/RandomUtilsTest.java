/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.test.util;

import com.huawei.test.exception.FunctionException;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class RandomUtilsTest {

	@Test
	public void test_randomTime_when_all_params_are_valid() {
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		try {
			Date startDate = simpleDateFormat.parse("2021-12-12 00:00:00");
			Date endDate = simpleDateFormat.parse("2021-12-15 00:00:00");
			String randomDate = RandomUtils.randomTime(startDate, endDate, dateFormat);
			Assert.assertTrue(simpleDateFormat.parse(randomDate).getTime() > startDate.getTime());
			Assert.assertTrue(simpleDateFormat.parse(randomDate).getTime() < endDate.getTime());
		} catch (ParseException e) {
			Assert.fail();
		}
	}

	@Test(expected = FunctionException.class)
	public void test_randomTime_when_start_date_is_null() {
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		try {
			Date endDate = simpleDateFormat.parse("2021-12-15 00:00:00");
			RandomUtils.randomTime(null, endDate, dateFormat);
		} catch (ParseException e) {
			Assert.fail();
		}
	}

	@Test(expected = FunctionException.class)
	public void test_randomTime_when_end_date_is_null() {
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		try {
			Date endDate = simpleDateFormat.parse("2021-12-15 00:00:00");
			RandomUtils.randomTime(null, endDate, dateFormat);
		} catch (ParseException e) {
			Assert.fail();
		}
	}

	@Test
	public void test_randomTime_when_dateformat_is_null() {
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		try {
			Date startDate = simpleDateFormat.parse("2021-12-12 00:00:00");
			Date endDate = simpleDateFormat.parse("2021-12-15 00:00:00");
			String randomDate = RandomUtils.randomTime(startDate, endDate, "");
			Assert.assertTrue(simpleDateFormat.parse(randomDate).getTime() > startDate.getTime());
			Assert.assertTrue(simpleDateFormat.parse(randomDate).getTime() < endDate.getTime());
		} catch (ParseException e) {
			Assert.fail();
		}
	}

	@Test(expected = FunctionException.class)
	public void test_randomTime_when_end_less_than_start() {
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		try {
			Date startDate = simpleDateFormat.parse("2021-12-13 00:00:00");
			Date endDate = simpleDateFormat.parse("2021-12-12 00:00:00");
			String randomDate = RandomUtils.randomTime(startDate, endDate, "");
			Assert.assertTrue(simpleDateFormat.parse(randomDate).getTime() > startDate.getTime());
			Assert.assertTrue(simpleDateFormat.parse(randomDate).getTime() < endDate.getTime());
		} catch (ParseException e) {
			Assert.fail();
		}
	}

	@Test
	public void test_randomString_when_length_less_than_0() {
		String randomString = RandomUtils.randomString(-1);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_when_length_equal_0() {
		String randomString = RandomUtils.randomString(0);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_when_length_is_5() {
		String randomString = RandomUtils.randomString(5);
		Assert.assertEquals(5, randomString.length());
	}

	@Test
	public void test_randomString_by_char_array() {
		char[] chars = "abcdefg".toCharArray();
		String randomString = RandomUtils.randomString(chars, 5);
		Assert.assertEquals(5, randomString.length());
		Assert.assertTrue(Pattern.matches("[a-g]{5}", randomString));
	}

	@Test
	public void test_randomString_by_char_array_when_length_eq_0() {
		char[] charSet = "abcdefg".toCharArray();
		String randomString = RandomUtils.randomString(charSet, 0);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_by_char_array_when_length_lt_0() {
		char[] charSet = "abcdefg".toCharArray();
		String randomString = RandomUtils.randomString(charSet, -2);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_by_char_array_when_chars_length_eq_0() {
		char[] charSet = "".toCharArray();
		String randomString = RandomUtils.randomString(charSet, 2);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_by_char_array_when_chars_is_null() {
		String randomString = RandomUtils.randomString((char[])null, 2);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_by_string_char() {
		String charSet = "abcdefg";
		String randomString = RandomUtils.randomString(charSet, 5);
		Assert.assertEquals(5, randomString.length());
		Assert.assertTrue(Pattern.matches("[a-g]{5}", randomString));
	}

	@Test
	public void test_randomString_by_string_char_when_length_eq_0() {
		String charSet = "abcdefg";
		String randomString = RandomUtils.randomString(charSet, 0);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_by_string_char_when_length_lt_0() {
		String charSet = "abcdefg";
		String randomString = RandomUtils.randomString(charSet, -2);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_by_string_char_when_chars_is_empty() {
		String charSet = "";
		String randomString = RandomUtils.randomString(charSet, 2);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomString_by_string_char_when_chars_is_null() {
		String randomString = RandomUtils.randomString((String)null, 2);
		Assert.assertEquals("", randomString);
	}

	@Test
	public void test_randomAlphabetic() {
		String randomString = RandomUtils.randomAlphabetic(10);
		Assert.assertEquals(10, randomString.length());
		Assert.assertTrue(Pattern.matches("[a-zA-Z]{10}", randomString));
	}

	@Test
	public void test_randomAlphabetic_when_size_is_0() {
		String randomString = RandomUtils.randomAlphabetic(0);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomAlphabetic_when_size_lt_0() {
		String randomString = RandomUtils.randomAlphabetic(-1);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomAlphaNumeric() {
		String randomString = RandomUtils.randomAlphaNumeric(10);
		Assert.assertEquals(10, randomString.length());
		Assert.assertTrue(Pattern.matches("[a-zA-Z0-9]{10}", randomString));
	}

	@Test
	public void test_randomAlphaNumeric_when_size_is_0() {
		String randomString = RandomUtils.randomAlphaNumeric(0);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomAlphaNumeric_when_size_lt_0() {
		String randomString = RandomUtils.randomAlphaNumeric(-1);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomNumeric() {
		String randomString = RandomUtils.randomNumeric(10);
		Assert.assertEquals(10, randomString.length());
		Assert.assertTrue(Pattern.matches("\\d{10}", randomString));
	}

	@Test
	public void test_randomNumeric_when_size_is_0() {
		String randomString = RandomUtils.randomNumeric(0);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomNumeric_when_size_lt_0() {
		String randomString = RandomUtils.randomNumeric(-1);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomAscii() {
		String randomString = RandomUtils.randomAscii(10);
		Assert.assertEquals(10, randomString.length());
	}

	@Test
	public void test_randomAscii_when_size_is_0() {
		String randomString = RandomUtils.randomAscii(0);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomAscii_when_size_lt_0() {
		String randomString = RandomUtils.randomAscii(-1);
		Assert.assertEquals(0, randomString.length());
	}

	@Test
	public void test_randomInt() {
		int randomInt1 = RandomUtils.randomInt();
		int randomInt2 = RandomUtils.randomInt();
		Assert.assertNotEquals(randomInt1, randomInt2);
	}

	@Test
	public void test_randomInt_by_zone() {
		int randomInt = RandomUtils.randomInt(1, 10);
		Assert.assertTrue(randomInt >= 1 && randomInt <= 10);
	}

	@Test(expected = FunctionException.class)
	public void test_randomInt_by_zone_when_end_less_than_start() {
		int randomInt = RandomUtils.randomInt(10, 1);
		Assert.assertTrue(randomInt >= 1 && randomInt <= 10);
	}

	@Test(expected = FunctionException.class)
	public void test_randomInt_by_zone_when_end_eq_start() {
		int randomInt = RandomUtils.randomInt(10, 10);
		Assert.assertTrue(randomInt >= 1 && randomInt <= 10);
	}
}
