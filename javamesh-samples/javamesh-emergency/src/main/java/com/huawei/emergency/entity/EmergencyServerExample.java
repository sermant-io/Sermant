package com.huawei.emergency.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmergencyServerExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public EmergencyServerExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andServerIdIsNull() {
            addCriterion("server_id is null");
            return (Criteria) this;
        }

        public Criteria andServerIdIsNotNull() {
            addCriterion("server_id is not null");
            return (Criteria) this;
        }

        public Criteria andServerIdEqualTo(Integer value) {
            addCriterion("server_id =", value, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdNotEqualTo(Integer value) {
            addCriterion("server_id <>", value, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdGreaterThan(Integer value) {
            addCriterion("server_id >", value, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("server_id >=", value, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdLessThan(Integer value) {
            addCriterion("server_id <", value, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdLessThanOrEqualTo(Integer value) {
            addCriterion("server_id <=", value, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdIn(List<Integer> values) {
            addCriterion("server_id in", values, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdNotIn(List<Integer> values) {
            addCriterion("server_id not in", values, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdBetween(Integer value1, Integer value2) {
            addCriterion("server_id between", value1, value2, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerIdNotBetween(Integer value1, Integer value2) {
            addCriterion("server_id not between", value1, value2, "serverId");
            return (Criteria) this;
        }

        public Criteria andServerNameIsNull() {
            addCriterion("server_name is null");
            return (Criteria) this;
        }

        public Criteria andServerNameIsNotNull() {
            addCriterion("server_name is not null");
            return (Criteria) this;
        }

        public Criteria andServerNameEqualTo(String value) {
            addCriterion("server_name =", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameNotEqualTo(String value) {
            addCriterion("server_name <>", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameGreaterThan(String value) {
            addCriterion("server_name >", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameGreaterThanOrEqualTo(String value) {
            addCriterion("server_name >=", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameLessThan(String value) {
            addCriterion("server_name <", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameLessThanOrEqualTo(String value) {
            addCriterion("server_name <=", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameLike(String value) {
            addCriterion("server_name like", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameNotLike(String value) {
            addCriterion("server_name not like", value, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameIn(List<String> values) {
            addCriterion("server_name in", values, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameNotIn(List<String> values) {
            addCriterion("server_name not in", values, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameBetween(String value1, String value2) {
            addCriterion("server_name between", value1, value2, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerNameNotBetween(String value1, String value2) {
            addCriterion("server_name not between", value1, value2, "serverName");
            return (Criteria) this;
        }

        public Criteria andServerUserIsNull() {
            addCriterion("server_user is null");
            return (Criteria) this;
        }

        public Criteria andServerUserIsNotNull() {
            addCriterion("server_user is not null");
            return (Criteria) this;
        }

        public Criteria andServerUserEqualTo(String value) {
            addCriterion("server_user =", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserNotEqualTo(String value) {
            addCriterion("server_user <>", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserGreaterThan(String value) {
            addCriterion("server_user >", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserGreaterThanOrEqualTo(String value) {
            addCriterion("server_user >=", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserLessThan(String value) {
            addCriterion("server_user <", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserLessThanOrEqualTo(String value) {
            addCriterion("server_user <=", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserLike(String value) {
            addCriterion("server_user like", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserNotLike(String value) {
            addCriterion("server_user not like", value, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserIn(List<String> values) {
            addCriterion("server_user in", values, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserNotIn(List<String> values) {
            addCriterion("server_user not in", values, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserBetween(String value1, String value2) {
            addCriterion("server_user between", value1, value2, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerUserNotBetween(String value1, String value2) {
            addCriterion("server_user not between", value1, value2, "serverUser");
            return (Criteria) this;
        }

        public Criteria andServerIpIsNull() {
            addCriterion("server_ip is null");
            return (Criteria) this;
        }

        public Criteria andServerIpIsNotNull() {
            addCriterion("server_ip is not null");
            return (Criteria) this;
        }

        public Criteria andServerIpEqualTo(String value) {
            addCriterion("server_ip =", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpNotEqualTo(String value) {
            addCriterion("server_ip <>", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpGreaterThan(String value) {
            addCriterion("server_ip >", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpGreaterThanOrEqualTo(String value) {
            addCriterion("server_ip >=", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpLessThan(String value) {
            addCriterion("server_ip <", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpLessThanOrEqualTo(String value) {
            addCriterion("server_ip <=", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpLike(String value) {
            addCriterion("server_ip like", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpNotLike(String value) {
            addCriterion("server_ip not like", value, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpIn(List<String> values) {
            addCriterion("server_ip in", values, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpNotIn(List<String> values) {
            addCriterion("server_ip not in", values, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpBetween(String value1, String value2) {
            addCriterion("server_ip between", value1, value2, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerIpNotBetween(String value1, String value2) {
            addCriterion("server_ip not between", value1, value2, "serverIp");
            return (Criteria) this;
        }

        public Criteria andServerPortIsNull() {
            addCriterion("server_port is null");
            return (Criteria) this;
        }

        public Criteria andServerPortIsNotNull() {
            addCriterion("server_port is not null");
            return (Criteria) this;
        }

        public Criteria andServerPortEqualTo(Integer value) {
            addCriterion("server_port =", value, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortNotEqualTo(Integer value) {
            addCriterion("server_port <>", value, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortGreaterThan(Integer value) {
            addCriterion("server_port >", value, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("server_port >=", value, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortLessThan(Integer value) {
            addCriterion("server_port <", value, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortLessThanOrEqualTo(Integer value) {
            addCriterion("server_port <=", value, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortIn(List<Integer> values) {
            addCriterion("server_port in", values, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortNotIn(List<Integer> values) {
            addCriterion("server_port not in", values, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortBetween(Integer value1, Integer value2) {
            addCriterion("server_port between", value1, value2, "serverPort");
            return (Criteria) this;
        }

        public Criteria andServerPortNotBetween(Integer value1, Integer value2) {
            addCriterion("server_port not between", value1, value2, "serverPort");
            return (Criteria) this;
        }

        public Criteria andHavePasswordIsNull() {
            addCriterion("have_password is null");
            return (Criteria) this;
        }

        public Criteria andHavePasswordIsNotNull() {
            addCriterion("have_password is not null");
            return (Criteria) this;
        }

        public Criteria andHavePasswordEqualTo(String value) {
            addCriterion("have_password =", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordNotEqualTo(String value) {
            addCriterion("have_password <>", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordGreaterThan(String value) {
            addCriterion("have_password >", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordGreaterThanOrEqualTo(String value) {
            addCriterion("have_password >=", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordLessThan(String value) {
            addCriterion("have_password <", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordLessThanOrEqualTo(String value) {
            addCriterion("have_password <=", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordLike(String value) {
            addCriterion("have_password like", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordNotLike(String value) {
            addCriterion("have_password not like", value, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordIn(List<String> values) {
            addCriterion("have_password in", values, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordNotIn(List<String> values) {
            addCriterion("have_password not in", values, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordBetween(String value1, String value2) {
            addCriterion("have_password between", value1, value2, "havePassword");
            return (Criteria) this;
        }

        public Criteria andHavePasswordNotBetween(String value1, String value2) {
            addCriterion("have_password not between", value1, value2, "havePassword");
            return (Criteria) this;
        }

        public Criteria andPasswordModeIsNull() {
            addCriterion("password_mode is null");
            return (Criteria) this;
        }

        public Criteria andPasswordModeIsNotNull() {
            addCriterion("password_mode is not null");
            return (Criteria) this;
        }

        public Criteria andPasswordModeEqualTo(String value) {
            addCriterion("password_mode =", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeNotEqualTo(String value) {
            addCriterion("password_mode <>", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeGreaterThan(String value) {
            addCriterion("password_mode >", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeGreaterThanOrEqualTo(String value) {
            addCriterion("password_mode >=", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeLessThan(String value) {
            addCriterion("password_mode <", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeLessThanOrEqualTo(String value) {
            addCriterion("password_mode <=", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeLike(String value) {
            addCriterion("password_mode like", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeNotLike(String value) {
            addCriterion("password_mode not like", value, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeIn(List<String> values) {
            addCriterion("password_mode in", values, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeNotIn(List<String> values) {
            addCriterion("password_mode not in", values, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeBetween(String value1, String value2) {
            addCriterion("password_mode between", value1, value2, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordModeNotBetween(String value1, String value2) {
            addCriterion("password_mode not between", value1, value2, "passwordMode");
            return (Criteria) this;
        }

        public Criteria andPasswordUriIsNull() {
            addCriterion("password_uri is null");
            return (Criteria) this;
        }

        public Criteria andPasswordUriIsNotNull() {
            addCriterion("password_uri is not null");
            return (Criteria) this;
        }

        public Criteria andPasswordUriEqualTo(String value) {
            addCriterion("password_uri =", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriNotEqualTo(String value) {
            addCriterion("password_uri <>", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriGreaterThan(String value) {
            addCriterion("password_uri >", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriGreaterThanOrEqualTo(String value) {
            addCriterion("password_uri >=", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriLessThan(String value) {
            addCriterion("password_uri <", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriLessThanOrEqualTo(String value) {
            addCriterion("password_uri <=", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriLike(String value) {
            addCriterion("password_uri like", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriNotLike(String value) {
            addCriterion("password_uri not like", value, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriIn(List<String> values) {
            addCriterion("password_uri in", values, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriNotIn(List<String> values) {
            addCriterion("password_uri not in", values, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriBetween(String value1, String value2) {
            addCriterion("password_uri between", value1, value2, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordUriNotBetween(String value1, String value2) {
            addCriterion("password_uri not between", value1, value2, "passwordUri");
            return (Criteria) this;
        }

        public Criteria andPasswordIsNull() {
            addCriterion("password is null");
            return (Criteria) this;
        }

        public Criteria andPasswordIsNotNull() {
            addCriterion("password is not null");
            return (Criteria) this;
        }

        public Criteria andPasswordEqualTo(String value) {
            addCriterion("password =", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotEqualTo(String value) {
            addCriterion("password <>", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordGreaterThan(String value) {
            addCriterion("password >", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordGreaterThanOrEqualTo(String value) {
            addCriterion("password >=", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLessThan(String value) {
            addCriterion("password <", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLessThanOrEqualTo(String value) {
            addCriterion("password <=", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLike(String value) {
            addCriterion("password like", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotLike(String value) {
            addCriterion("password not like", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordIn(List<String> values) {
            addCriterion("password in", values, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotIn(List<String> values) {
            addCriterion("password not in", values, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordBetween(String value1, String value2) {
            addCriterion("password between", value1, value2, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotBetween(String value1, String value2) {
            addCriterion("password not between", value1, value2, "password");
            return (Criteria) this;
        }

        public Criteria andLicensedIsNull() {
            addCriterion("licensed is null");
            return (Criteria) this;
        }

        public Criteria andLicensedIsNotNull() {
            addCriterion("licensed is not null");
            return (Criteria) this;
        }

        public Criteria andLicensedEqualTo(String value) {
            addCriterion("licensed =", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedNotEqualTo(String value) {
            addCriterion("licensed <>", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedGreaterThan(String value) {
            addCriterion("licensed >", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedGreaterThanOrEqualTo(String value) {
            addCriterion("licensed >=", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedLessThan(String value) {
            addCriterion("licensed <", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedLessThanOrEqualTo(String value) {
            addCriterion("licensed <=", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedLike(String value) {
            addCriterion("licensed like", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedNotLike(String value) {
            addCriterion("licensed not like", value, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedIn(List<String> values) {
            addCriterion("licensed in", values, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedNotIn(List<String> values) {
            addCriterion("licensed not in", values, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedBetween(String value1, String value2) {
            addCriterion("licensed between", value1, value2, "licensed");
            return (Criteria) this;
        }

        public Criteria andLicensedNotBetween(String value1, String value2) {
            addCriterion("licensed not between", value1, value2, "licensed");
            return (Criteria) this;
        }

        public Criteria andAgentNameIsNull() {
            addCriterion("agent_name is null");
            return (Criteria) this;
        }

        public Criteria andAgentNameIsNotNull() {
            addCriterion("agent_name is not null");
            return (Criteria) this;
        }

        public Criteria andAgentNameEqualTo(String value) {
            addCriterion("agent_name =", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameNotEqualTo(String value) {
            addCriterion("agent_name <>", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameGreaterThan(String value) {
            addCriterion("agent_name >", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameGreaterThanOrEqualTo(String value) {
            addCriterion("agent_name >=", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameLessThan(String value) {
            addCriterion("agent_name <", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameLessThanOrEqualTo(String value) {
            addCriterion("agent_name <=", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameLike(String value) {
            addCriterion("agent_name like", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameNotLike(String value) {
            addCriterion("agent_name not like", value, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameIn(List<String> values) {
            addCriterion("agent_name in", values, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameNotIn(List<String> values) {
            addCriterion("agent_name not in", values, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameBetween(String value1, String value2) {
            addCriterion("agent_name between", value1, value2, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentNameNotBetween(String value1, String value2) {
            addCriterion("agent_name not between", value1, value2, "agentName");
            return (Criteria) this;
        }

        public Criteria andAgentPortIsNull() {
            addCriterion("agent_port is null");
            return (Criteria) this;
        }

        public Criteria andAgentPortIsNotNull() {
            addCriterion("agent_port is not null");
            return (Criteria) this;
        }

        public Criteria andAgentPortEqualTo(Integer value) {
            addCriterion("agent_port =", value, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortNotEqualTo(Integer value) {
            addCriterion("agent_port <>", value, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortGreaterThan(Integer value) {
            addCriterion("agent_port >", value, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("agent_port >=", value, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortLessThan(Integer value) {
            addCriterion("agent_port <", value, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortLessThanOrEqualTo(Integer value) {
            addCriterion("agent_port <=", value, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortIn(List<Integer> values) {
            addCriterion("agent_port in", values, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortNotIn(List<Integer> values) {
            addCriterion("agent_port not in", values, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortBetween(Integer value1, Integer value2) {
            addCriterion("agent_port between", value1, value2, "agentPort");
            return (Criteria) this;
        }

        public Criteria andAgentPortNotBetween(Integer value1, Integer value2) {
            addCriterion("agent_port not between", value1, value2, "agentPort");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNull() {
            addCriterion("create_user is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNotNull() {
            addCriterion("create_user is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserEqualTo(String value) {
            addCriterion("create_user =", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotEqualTo(String value) {
            addCriterion("create_user <>", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThan(String value) {
            addCriterion("create_user >", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThanOrEqualTo(String value) {
            addCriterion("create_user >=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThan(String value) {
            addCriterion("create_user <", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThanOrEqualTo(String value) {
            addCriterion("create_user <=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLike(String value) {
            addCriterion("create_user like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotLike(String value) {
            addCriterion("create_user not like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserIn(List<String> values) {
            addCriterion("create_user in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotIn(List<String> values) {
            addCriterion("create_user not in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserBetween(String value1, String value2) {
            addCriterion("create_user between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotBetween(String value1, String value2) {
            addCriterion("create_user not between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIsNull() {
            addCriterion("update_user is null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIsNotNull() {
            addCriterion("update_user is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateUserEqualTo(String value) {
            addCriterion("update_user =", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotEqualTo(String value) {
            addCriterion("update_user <>", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThan(String value) {
            addCriterion("update_user >", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserGreaterThanOrEqualTo(String value) {
            addCriterion("update_user >=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThan(String value) {
            addCriterion("update_user <", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLessThanOrEqualTo(String value) {
            addCriterion("update_user <=", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserLike(String value) {
            addCriterion("update_user like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotLike(String value) {
            addCriterion("update_user not like", value, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserIn(List<String> values) {
            addCriterion("update_user in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotIn(List<String> values) {
            addCriterion("update_user not in", values, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserBetween(String value1, String value2) {
            addCriterion("update_user between", value1, value2, "updateUser");
            return (Criteria) this;
        }

        public Criteria andUpdateUserNotBetween(String value1, String value2) {
            addCriterion("update_user not between", value1, value2, "updateUser");
            return (Criteria) this;
        }

        public Criteria andIsValidIsNull() {
            addCriterion("is_valid is null");
            return (Criteria) this;
        }

        public Criteria andIsValidIsNotNull() {
            addCriterion("is_valid is not null");
            return (Criteria) this;
        }

        public Criteria andIsValidEqualTo(String value) {
            addCriterion("is_valid =", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidNotEqualTo(String value) {
            addCriterion("is_valid <>", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidGreaterThan(String value) {
            addCriterion("is_valid >", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(String value) {
            addCriterion("is_valid >=", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidLessThan(String value) {
            addCriterion("is_valid <", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidLessThanOrEqualTo(String value) {
            addCriterion("is_valid <=", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidLike(String value) {
            addCriterion("is_valid like", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidNotLike(String value) {
            addCriterion("is_valid not like", value, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidIn(List<String> values) {
            addCriterion("is_valid in", values, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidNotIn(List<String> values) {
            addCriterion("is_valid not in", values, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidBetween(String value1, String value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return (Criteria) this;
        }

        public Criteria andIsValidNotBetween(String value1, String value2) {
            addCriterion("is_valid not between", value1, value2, "isValid");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}