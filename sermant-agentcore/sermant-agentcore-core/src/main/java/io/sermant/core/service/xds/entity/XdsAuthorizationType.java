/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
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

/**
 * Authentication types for xDS authorization rules.
 * <p>
 * Defines various authentication sources and attributes that can be used in xDS authorization rules. Each enum value
 * corresponds to a specific authentication source and describes its usage in rules.
 * </p>
 *
 * @author lilai
 * @since 2025-06-17
 */
public enum XdsAuthorizationType {
    /**
     * Authentication based on request headers.Used in rule sections:
     *
     * <ul>
     *   <li>when: rules:when (request.headers[xxx])</li>
     * </ul>
     */
    REQUEST_HEADERS,

    /**
     * Authentication based on direct client IP address.Used in rule sections:
     *
     * <ul>
     *   <li>when: rules:when (source.ip)</li>
     * </ul>
     */
    SOURCE_IP,

    /**
     * Authentication based on original client IP from X-Forwarded-For or proxy protocol.Used in rule sections:
     *
     * <ul>
     *   <li>when: rules:when (remote.ip)</li>
     * </ul>
     */
    REMOTE_IP,

    /**
     * Identity in jwt = issuer + "/" + subject Rule attribution:principal. Rule modification section：
     *
     * <ul>
     *   <li>from: rules:from:source:requestPrincipals</li>
     *   <li>when: rules:when (request.auth.principal)</li>
     * </ul>
     */
    REQUEST_AUTH_PRINCIPAL,

    /**
     * Audience in jwt Rule attribution:principal.Rule modification section：
     *
     * <ul>
     *   <li>when: rules:when (request.auth.audiences)</li>
     * </ul>
     */
    REQUEST_AUTH_AUDIENCES,
    /**
     * Audience in jwt Rule attribution:principal
     *
     * <ul>
     *   <li>when: rules:when (request.auth.claims[xxx])</li>
     * </ul>
     */
    REQUEST_AUTH_CLAIMS,

    /**
     * Azp in jwt: Authorized party - the party to which the ID Token was issued rule attribution:principal. Used in
     * rule sections:
     *
     * <ul>
     *   <li>when: rules:when (request.auth.presenter)</li>
     * </ul>
     */
    REQUEST_AUTH_PRESENTERS,

    /**
     * Server ip Rule attribution:permission
     *
     * <ul>
     *   <li>when: rules:when (destination.ip)</li>
     * </ul>
     */
    DESTINATION_IP,
    /**
     * Server hosts. Rule modification section：
     *
     * <ul>
     *   <li>rules:to:operation:hosts 2)rules:to:operation:notHosts</li>
     * </ul>
     */
    HOSTS,
    /**
     * Server url path Rule attribution:permission. Rule modification section
     *
     * <ul>
     *   <li>rules:to:operation:paths 2)rules:to:operation:notPaths</li>
     * </ul>
     */
    URL_PATH,
    /**
     * Server port based authentication.Used in rule sections:
     *
     * <ul>
     *   <li>to: rules:to:operation:ports or notPorts</li>
     *   <li>when: rules:when (destination.port)</li>
     * </ul>
     */
    DESTINATION_PORT,
    /**
     * Server methods Rule attribution:permission. Used in rule sections:
     *
     * <p>
     *
     *
     * <ul>
     *   <li>to: rules:to:operation:methods</li>
     *   <li>rules:to:operation:notMethods</li>
     * </ul>
     */
    METHODS,

    /**
     * Server sni : request.getServerName() Rule attribution:permission
     *
     * <ul>
     *   <li> when: rules:when (connection.sni)</li>
     * </ul>
     */
    CONNECTION_SNI;
}
