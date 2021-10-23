package org.ngrinder.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "MONITORING_CONFIG")
public class MonitoringConfig extends BaseEntity<MonitoringConfig> {

	private static final int MAX_LONG_STRING_SIZE = 9990;

	private static final int MAX_STRING_SIZE = 2048;

	public MonitoringConfig() {

	}

	@JsonIgnore
	@OneToOne(mappedBy = "monitoringConfig")
	private PerfTest perfTest;

	@Expose
	@Cloneable
	@Column(name = "nmon_all")
	private boolean nmonAll;

	@Expose
	@Cloneable
	@Column(name = "jvm_thr")
	private boolean jvmThr;

	@Expose
	@Cloneable
	@Column(name = "jvm_cpu")
	private boolean jvmCpu;

	@Expose
	@Cloneable
	@Column(name = "jvm_mem")
	private boolean jvmMem;

	@Expose
	@Cloneable
	@Column(name = "jvm_cl")
	private boolean jvmCl;

	@Expose
	@Cloneable
	@Column(name = "jvm_gc")
	private boolean jvmGc;

	@Expose
	@Cloneable
	@Column(name = "jvm_mp")
	private boolean jvmMp;

	public PerfTest getPerfTest() {
		return perfTest;
	}

	public void setPerfTest(PerfTest perfTest) {
		this.perfTest = perfTest;
	}

	public boolean isNmonAll() {
		return nmonAll;
	}

	public void setNmonAll(boolean nmonAll) {
		this.nmonAll = nmonAll;
	}

	public boolean isJvmThr() {
		return jvmThr;
	}

	public void setJvmThr(boolean jvmThr) {
		this.jvmThr = jvmThr;
	}

	public boolean isJvmCpu() {
		return jvmCpu;
	}

	public void setJvmCpu(boolean jvmCpu) {
		this.jvmCpu = jvmCpu;
	}

	public boolean isJvmMem() {
		return jvmMem;
	}

	public void setJvmMem(boolean jvmMem) {
		this.jvmMem = jvmMem;
	}

	public boolean isJvmCl() {
		return jvmCl;
	}

	public void setJvmCl(boolean jvmCl) {
		this.jvmCl = jvmCl;
	}

	public boolean isJvmGc() {
		return jvmGc;
	}

	public void setJvmGc(boolean jvmGc) {
		this.jvmGc = jvmGc;
	}

	public boolean isJvmMp() {
		return jvmMp;
	}

	public void setJvmMp(boolean jvmMp) {
		this.jvmMp = jvmMp;
	}
}
