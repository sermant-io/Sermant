/*
 * Copyright 2017 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on io/micrometer/core/instrument/distribution/DistributionStatisticConfig.java
 * from the Micrometer project.
 */

package io.sermant.core.service.metric.api;

import java.time.Duration;

/**
 * DistributionStatisticConfig
 *
 * @author zwmagic
 * @since 2024-08-17
 */
public class DistributionStatisticConfig {
    private static final Integer EXPIRY_MINUTE = 2;

    /**
     * Defines a static final constant named DEFAULT, which represents a distribution statistic configuration. This
     * configuration controls and describes various aspects of data distribution statistics, such as the publication of
     * percentile histograms, the precision of percentiles, and the expected value range. These configurations are
     * useful for monitoring and analyzing data distributions, particularly in system performance monitoring and log
     * analysis.
     */
    public static final DistributionStatisticConfig DEFAULT =
            new DistributionStatisticConfig().publishPercentileHistogram(false)
                    .percentilePrecision(1).minimumExpectedValue(1.0).maximumExpectedValue(Double.POSITIVE_INFINITY)
                    .distributionStatisticExpiry(Duration.ofMinutes(EXPIRY_MINUTE))
                    .distributionStatisticBufferLength(3);

    private Boolean percentileHistogram;

    private double[] percentiles;

    private Integer percentilePrecision;

    private Double minimumExpectedValue;

    private Double maximumExpectedValue;

    private Duration expiry;

    private Integer bufferLength;

    /**
     * Gets the percentile histogram flag.
     *
     * @return the percentile histogram flag
     */
    public Boolean getPercentileHistogram() {
        return percentileHistogram;
    }

    /**
     * Gets the array of percentiles.
     *
     * @return the array of percentiles
     */
    public double[] getPercentiles() {
        return percentiles;
    }

    /**
     * Gets the precision for percentiles.
     *
     * @return the precision for percentiles
     */
    public Integer getPercentilePrecision() {
        return percentilePrecision;
    }

    /**
     * Gets the minimum expected value.
     *
     * @return the minimum expected value
     */
    public Double getMinimumExpectedValue() {
        return minimumExpectedValue;
    }

    /**
     * Gets the maximum expected value.
     *
     * @return the maximum expected value
     */
    public Double getMaximumExpectedValue() {
        return maximumExpectedValue;
    }

    /**
     * Gets the expiry duration.
     *
     * @return the expiry duration
     */
    public Duration getExpiry() {
        return expiry;
    }

    /**
     * Gets the length of the buffer.
     *
     * @return the length of the buffer
     */
    public Integer getBufferLength() {
        return bufferLength;
    }

    /**
     * Produces an additional time series for each requested percentile. This percentile is computed locally, and so
     * can't be aggregated with percentiles computed across other dimensions (e.g. in a different instance). Use
     * {@link #publishPercentileHistogram()} to publish a histogram that can be used to generate aggregable percentile
     * approximations.
     *
     * @param percentilesValue Percentiles to compute and publish. The 95th percentile should be expressed as
     * {@code 0.95}.
     * @return DistributionStatisticConfig
     */
    public DistributionStatisticConfig publishPercentiles(double[] percentilesValue) {
        this.percentiles = percentilesValue;
        return this;
    }

    /**
     * Adds histogram buckets used to generate aggregable percentile approximations in monitoring systems that have
     * query facilities to do so (e.g. Prometheus' {@code histogram_quantile}, Atlas' {@code :percentiles}).
     *
     * @return DistributionStatisticConfig
     */
    public DistributionStatisticConfig publishPercentileHistogram() {
        return publishPercentileHistogram(true);
    }

    /**
     * Adds histogram buckets used to generate aggregable percentile approximations in monitoring systems that have
     * query facilities to do so (e.g. Prometheus' {@code histogram_quantile}, Atlas' {@code :percentiles}).
     *
     * @param enabled Determines whether percentile histograms should be published.
     * @return DistributionStatisticConfig
     */
    public DistributionStatisticConfig publishPercentileHistogram(Boolean enabled) {
        this.percentileHistogram = enabled;
        return this;
    }

    /**
     * Determines the number of digits of precision to maintain on the dynamic range histogram used to compute
     * percentile approximations. The higher the degrees of precision, the more accurate the approximation is at the
     * cost of more memory.
     *
     * @param digitsOfPrecision The digits of precision to maintain for percentile approximations.
     * @return DistributionStatisticConfig
     */
    public DistributionStatisticConfig percentilePrecision(Integer digitsOfPrecision) {
        this.percentilePrecision = digitsOfPrecision;
        return this;
    }

    /**
     * Sets the minimum value that this distribution summary is expected to observe. Sets a lower bound on histogram
     * buckets that are shipped to monitoring systems that support aggregable percentile approximations.
     *
     * @param min The minimum value that this distribution summary is expected to observe.
     * @return This builder.
     */
    public DistributionStatisticConfig minimumExpectedValue(Double min) {
        this.minimumExpectedValue = min;
        return this;
    }

    /**
     * Sets the maximum value that this distribution summary is expected to observe. Sets an upper bound on histogram
     * buckets that are shipped to monitoring systems that support aggregable percentile approximations.
     *
     * @param max The maximum value that this distribution summary is expected to observe.
     * @return DistributionStatisticConfig
     */
    public DistributionStatisticConfig maximumExpectedValue(Double max) {
        this.maximumExpectedValue = max;
        return this;
    }

    /**
     * Statistics emanating from a distribution summary like max, percentiles, and histogram counts decay over time to
     * give greater weight to recent samples (exception: histogram counts are cumulative for those systems that expect
     * cumulative histogram buckets). Samples are accumulated to such statistics in ring buffers which rotate after this
     * expiry, with a buffer length of {@link #distributionStatisticBufferLength(Integer)}.
     *
     * @param expiryDuration The amount of time samples are accumulated to a histogram before it is reset and rotated.
     * @return DistributionStatisticConfig
     */
    public DistributionStatisticConfig distributionStatisticExpiry(Duration expiryDuration) {
        this.expiry = expiryDuration;
        return this;
    }

    /**
     * Statistics emanating from a distribution summary like max, percentiles, and histogram counts decay over time to
     * give greater weight to recent samples (exception: histogram counts are cumulative for those systems that expect
     * cumulative histogram buckets). Samples are accumulated to such statistics in ring buffers which rotate after
     * {@link #distributionStatisticExpiry(Duration)}, with this buffer length.
     *
     * @param bufferLengthValue The number of histograms to keep in the ring buffer.
     * @return DistributionStatisticConfig
     */
    public DistributionStatisticConfig distributionStatisticBufferLength(Integer bufferLengthValue) {
        this.bufferLength = bufferLengthValue;
        return this;
    }
}
