package com.huawei.emergency.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmergencyPlanExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public EmergencyPlanExample() {
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

        public Criteria andPlanIdIsNull() {
            addCriterion("plan_id is null");
            return (Criteria) this;
        }

        public Criteria andPlanIdIsNotNull() {
            addCriterion("plan_id is not null");
            return (Criteria) this;
        }

        public Criteria andPlanIdEqualTo(Integer value) {
            addCriterion("plan_id =", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdNotEqualTo(Integer value) {
            addCriterion("plan_id <>", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdGreaterThan(Integer value) {
            addCriterion("plan_id >", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("plan_id >=", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdLessThan(Integer value) {
            addCriterion("plan_id <", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdLessThanOrEqualTo(Integer value) {
            addCriterion("plan_id <=", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdIn(List<Integer> values) {
            addCriterion("plan_id in", values, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdNotIn(List<Integer> values) {
            addCriterion("plan_id not in", values, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdBetween(Integer value1, Integer value2) {
            addCriterion("plan_id between", value1, value2, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdNotBetween(Integer value1, Integer value2) {
            addCriterion("plan_id not between", value1, value2, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanNoIsNull() {
            addCriterion("plan_no is null");
            return (Criteria) this;
        }

        public Criteria andPlanNoIsNotNull() {
            addCriterion("plan_no is not null");
            return (Criteria) this;
        }

        public Criteria andPlanNoEqualTo(String value) {
            addCriterion("plan_no =", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoNotEqualTo(String value) {
            addCriterion("plan_no <>", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoGreaterThan(String value) {
            addCriterion("plan_no >", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoGreaterThanOrEqualTo(String value) {
            addCriterion("plan_no >=", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoLessThan(String value) {
            addCriterion("plan_no <", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoLessThanOrEqualTo(String value) {
            addCriterion("plan_no <=", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoLike(String value) {
            addCriterion("plan_no like", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoNotLike(String value) {
            addCriterion("plan_no not like", value, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoIn(List<String> values) {
            addCriterion("plan_no in", values, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoNotIn(List<String> values) {
            addCriterion("plan_no not in", values, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoBetween(String value1, String value2) {
            addCriterion("plan_no between", value1, value2, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNoNotBetween(String value1, String value2) {
            addCriterion("plan_no not between", value1, value2, "planNo");
            return (Criteria) this;
        }

        public Criteria andPlanNameIsNull() {
            addCriterion("plan_name is null");
            return (Criteria) this;
        }

        public Criteria andPlanNameIsNotNull() {
            addCriterion("plan_name is not null");
            return (Criteria) this;
        }

        public Criteria andPlanNameEqualTo(String value) {
            addCriterion("plan_name =", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameNotEqualTo(String value) {
            addCriterion("plan_name <>", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameGreaterThan(String value) {
            addCriterion("plan_name >", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameGreaterThanOrEqualTo(String value) {
            addCriterion("plan_name >=", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameLessThan(String value) {
            addCriterion("plan_name <", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameLessThanOrEqualTo(String value) {
            addCriterion("plan_name <=", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameLike(String value) {
            addCriterion("plan_name like", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameNotLike(String value) {
            addCriterion("plan_name not like", value, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameIn(List<String> values) {
            addCriterion("plan_name in", values, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameNotIn(List<String> values) {
            addCriterion("plan_name not in", values, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameBetween(String value1, String value2) {
            addCriterion("plan_name between", value1, value2, "planName");
            return (Criteria) this;
        }

        public Criteria andPlanNameNotBetween(String value1, String value2) {
            addCriterion("plan_name not between", value1, value2, "planName");
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

        public Criteria andCheckUserIsNull() {
            addCriterion("check_user is null");
            return (Criteria) this;
        }

        public Criteria andCheckUserIsNotNull() {
            addCriterion("check_user is not null");
            return (Criteria) this;
        }

        public Criteria andCheckUserEqualTo(String value) {
            addCriterion("check_user =", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserNotEqualTo(String value) {
            addCriterion("check_user <>", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserGreaterThan(String value) {
            addCriterion("check_user >", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserGreaterThanOrEqualTo(String value) {
            addCriterion("check_user >=", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserLessThan(String value) {
            addCriterion("check_user <", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserLessThanOrEqualTo(String value) {
            addCriterion("check_user <=", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserLike(String value) {
            addCriterion("check_user like", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserNotLike(String value) {
            addCriterion("check_user not like", value, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserIn(List<String> values) {
            addCriterion("check_user in", values, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserNotIn(List<String> values) {
            addCriterion("check_user not in", values, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserBetween(String value1, String value2) {
            addCriterion("check_user between", value1, value2, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckUserNotBetween(String value1, String value2) {
            addCriterion("check_user not between", value1, value2, "checkUser");
            return (Criteria) this;
        }

        public Criteria andCheckTimeIsNull() {
            addCriterion("check_time is null");
            return (Criteria) this;
        }

        public Criteria andCheckTimeIsNotNull() {
            addCriterion("check_time is not null");
            return (Criteria) this;
        }

        public Criteria andCheckTimeEqualTo(Date value) {
            addCriterion("check_time =", value, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeNotEqualTo(Date value) {
            addCriterion("check_time <>", value, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeGreaterThan(Date value) {
            addCriterion("check_time >", value, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("check_time >=", value, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeLessThan(Date value) {
            addCriterion("check_time <", value, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeLessThanOrEqualTo(Date value) {
            addCriterion("check_time <=", value, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeIn(List<Date> values) {
            addCriterion("check_time in", values, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeNotIn(List<Date> values) {
            addCriterion("check_time not in", values, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeBetween(Date value1, Date value2) {
            addCriterion("check_time between", value1, value2, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckTimeNotBetween(Date value1, Date value2) {
            addCriterion("check_time not between", value1, value2, "checkTime");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkIsNull() {
            addCriterion("check_remark is null");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkIsNotNull() {
            addCriterion("check_remark is not null");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkEqualTo(String value) {
            addCriterion("check_remark =", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkNotEqualTo(String value) {
            addCriterion("check_remark <>", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkGreaterThan(String value) {
            addCriterion("check_remark >", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("check_remark >=", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkLessThan(String value) {
            addCriterion("check_remark <", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkLessThanOrEqualTo(String value) {
            addCriterion("check_remark <=", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkLike(String value) {
            addCriterion("check_remark like", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkNotLike(String value) {
            addCriterion("check_remark not like", value, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkIn(List<String> values) {
            addCriterion("check_remark in", values, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkNotIn(List<String> values) {
            addCriterion("check_remark not in", values, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkBetween(String value1, String value2) {
            addCriterion("check_remark between", value1, value2, "checkRemark");
            return (Criteria) this;
        }

        public Criteria andCheckRemarkNotBetween(String value1, String value2) {
            addCriterion("check_remark not between", value1, value2, "checkRemark");
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

        public Criteria andScheduleTypeIsNull() {
            addCriterion("schedule_type is null");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeIsNotNull() {
            addCriterion("schedule_type is not null");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeEqualTo(String value) {
            addCriterion("schedule_type =", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeNotEqualTo(String value) {
            addCriterion("schedule_type <>", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeGreaterThan(String value) {
            addCriterion("schedule_type >", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeGreaterThanOrEqualTo(String value) {
            addCriterion("schedule_type >=", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeLessThan(String value) {
            addCriterion("schedule_type <", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeLessThanOrEqualTo(String value) {
            addCriterion("schedule_type <=", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeLike(String value) {
            addCriterion("schedule_type like", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeNotLike(String value) {
            addCriterion("schedule_type not like", value, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeIn(List<String> values) {
            addCriterion("schedule_type in", values, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeNotIn(List<String> values) {
            addCriterion("schedule_type not in", values, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeBetween(String value1, String value2) {
            addCriterion("schedule_type between", value1, value2, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleTypeNotBetween(String value1, String value2) {
            addCriterion("schedule_type not between", value1, value2, "scheduleType");
            return (Criteria) this;
        }

        public Criteria andScheduleConfIsNull() {
            addCriterion("schedule_conf is null");
            return (Criteria) this;
        }

        public Criteria andScheduleConfIsNotNull() {
            addCriterion("schedule_conf is not null");
            return (Criteria) this;
        }

        public Criteria andScheduleConfEqualTo(String value) {
            addCriterion("schedule_conf =", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfNotEqualTo(String value) {
            addCriterion("schedule_conf <>", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfGreaterThan(String value) {
            addCriterion("schedule_conf >", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfGreaterThanOrEqualTo(String value) {
            addCriterion("schedule_conf >=", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfLessThan(String value) {
            addCriterion("schedule_conf <", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfLessThanOrEqualTo(String value) {
            addCriterion("schedule_conf <=", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfLike(String value) {
            addCriterion("schedule_conf like", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfNotLike(String value) {
            addCriterion("schedule_conf not like", value, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfIn(List<String> values) {
            addCriterion("schedule_conf in", values, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfNotIn(List<String> values) {
            addCriterion("schedule_conf not in", values, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfBetween(String value1, String value2) {
            addCriterion("schedule_conf between", value1, value2, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleConfNotBetween(String value1, String value2) {
            addCriterion("schedule_conf not between", value1, value2, "scheduleConf");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusIsNull() {
            addCriterion("schedule_status is null");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusIsNotNull() {
            addCriterion("schedule_status is not null");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusEqualTo(String value) {
            addCriterion("schedule_status =", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusNotEqualTo(String value) {
            addCriterion("schedule_status <>", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusGreaterThan(String value) {
            addCriterion("schedule_status >", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusGreaterThanOrEqualTo(String value) {
            addCriterion("schedule_status >=", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusLessThan(String value) {
            addCriterion("schedule_status <", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusLessThanOrEqualTo(String value) {
            addCriterion("schedule_status <=", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusLike(String value) {
            addCriterion("schedule_status like", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusNotLike(String value) {
            addCriterion("schedule_status not like", value, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusIn(List<String> values) {
            addCriterion("schedule_status in", values, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusNotIn(List<String> values) {
            addCriterion("schedule_status not in", values, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusBetween(String value1, String value2) {
            addCriterion("schedule_status between", value1, value2, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andScheduleStatusNotBetween(String value1, String value2) {
            addCriterion("schedule_status not between", value1, value2, "scheduleStatus");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeIsNull() {
            addCriterion("trigger_last_time is null");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeIsNotNull() {
            addCriterion("trigger_last_time is not null");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeEqualTo(Long value) {
            addCriterion("trigger_last_time =", value, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeNotEqualTo(Long value) {
            addCriterion("trigger_last_time <>", value, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeGreaterThan(Long value) {
            addCriterion("trigger_last_time >", value, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("trigger_last_time >=", value, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeLessThan(Long value) {
            addCriterion("trigger_last_time <", value, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeLessThanOrEqualTo(Long value) {
            addCriterion("trigger_last_time <=", value, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeIn(List<Long> values) {
            addCriterion("trigger_last_time in", values, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeNotIn(List<Long> values) {
            addCriterion("trigger_last_time not in", values, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeBetween(Long value1, Long value2) {
            addCriterion("trigger_last_time between", value1, value2, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerLastTimeNotBetween(Long value1, Long value2) {
            addCriterion("trigger_last_time not between", value1, value2, "triggerLastTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeIsNull() {
            addCriterion("trigger_next_time is null");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeIsNotNull() {
            addCriterion("trigger_next_time is not null");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeEqualTo(Long value) {
            addCriterion("trigger_next_time =", value, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeNotEqualTo(Long value) {
            addCriterion("trigger_next_time <>", value, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeGreaterThan(Long value) {
            addCriterion("trigger_next_time >", value, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("trigger_next_time >=", value, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeLessThan(Long value) {
            addCriterion("trigger_next_time <", value, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeLessThanOrEqualTo(Long value) {
            addCriterion("trigger_next_time <=", value, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeIn(List<Long> values) {
            addCriterion("trigger_next_time in", values, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeNotIn(List<Long> values) {
            addCriterion("trigger_next_time not in", values, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeBetween(Long value1, Long value2) {
            addCriterion("trigger_next_time between", value1, value2, "triggerNextTime");
            return (Criteria) this;
        }

        public Criteria andTriggerNextTimeNotBetween(Long value1, Long value2) {
            addCriterion("trigger_next_time not between", value1, value2, "triggerNextTime");
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