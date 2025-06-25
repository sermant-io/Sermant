package io.sermant.implement.service.xds.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import io.sermant.core.service.xds.entity.match.IpMatcher;
import io.sermant.core.service.xds.entity.match.StringMatcher.MatcherStrategy;

import org.junit.Test;

public class MatcherUtilTest {
    @Test
    public void testConvertEnvoyStringMatcher_NullInput() {
        assertNull(MatcherUtil.convertEnvoyStringMatcher(null));
    }

    @Test
    public void testConvertEnvoyStringMatcher_ExactMatch() {
        StringMatcher envoyMatcher = StringMatcher.newBuilder()
                .setExact("test")
                .setIgnoreCase(true)
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertEnvoyStringMatcher(envoyMatcher);

        assertNotNull(result);
        assertEquals("test", result.getPattern());
        assertEquals(MatcherStrategy.EXACT, result.getStrategy());
        assertTrue(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertEnvoyStringMatcher_PrefixMatch() {
        StringMatcher envoyMatcher = StringMatcher.newBuilder()
                .setPrefix("prefix")
                .setIgnoreCase(false)
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertEnvoyStringMatcher(envoyMatcher);

        assertNotNull(result);
        assertEquals("prefix", result.getPattern());
        assertEquals(MatcherStrategy.PREFIX, result.getStrategy());
        assertFalse(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertEnvoyStringMatcher_SuffixMatch() {
        StringMatcher envoyMatcher = StringMatcher.newBuilder()
                .setSuffix("suffix")
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertEnvoyStringMatcher(envoyMatcher);

        assertNotNull(result);
        assertEquals("suffix", result.getPattern());
        assertEquals(MatcherStrategy.SUFFIX, result.getStrategy());
        assertFalse(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertEnvoyStringMatcher_ContainsMatch() {
        StringMatcher envoyMatcher = StringMatcher.newBuilder()
                .setContains("contains")
                .setIgnoreCase(true)
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertEnvoyStringMatcher(envoyMatcher);

        assertNotNull(result);
        assertEquals("contains", result.getPattern());
        assertEquals(MatcherStrategy.CONTAIN, result.getStrategy());
        assertTrue(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertEnvoyStringMatcher_RegexMatch() {
        StringMatcher envoyMatcher = StringMatcher.newBuilder()
                .setSafeRegex(RegexMatcher.newBuilder().setRegex(".*").build())
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertEnvoyStringMatcher(envoyMatcher);

        assertNotNull(result);
        assertEquals(".*", result.getPattern());
        assertEquals(MatcherStrategy.REGEX, result.getStrategy());
        assertFalse(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertEnvoyStringMatcher_InvalidInput() {
        StringMatcher envoyMatcher = StringMatcher.newBuilder().build();
        assertNull(MatcherUtil.convertEnvoyStringMatcher(envoyMatcher));
    }

    @Test
    public void testConvertCidrRangeToIpMatcher() {
        CidrRange cidrRange = CidrRange.newBuilder()
                .setAddressPrefix("192.168.1.0")
                .setPrefixLen(com.google.protobuf.UInt32Value.of(24))
                .build();

        IpMatcher result = MatcherUtil.convertCidrRangeToIpMatcher(cidrRange);

        assertNotNull(result);
        assertEquals(24, result.getPrefixLength());
    }

    @Test
    public void testConvertHeaderMatcher_NullInput() {
        assertNull(MatcherUtil.convertHeaderMatcher(null));
    }

    @Test
    public void testConvertHeaderMatcher_ExactMatch() {
        HeaderMatcher headerMatcher = HeaderMatcher.newBuilder()
                .setExactMatch("exact")
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertHeaderMatcher(headerMatcher);

        assertNotNull(result);
        assertEquals("exact", result.getPattern());
        assertEquals(MatcherStrategy.EXACT, result.getStrategy());
        assertTrue(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertHeaderMatcher_ContainsMatch() {
        HeaderMatcher headerMatcher = HeaderMatcher.newBuilder()
                .setContainsMatch("contains")
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertHeaderMatcher(headerMatcher);

        assertNotNull(result);
        assertEquals("contains", result.getPattern());
        assertEquals(MatcherStrategy.CONTAIN, result.getStrategy());
        assertTrue(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertHeaderMatcher_PrefixMatch() {
        HeaderMatcher headerMatcher = HeaderMatcher.newBuilder()
                .setPrefixMatch("prefix")
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertHeaderMatcher(headerMatcher);

        assertNotNull(result);
        assertEquals("prefix", result.getPattern());
        assertEquals(MatcherStrategy.PREFIX, result.getStrategy());
        assertTrue(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertHeaderMatcher_SuffixMatch() {
        HeaderMatcher headerMatcher = HeaderMatcher.newBuilder()
                .setSuffixMatch("suffix")
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertHeaderMatcher(headerMatcher);

        assertNotNull(result);
        assertEquals("suffix", result.getPattern());
        assertEquals(MatcherStrategy.SUFFIX, result.getStrategy());
        assertTrue(result.isCaseNotInsensitive());
    }

    @Test
    public void testConvertHeaderMatcher_RegexMatch() {
        HeaderMatcher headerMatcher = HeaderMatcher.newBuilder()
                .setSafeRegexMatch(RegexMatcher.newBuilder().setRegex(".*").build())
                .build();

        io.sermant.core.service.xds.entity.match.StringMatcher result =
                MatcherUtil.convertHeaderMatcher(headerMatcher);

        assertNotNull(result);
        assertEquals(".*", result.getPattern());
        assertEquals(MatcherStrategy.REGEX, result.getStrategy());
        assertTrue(result.isCaseNotInsensitive());
    }
}
