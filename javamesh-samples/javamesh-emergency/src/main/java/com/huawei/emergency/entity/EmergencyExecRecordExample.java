package com.huawei.emergency.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmergencyExecRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public EmergencyExecRecordExample() {
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

        public Criteria andRecordIdIsNull() {
            addCriterion("record_id is null");
            return (Criteria) this;
        }

        public Criteria andRecordIdIsNotNull() {
            addCriterion("record_id is not null");
            return (Criteria) this;
        }

        public Criteria andRecordIdEqualTo(Integer value) {
            addCriterion("record_id =", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdNotEqualTo(Integer value) {
            addCriterion("record_id <>", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdGreaterThan(Integer value) {
            addCriterion("record_id >", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("record_id >=", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdLessThan(Integer value) {
            addCriterion("record_id <", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdLessThanOrEqualTo(Integer value) {
            addCriterion("record_id <=", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdIn(List<Integer> values) {
            addCriterion("record_id in", values, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdNotIn(List<Integer> values) {
            addCriterion("record_id not in", values, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdBetween(Integer value1, Integer value2) {
            addCriterion("record_id between", value1, value2, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdNotBetween(Integer value1, Integer value2) {
            addCriterion("record_id not between", value1, value2, "recordId");
            return (Criteria) this;
        }

        public Criteria andExecIdIsNull() {
            addCriterion("exec_id is null");
            return (Criteria) this;
        }

        public Criteria andExecIdIsNotNull() {
            addCriterion("exec_id is not null");
            return (Criteria) this;
        }

        public Criteria andExecIdEqualTo(Integer value) {
            addCriterion("exec_id =", value, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdNotEqualTo(Integer value) {
            addCriterion("exec_id <>", value, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdGreaterThan(Integer value) {
            addCriterion("exec_id >", value, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("exec_id >=", value, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdLessThan(Integer value) {
            addCriterion("exec_id <", value, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdLessThanOrEqualTo(Integer value) {
            addCriterion("exec_id <=", value, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdIn(List<Integer> values) {
            addCriterion("exec_id in", values, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdNotIn(List<Integer> values) {
            addCriterion("exec_id not in", values, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdBetween(Integer value1, Integer value2) {
            addCriterion("exec_id between", value1, value2, "execId");
            return (Criteria) this;
        }

        public Criteria andExecIdNotBetween(Integer value1, Integer value2) {
            addCriterion("exec_id not between", value1, value2, "execId");
            return (Criteria) this;
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

        public Criteria andPreSceneIdIsNull() {
            addCriterion("pre_scene_id is null");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdIsNotNull() {
            addCriterion("pre_scene_id is not null");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdEqualTo(Integer value) {
            addCriterion("pre_scene_id =", value, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdNotEqualTo(Integer value) {
            addCriterion("pre_scene_id <>", value, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdGreaterThan(Integer value) {
            addCriterion("pre_scene_id >", value, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("pre_scene_id >=", value, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdLessThan(Integer value) {
            addCriterion("pre_scene_id <", value, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdLessThanOrEqualTo(Integer value) {
            addCriterion("pre_scene_id <=", value, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdIn(List<Integer> values) {
            addCriterion("pre_scene_id in", values, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdNotIn(List<Integer> values) {
            addCriterion("pre_scene_id not in", values, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdBetween(Integer value1, Integer value2) {
            addCriterion("pre_scene_id between", value1, value2, "preSceneId");
            return (Criteria) this;
        }

        public Criteria andPreSceneIdNotBetween(Integer value1, Integer value2) {
            addCriterion("pre_scene_id not between", value1, value2, "preSceneId");
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

        public Criteria andParentTaskIdIsNull() {
            addCriterion("parent_task_id is null");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdIsNotNull() {
            addCriterion("parent_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdEqualTo(Integer value) {
            addCriterion("parent_task_id =", value, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdNotEqualTo(Integer value) {
            addCriterion("parent_task_id <>", value, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdGreaterThan(Integer value) {
            addCriterion("parent_task_id >", value, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("parent_task_id >=", value, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdLessThan(Integer value) {
            addCriterion("parent_task_id <", value, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdLessThanOrEqualTo(Integer value) {
            addCriterion("parent_task_id <=", value, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdIn(List<Integer> values) {
            addCriterion("parent_task_id in", values, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdNotIn(List<Integer> values) {
            addCriterion("parent_task_id not in", values, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdBetween(Integer value1, Integer value2) {
            addCriterion("parent_task_id between", value1, value2, "parentTaskId");
            return (Criteria) this;
        }

        public Criteria andParentTaskIdNotBetween(Integer value1, Integer value2) {
            addCriterion("parent_task_id not between", value1, value2, "parentTaskId");
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

        public Criteria andScriptTypeIsNull() {
            addCriterion("script_type is null");
            return (Criteria) this;
        }

        public Criteria andScriptTypeIsNotNull() {
            addCriterion("script_type is not null");
            return (Criteria) this;
        }

        public Criteria andScriptTypeEqualTo(String value) {
            addCriterion("script_type =", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeNotEqualTo(String value) {
            addCriterion("script_type <>", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeGreaterThan(String value) {
            addCriterion("script_type >", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeGreaterThanOrEqualTo(String value) {
            addCriterion("script_type >=", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeLessThan(String value) {
            addCriterion("script_type <", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeLessThanOrEqualTo(String value) {
            addCriterion("script_type <=", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeLike(String value) {
            addCriterion("script_type like", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeNotLike(String value) {
            addCriterion("script_type not like", value, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeIn(List<String> values) {
            addCriterion("script_type in", values, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeNotIn(List<String> values) {
            addCriterion("script_type not in", values, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeBetween(String value1, String value2) {
            addCriterion("script_type between", value1, value2, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptTypeNotBetween(String value1, String value2) {
            addCriterion("script_type not between", value1, value2, "scriptType");
            return (Criteria) this;
        }

        public Criteria andScriptParamsIsNull() {
            addCriterion("script_params is null");
            return (Criteria) this;
        }

        public Criteria andScriptParamsIsNotNull() {
            addCriterion("script_params is not null");
            return (Criteria) this;
        }

        public Criteria andScriptParamsEqualTo(String value) {
            addCriterion("script_params =", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsNotEqualTo(String value) {
            addCriterion("script_params <>", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsGreaterThan(String value) {
            addCriterion("script_params >", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsGreaterThanOrEqualTo(String value) {
            addCriterion("script_params >=", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsLessThan(String value) {
            addCriterion("script_params <", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsLessThanOrEqualTo(String value) {
            addCriterion("script_params <=", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsLike(String value) {
            addCriterion("script_params like", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsNotLike(String value) {
            addCriterion("script_params not like", value, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsIn(List<String> values) {
            addCriterion("script_params in", values, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsNotIn(List<String> values) {
            addCriterion("script_params not in", values, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsBetween(String value1, String value2) {
            addCriterion("script_params between", value1, value2, "scriptParams");
            return (Criteria) this;
        }

        public Criteria andScriptParamsNotBetween(String value1, String value2) {
            addCriterion("script_params not between", value1, value2, "scriptParams");
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

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(Date value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(Date value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(Date value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(Date value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<Date> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<Date> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(Date value1, Date value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return (Criteria) this;
        }

        public Criteria andEndTimeEqualTo(Date value) {
            addCriterion("end_time =", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotEqualTo(Date value) {
            addCriterion("end_time <>", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThan(Date value) {
            addCriterion("end_time >", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("end_time >=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThan(Date value) {
            addCriterion("end_time <", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("end_time <=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIn(List<Date> values) {
            addCriterion("end_time in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotIn(List<Date> values) {
            addCriterion("end_time not in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeBetween(Date value1, Date value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEnsureUserIsNull() {
            addCriterion("ensure_user is null");
            return (Criteria) this;
        }

        public Criteria andEnsureUserIsNotNull() {
            addCriterion("ensure_user is not null");
            return (Criteria) this;
        }

        public Criteria andEnsureUserEqualTo(String value) {
            addCriterion("ensure_user =", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserNotEqualTo(String value) {
            addCriterion("ensure_user <>", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserGreaterThan(String value) {
            addCriterion("ensure_user >", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserGreaterThanOrEqualTo(String value) {
            addCriterion("ensure_user >=", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserLessThan(String value) {
            addCriterion("ensure_user <", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserLessThanOrEqualTo(String value) {
            addCriterion("ensure_user <=", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserLike(String value) {
            addCriterion("ensure_user like", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserNotLike(String value) {
            addCriterion("ensure_user not like", value, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserIn(List<String> values) {
            addCriterion("ensure_user in", values, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserNotIn(List<String> values) {
            addCriterion("ensure_user not in", values, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserBetween(String value1, String value2) {
            addCriterion("ensure_user between", value1, value2, "ensureUser");
            return (Criteria) this;
        }

        public Criteria andEnsureUserNotBetween(String value1, String value2) {
            addCriterion("ensure_user not between", value1, value2, "ensureUser");
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