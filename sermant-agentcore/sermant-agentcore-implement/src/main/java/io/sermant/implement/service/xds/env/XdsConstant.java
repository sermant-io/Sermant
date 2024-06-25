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

package io.sermant.implement.service.xds.env;

/**
 * Constant
 *
 * @author daizhenyu
 * @since 2024-05-09
 **/
public class XdsConstant {
    /**
     * pod name environment
     */
    public static final String POD_NAME_ENV = "HOSTNAME";

    /**
     * eds resource type
     */
    public static final String EDS_RESOURCE_TYPE = "type.googleapis.com/envoy.config.endpoint.v3.ClusterLoadAssignment";

    /**
     * cds resource type
     */
    public static final String CDS_RESOURCE_TYPE = "type.googleapis.com/envoy.config.cluster.v3.Cluster";

    /**
     * sidecar string
     */
    public static final String SIDECAR = "sidecar";

    /**
     * ~ string
     */
    public static final String WAVY_LINE = "~";

    /**
     * . string
     */
    public static final String POINT = ".";

    /**
     * hots suffix of k8s
     */
    public static final String HOST_SUFFIX = "svc.cluster.local";

    /**
     * cds request cache key for subscribe all resource
     */
    public static final String CDS_ALL_RESOURCE = "CLUSTER_ALL";

    /**
     * the namespace file path of k8s pod
     */
    public static final String K8S_POD_NAMESPACE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

    /**
     * the default namespace of k8s pod
     */
    public static final String K8S_DEFAULT_NAMESPACE = "default";

    private XdsConstant() {
    }
}
