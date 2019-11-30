package org.shelltest.service.entity;

import java.util.ArrayList;
import java.util.List;

public class RepoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RepoExample() {
        oredCriteria = new ArrayList<Criteria>();
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
            criteria = new ArrayList<Criterion>();
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

        public Criteria andRepoTypeIsNull() {
            addCriterion("`repo_type` is null");
            return (Criteria) this;
        }

        public Criteria andRepoTypeIsNotNull() {
            addCriterion("`repo_type` is not null");
            return (Criteria) this;
        }

        public Criteria andRepoTypeEqualTo(String value) {
            addCriterion("`repo_type` =", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeNotEqualTo(String value) {
            addCriterion("`repo_type` <>", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeGreaterThan(String value) {
            addCriterion("`repo_type` >", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeGreaterThanOrEqualTo(String value) {
            addCriterion("`repo_type` >=", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeLessThan(String value) {
            addCriterion("`repo_type` <", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeLessThanOrEqualTo(String value) {
            addCriterion("`repo_type` <=", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeLike(String value) {
            addCriterion("`repo_type` like", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeNotLike(String value) {
            addCriterion("`repo_type` not like", value, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeIn(List<String> values) {
            addCriterion("`repo_type` in", values, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeNotIn(List<String> values) {
            addCriterion("`repo_type` not in", values, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeBetween(String value1, String value2) {
            addCriterion("`repo_type` between", value1, value2, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoTypeNotBetween(String value1, String value2) {
            addCriterion("`repo_type` not between", value1, value2, "repoType");
            return (Criteria) this;
        }

        public Criteria andRepoIsNull() {
            addCriterion("`repo` is null");
            return (Criteria) this;
        }

        public Criteria andRepoIsNotNull() {
            addCriterion("`repo` is not null");
            return (Criteria) this;
        }

        public Criteria andRepoEqualTo(String value) {
            addCriterion("`repo` =", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoNotEqualTo(String value) {
            addCriterion("`repo` <>", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoGreaterThan(String value) {
            addCriterion("`repo` >", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoGreaterThanOrEqualTo(String value) {
            addCriterion("`repo` >=", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoLessThan(String value) {
            addCriterion("`repo` <", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoLessThanOrEqualTo(String value) {
            addCriterion("`repo` <=", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoLike(String value) {
            addCriterion("`repo` like", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoNotLike(String value) {
            addCriterion("`repo` not like", value, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoIn(List<String> values) {
            addCriterion("`repo` in", values, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoNotIn(List<String> values) {
            addCriterion("`repo` not in", values, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoBetween(String value1, String value2) {
            addCriterion("`repo` between", value1, value2, "repo");
            return (Criteria) this;
        }

        public Criteria andRepoNotBetween(String value1, String value2) {
            addCriterion("`repo` not between", value1, value2, "repo");
            return (Criteria) this;
        }

        public Criteria andFilenameIsNull() {
            addCriterion("`filename` is null");
            return (Criteria) this;
        }

        public Criteria andFilenameIsNotNull() {
            addCriterion("`filename` is not null");
            return (Criteria) this;
        }

        public Criteria andFilenameEqualTo(String value) {
            addCriterion("`filename` =", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameNotEqualTo(String value) {
            addCriterion("`filename` <>", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameGreaterThan(String value) {
            addCriterion("`filename` >", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameGreaterThanOrEqualTo(String value) {
            addCriterion("`filename` >=", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameLessThan(String value) {
            addCriterion("`filename` <", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameLessThanOrEqualTo(String value) {
            addCriterion("`filename` <=", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameLike(String value) {
            addCriterion("`filename` like", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameNotLike(String value) {
            addCriterion("`filename` not like", value, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameIn(List<String> values) {
            addCriterion("`filename` in", values, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameNotIn(List<String> values) {
            addCriterion("`filename` not in", values, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameBetween(String value1, String value2) {
            addCriterion("`filename` between", value1, value2, "filename");
            return (Criteria) this;
        }

        public Criteria andFilenameNotBetween(String value1, String value2) {
            addCriterion("`filename` not between", value1, value2, "filename");
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