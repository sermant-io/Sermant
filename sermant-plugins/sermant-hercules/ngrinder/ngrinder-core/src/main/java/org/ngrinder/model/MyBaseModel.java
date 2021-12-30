package org.ngrinder.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.Expose;
import com.huawei.argus.serializer.TimestampDatetimeSerializer;
import com.huawei.argus.serializer.UserSerializer;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import javax.persistence.CascadeType;
import java.util.Date;

/**
 * @Author: j00466872
 * @Date: 2019/4/26 20:06
 * 对于创建者和最后修改者 只序列化用户名
 * 自动生成创建时间和最后修改时间
 */
@MappedSuperclass
public class MyBaseModel<M> extends BaseEntity<M> {

	private static final long serialVersionUID = -3876339828833595694L;

	@Expose
	@Column(name = "created_date", insertable = true, updatable = false)
	@CreationTimestamp
	@JsonSerialize(using = TimestampDatetimeSerializer.class)
	private Date createdDate;

	@OneToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name = "created_user", insertable = true, updatable = false)
	@Index(name = "created_user_index")
	@NotFound(action = NotFoundAction.IGNORE)
	@JsonSerialize(using = UserSerializer.class)
	private User createdUser;

	@Expose
	@Column(name = "last_modified_date", insertable = true, updatable = true)
	@UpdateTimestamp
	@JsonSerialize(using = TimestampDatetimeSerializer.class)
	private Date lastModifiedDate;

	@OneToOne(cascade = {CascadeType.MERGE})
	@JoinColumn(name = "last_modified_user", insertable = true, updatable = true)
	@Index(name = "last_modified_user_index")
	@NotFound(action = NotFoundAction.IGNORE)
	@JsonSerialize(using = UserSerializer.class)
	private User lastModifiedUser;

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public User getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(User createdUser) {
		this.createdUser = createdUser;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public User getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(User lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
}
