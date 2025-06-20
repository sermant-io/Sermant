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

import io.sermant.core.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * XDS authorization rule implementation.
 * <p>
 * Represents a rule in xDS authorization policy which can be either:
 * <ul>
 *   <li>A leaf node containing a single authentication condition</li>
 *   <li>A composite node containing child rules combined with AND/OR logic</li>
 * </ul>
 * Rules can be negated using the negation flag.
 *
 * @since 2025-06-17
 */
public class XdsAuthorizationRule {
    /**
     * Rule chaining type, determines how child rules are combined:
     * <ul>
     *   <li>LEAF - represents a leaf node with authentication condition</li>
     *   <li>AND - child rules must all be satisfied (logical AND)</li>
     *   <li>OR - at least one child rule must be satisfied (logical OR)</li>
     * </ul>
     */
    private ChildChainType childChainType;

    /**
     * Child rules, null if this is a leaf node
     */
    private List<XdsAuthorizationRule> children;

    /**
     * Authentication condition, null if this is not a leaf node
     */
    private XdsAuthCondition condition;

    /**
     * Whether this rule is negated
     */
    private boolean not;

    /**
     * Default constructor
     */
    public XdsAuthorizationRule() {
    }

    /**
     * Constructor for non-leaf nodes
     *
     * @param childChainType rule chaining type (AND/OR)
     */
    public XdsAuthorizationRule(ChildChainType childChainType) {
        this.childChainType = childChainType;
        this.children = new ArrayList<>();
        this.condition = null;
        this.not = false;
    }

    /**
     * Constructor for non-leaf nodes with negation
     *
     * @param childChainType rule chaining type (AND/OR)
     * @param isNegated whether the rule is negated
     */
    public XdsAuthorizationRule(ChildChainType childChainType, boolean isNegated) {
        this.childChainType = childChainType;
        this.children = new ArrayList<>();
        this.condition = null;
        this.not = isNegated;
    }

    /**
     * Constructor for leaf nodes
     *
     * @param condition authentication condition
     */
    public XdsAuthorizationRule(XdsAuthCondition condition) {
        this.childChainType = ChildChainType.LEAF;
        this.children = null;
        this.condition = condition;
        this.not = false;
    }

    /**
     * Adds a child rule to this rule.
     * <p>
     * Only applicable for non-leaf nodes (AND/OR types).
     *
     * @param rule child rule to add
     * @throws IllegalStateException if called on a leaf node
     */
    public void addChildren(XdsAuthorizationRule rule) {
        if (isLeaf()) {
            throw new IllegalStateException("Cannot add children to a leaf node");
        }
        children.add(rule);
    }

    /**
     * Get children
     *
     * @return XdsAuthorizationRule
     */
    public List<XdsAuthorizationRule> getChildren() {
        return children;
    }

    /**
     * Get child chain type
     *
     * @return ChildChainType
     */
    public ChildChainType getChildChainType() {
        return childChainType;
    }

    /**
     * Checks if this rule is empty.
     * <p>
     * For leaf nodes: checks if condition is null. For non-leaf nodes: checks if there are no child rules.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return isLeaf() ? condition == null : CollectionUtils.isEmpty(children);
    }

    /**
     * Whether this rule is a leaf node.
     *
     * @return true if this is a leaf node (LEAF type), false otherwise
     */
    public boolean isLeaf() {
        return childChainType == ChildChainType.LEAF;
    }

    public XdsAuthCondition getCondition() {
        return condition;
    }

    public boolean isNot() {
        return not;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        XdsAuthorizationRule rule = (XdsAuthorizationRule) object;
        return not == rule.not
                && childChainType == rule.childChainType
                && Objects.equals(children, rule.children)
                && Objects.equals(condition, rule.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(childChainType, children, condition, not);
    }

    /**
     * Enum representing rule chaining types.
     *
     * @since 2025-06-16
     */
    public enum ChildChainType {
        /**
         * Leaf node with authentication condition
         */
        LEAF,
        /**
         * Child rules are combined with AND logic
         */
        AND,
        /**
         * Child rules are combined with OR logic
         */
        OR
    }
}
