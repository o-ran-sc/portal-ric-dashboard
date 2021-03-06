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

	public StatsDetailsTransport(int appId, String appName, String metricUrl) {
		this.appId = appId;
		this.appName = appName;
		this.metricUrl = metricUrl;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public StatsDetailsTransport() {
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

	@Override
	public String toString() {
		return this.getClass().getName() + "[appId=" + getAppId() + ", appName=" + getAppName() + ", metricUrl="
				+ getMetricUrl() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatsDetailsTransport other = (StatsDetailsTransport) obj;
		return appId == other.appId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + appId;
		result = prime * result + ((appName == null) ? 0 : appName.hashCode());
		result = prime * result + ((metricUrl == null) ? 0 : metricUrl.hashCode());
		return result;
	}

}
