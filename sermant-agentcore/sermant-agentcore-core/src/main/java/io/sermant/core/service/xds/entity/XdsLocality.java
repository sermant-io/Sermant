/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.core.service.xds.entity;

import java.util.Objects;

/**
 * xDS Locality
 *
 * @author daizhenyu
 * @since 2024-08-15
 **/
public class XdsLocality {
    private String region;

    private String zone;

    private String subZone;

    private int loadBalanceWeight;

    private int localityPriority;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getSubZone() {
        return subZone;
    }

    public void setSubZone(String subZone) {
        this.subZone = subZone;
    }

    public int getLoadBalanceWeight() {
        return loadBalanceWeight;
    }

    public void setLoadBalanceWeight(int loadBalanceWeight) {
        this.loadBalanceWeight = loadBalanceWeight;
    }

    public int getLocalityPriority() {
        return localityPriority;
    }

    public void setLocalityPriority(int localityPriority) {
        this.localityPriority = localityPriority;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        XdsLocality locality = (XdsLocality) obj;
        return Objects.equals(region, locality.region)
                && Objects.equals(zone, locality.zone)
                && Objects.equals(subZone, locality.subZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, zone, subZone);
    }
}
