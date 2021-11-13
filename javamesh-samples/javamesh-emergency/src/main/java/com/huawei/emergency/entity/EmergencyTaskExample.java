package com.huawei.emergency.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmergencyTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public EmergencyTaskExample() {
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

        public Criteria andTaskIdIsNull() {
            addCriterion("task_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNotNull() {
            addCriterion("task_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdEqualTo(Integer value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(Integer value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(Integer value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(Integer value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(Integer value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<Integer> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<Integer> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(Integer value1, Integer value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(Integer value1, Integer value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskNoIsNull() {
            addCriterion("task_no is null");
            return (Criteria) this;
        }

        public Criteria andTaskNoIsNotNull() {
            addCriterion("task_no is not null");
            return (Criteria) this;
        }

        public Criteria andTaskNoEqualTo(String value) {
            addCriterion("task_no =", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoNotEqualTo(String value) {
            addCriterion("task_no <>", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoGreaterThan(String value) {
            addCriterion("task_no >", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoGreaterThanOrEqualTo(String value) {
            addCriterion("task_no >=", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoLessThan(String value) {
            addCriterion("task_no <", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoLessThanOrEqualTo(String value) {
            addCriterion("task_no <=", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoLike(String value) {
            addCriterion("task_no like", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoNotLike(String value) {
            addCriterion("task_no not like", value, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoIn(List<String> values) {
            addCriterion("task_no in", values, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoNotIn(List<String> values) {
            addCriterion("task_no not in", values, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoBetween(String value1, String value2) {
            addCriterion("task_no between", value1, value2, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNoNotBetween(String value1, String value2) {
            addCriterion("task_no not between", value1, value2, "taskNo");
            return (Criteria) this;
        }

        public Criteria andTaskNameIsNull() {
            addCriterion("task_name is null");
            return (Criteria) this;
        }

        public Criteria andTaskNameIsNotNull() {
            addCriterion("task_name is not null");
            return (Criteria) this;
        }

        public Criteria andTaskNameEqualTo(String value) {
            addCriterion("task_name =", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotEqualTo(String value) {
            addCriterion("task_name <>", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThan(String value) {
            addCriterion("task_name >", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThanOrEqualTo(String value) {
            addCriterion("task_name >=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThan(String value) {
            addCriterion("task_name <", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThanOrEqualTo(String value) {
            addCriterion("task_name <=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLike(String value) {
            addCriterion("task_name like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotLike(String value) {
            addCriterion("task_name not like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameIn(List<String> values) {
            addCriterion("task_name in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotIn(List<String> values) {
            addCriterion("task_name not in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameBetween(String value1, String value2) {
            addCriterion("task_name between", value1, value2, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotBetween(String value1, String value2) {
            addCriterion("task_name not between", value1, value2, "taskName");
            return (Criteria) this;
        }

        public Criteria andSceneIdIsNull() {
            addCriterion("scene_id is null");
            return (Criteria) this;
        }

        public Criteria andSceneIdIsNotNull() {
            addCriterion("scene_id is not null");
            return (Criteria) this;
        }

        public Criteria andSceneIdEqualTo(Integer value) {
            addCriterion("scene_id =", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdNotEqualTo(Integer value) {
            addCriterion("scene_id <>", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdGreaterThan(Integer value) {
            addCriterion("scene_id >", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("scene_id >=", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdLessThan(Integer value) {
            addCriterion("scene_id <", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdLessThanOrEqualTo(Integer value) {
            addCriterion("scene_id <=", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdIn(List<Integer> values) {
            addCriterion("scene_id in", values, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdNotIn(List<Integer> values) {
            addCriterion("scene_id not in", values, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdBetween(Integer value1, Integer value2) {
            addCriterion("scene_id between", value1, value2, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdNotBetween(Integer value1, Integer value2) {
            addCriterion("scene_id not between", value1, value2, "sceneId");
            return (Criteria) this;
        }

        public Criteria andScriptIdIsNull() {
            addCriterion("script_id is null");
            return (Criteria) this;
        }

        public Criteria andScriptIdIsNotNull() {
            addCriterion("script_id is not null");
            return (Criteria) this;
        }

        public Criteria andScriptIdEqualTo(Integer value) {
            addCriterion("script_id =", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotEqualTo(Integer value) {
            addCriterion("script_id <>", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdGreaterThan(Integer value) {
            addCriterion("script_id >", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("script_id >=", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLessThan(Integer value) {
            addCriterion("script_id <", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdLessThanOrEqualTo(Integer value) {
            addCriterion("script_id <=", value, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdIn(List<Integer> values) {
            addCriterion("script_id in", values, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotIn(List<Integer> values) {
            addCriterion("script_id not in", values, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdBetween(Integer value1, Integer value2) {
            addCriterion("script_id between", value1, value2, "scriptId");
            return (Criteria) this;
        }

        public Criteria andScriptIdNotBetween(Integer value1, Integer value2) {
            addCriterion("script_id not between", value1, value2, "scriptId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdIsNull() {
            addCriterion("pre_task_id is null");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdIsNotNull() {
            addCriterion("pre_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdEqualTo(Integer value) {
            addCriterion("pre_task_id =", value, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdNotEqualTo(Integer value) {
            addCriterion("pre_task_id <>", value, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdGreaterThan(Integer value) {
            addCriterion("pre_task_id >", value, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("pre_task_id >=", value, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdLessThan(Integer value) {
            addCriterion("pre_task_id <", value, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdLessThanOrEqualTo(Integer value) {
            addCriterion("pre_task_id <=", value, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdIn(List<Integer> values) {
            addCriterion("pre_task_id in", values, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdNotIn(List<Integer> values) {
            addCriterion("pre_task_id not in", values, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdBetween(Integer value1, Integer value2) {
            addCriterion("pre_task_id between", value1, value2, "preTaskId");
            return (Criteria) this;
        }

        public Criteria andPreTaskIdNotBetween(Integer value1, Integer value2) {
            addCriterion("pre_task_id not between", value1, value2, "preTaskId");
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

        public Criteria andChannelTypeIsNull() {
            addCriterion("channel_type is null");
            return (Criteria) this;
        }

        public Criteria andChannelTypeIsNotNull() {
            addCriterion("channel_type is not null");
            return (Criteria) this;
        }

        public Criteria andChannelTypeEqualTo(String value) {
            addCriterion("channel_type =", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotEqualTo(String value) {
            addCriterion("channel_type <>", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeGreaterThan(String value) {
            addCriterion("channel_type >", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeGreaterThanOrEqualTo(String value) {
            addCriterion("channel_type >=", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeLessThan(String value) {
            addCriterion("channel_type <", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeLessThanOrEqualTo(String value) {
            addCriterion("channel_type <=", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeLike(String value) {
            addCriterion("channel_type like", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotLike(String value) {
            addCriterion("channel_type not like", value, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeIn(List<String> values) {
            addCriterion("channel_type in", values, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotIn(List<String> values) {
            addCriterion("channel_type not in", values, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeBetween(String value1, String value2) {
            addCriterion("channel_type between", value1, value2, "channelType");
            return (Criteria) this;
        }

        public Criteria andChannelTypeNotBetween(String value1, String value2) {
            addCriterion("channel_type not between", value1, value2, "channelType");
            return (Criteria) this;
        }

        public Criteria andScriptNameIsNull() {
            addCriterion("script_name is null");
            return (Criteria) this;
        }

        public Criteria andScriptNameIsNotNull() {
            addCriterion("script_name is not null");
            return (Criteria) this;
        }

        public Criteria andScriptNameEqualTo(String value) {
            addCriterion("script_name =", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameNotEqualTo(String value) {
            addCriterion("script_name <>", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameGreaterThan(String value) {
            addCriterion("script_name >", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameGreaterThanOrEqualTo(String value) {
            addCriterion("script_name >=", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameLessThan(String value) {
            addCriterion("script_name <", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameLessThanOrEqualTo(String value) {
            addCriterion("script_name <=", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameLike(String value) {
            addCriterion("script_name like", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameNotLike(String value) {
            addCriterion("script_name not like", value, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameIn(List<String> values) {
            addCriterion("script_name in", values, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameNotIn(List<String> values) {
            addCriterion("script_name not in", values, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameBetween(String value1, String value2) {
            addCriterion("script_name between", value1, value2, "scriptName");
            return (Criteria) this;
        }

        public Criteria andScriptNameNotBetween(String value1, String value2) {
            addCriterion("script_name not between", value1, value2, "scriptName");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoIsNull() {
            addCriterion("submit_info is null");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoIsNotNull() {
            addCriterion("submit_info is not null");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoEqualTo(String value) {
            addCriterion("submit_info =", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoNotEqualTo(String value) {
            addCriterion("submit_info <>", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoGreaterThan(String value) {
            addCriterion("submit_info >", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoGreaterThanOrEqualTo(String value) {
            addCriterion("submit_info >=", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoLessThan(String value) {
            addCriterion("submit_info <", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoLessThanOrEqualTo(String value) {
            addCriterion("submit_info <=", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoLike(String value) {
            addCriterion("submit_info like", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoNotLike(String value) {
            addCriterion("submit_info not like", value, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoIn(List<String> values) {
            addCriterion("submit_info in", values, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoNotIn(List<String> values) {
            addCriterion("submit_info not in", values, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoBetween(String value1, String value2) {
            addCriterion("submit_info between", value1, value2, "submitInfo");
            return (Criteria) this;
        }

        public Criteria andSubmitInfoNotBetween(String value1, String value2) {
            addCriterion("submit_info not between", value1, value2, "submitInfo");
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