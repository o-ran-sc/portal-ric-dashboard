/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 * Copyright (C) 2020 AT&T Intellectual Property
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================LICENSE_END===================================
 */
package org.oransc.ric.portal.dashboard.model;


public class StatsDetailsTransport implements IDashboardResponse {

	private int appId;
	private String appName;
	private String metricUrl;
	private String instanceKey;

	public StatsDetailsTransport(int appId, String appName, String metricUrl, String instanceKey) {
		super();
		this.appId = appId;
		this.appName = appName;
		this.metricUrl = metricUrl;
		this.instanceKey = instanceKey;
	}
	
	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getInstanceKey() {
		return instanceKey;
	}

	public void setInstanceKey(String instanceKey) {
		this.instanceKey = instanceKey;
	}

	public StatsDetailsTransport() {
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[appIdentity=" + getAppId() + ", appName=" + getAppName() + ", metricUrl=" + getMetricUrl()
				+ "]";
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getMetricUrl() {
		return metricUrl;
	}

	public void setMetricUrl(String metricUrl) {
		this.metricUrl = metricUrl;
	}
}
