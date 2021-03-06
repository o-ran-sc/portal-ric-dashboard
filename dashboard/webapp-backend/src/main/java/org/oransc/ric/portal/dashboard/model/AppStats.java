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

import java.util.Objects;

public class AppStats implements IDashboardResponse {
	private String instanceKey;
	private StatsDetailsTransport statsDetails;

	public AppStats() {
		super();
	}

	public AppStats(String instanceKey, StatsDetailsTransport statsDetails) {
		super();
		this.instanceKey = instanceKey;
		this.statsDetails = statsDetails;
	}

	public StatsDetailsTransport getStatsDetails() {
		return statsDetails;
	}

	public void setStatsDetails(StatsDetailsTransport statsDetails) {
		this.statsDetails = statsDetails;
	}

	public String getInstanceKey() {
		return instanceKey;
	}

	public void setInstanceKey(String instanceKey) {
		this.instanceKey = instanceKey;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[instance=" + instanceKey + ", statsDetails=" + statsDetails + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppStats other = (AppStats) obj;
		return Objects.equals(instanceKey, other.instanceKey) && Objects.equals(statsDetails, other.statsDetails);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((instanceKey == null) ? 0 : instanceKey.hashCode());
		result = prime * result + ((statsDetails == null) ? 0 : statsDetails.hashCode());
		return result;
	}

}
