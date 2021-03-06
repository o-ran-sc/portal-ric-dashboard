/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 * Copyright (C) 2019 AT&T Intellectual Property
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
package org.oransc.ric.portal.dashboard.controller;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.oransc.ric.portal.dashboard.AppStatsManager;
import org.oransc.ric.portal.dashboard.DashboardApplication;
import org.oransc.ric.portal.dashboard.DashboardConstants;
import org.oransc.ric.portal.dashboard.DashboardUserManager;
import org.oransc.ric.portal.dashboard.exception.StatsManagerException;
import org.oransc.ric.portal.dashboard.model.AppStats;
import org.oransc.ric.portal.dashboard.model.IDashboardResponse;
import org.oransc.ric.portal.dashboard.model.RicRegion;
import org.oransc.ric.portal.dashboard.model.RicRegionList;
import org.oransc.ric.portal.dashboard.model.RicRegionTransport;
import org.oransc.ric.portal.dashboard.model.StatsDetailsTransport;
import org.oransc.ric.portal.dashboard.model.SuccessTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

/**
 * Answers REST requests for admin services like version, health etc.
 */
@RestController
@RequestMapping(value = AdminController.CONTROLLER_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// Publish paths in constants so tests are easy to write
	public static final String CONTROLLER_PATH = DashboardConstants.ENDPOINT_PREFIX + "/admin";
	public static final String HEALTH_METHOD = "health";
	public static final String INSTANCE_METHOD = "instance";
	public static final String USER_METHOD = "user";
	public static final String VERSION_METHOD = DashboardConstants.VERSION_METHOD;
	public static final String XAPPMETRICS_METHOD = "metrics";
	public static final String STATAPPMETRIC_METHOD = "appmetric";

	@Autowired
	private DashboardUserManager dashboardUserManager;

	@Autowired
	private AppStatsManager appStatsManager;

	@Autowired
	private RicRegionList instanceConfig;

	@ApiOperation(value = "Gets the Dashboard MANIFEST.MF property Implementation-Version.", response = SuccessTransport.class)
	@GetMapping(VERSION_METHOD)
	// No role required
	public SuccessTransport getVersion() {
		// These endpoints are invoked repeatedly by K8S
		logger.trace("getVersion");
		return new SuccessTransport(200,
				DashboardApplication.getImplementationVersion(MethodHandles.lookup().lookupClass()));
	}

	@ApiOperation(value = "Checks the health of the application.", response = SuccessTransport.class)
	@GetMapping(HEALTH_METHOD)
	// No role required
	public SuccessTransport getHealth() {
		// These endpoints are invoked repeatedly by K8S
		logger.trace("getHealth");
		return new SuccessTransport(200, "Dashboard is healthy!");
	}

	@ApiOperation(value = "Gets the list of application users.", response = EcompUser.class, responseContainer = "List")
	@GetMapping(USER_METHOD)
	@Secured({ DashboardConstants.ROLE_ADMIN }) // regular users should not see this
	public List<EcompUser> getUsers() {
		logger.debug("getUsers");
		return dashboardUserManager.getUsers();
	}

	@ApiOperation(value = "Gets the RIC regions and instances.", response = RicRegion.class, responseContainer = "List")
	@GetMapping(INSTANCE_METHOD)
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public List<RicRegionTransport> getRegionsInstances() {
		logger.debug("getRegionsInstances");
		return instanceConfig.getSimpleInstances();
	}

	@ApiOperation(value = "Gets all xApp statistics reporting details.", response = StatsDetailsTransport.class, responseContainer = "List")
	@GetMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ STATAPPMETRIC_METHOD)
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public List<AppStats> getStats(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey) {
		logger.debug("getStats for instance {}", instanceKey);
		return appStatsManager.getStatsByInstance(instanceKey);
	}

	@ApiOperation(value = "Gets a xApp's metrics status by Id.", response = StatsDetailsTransport.class, responseContainer = "List")
	@GetMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ STATAPPMETRIC_METHOD + "/" + DashboardConstants.APP_ID + "/{" + DashboardConstants.APP_ID + "}")
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public AppStats getStatsById(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey,
			@PathVariable(DashboardConstants.APP_ID) int appId) {
		logger.debug("getStatsById for instance {} by app id {}", instanceKey, appId);
		return appStatsManager.getStatsById(instanceKey, appId);
	}

	@ApiOperation(value = "Creates xApp metrics status.")
	@PostMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ STATAPPMETRIC_METHOD)
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public IDashboardResponse createStats(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey,
			@Validated @RequestBody StatsDetailsTransport statsSetupRequest) throws StatsManagerException, IOException {
		logger.debug("createStats with instance {} request {}", instanceKey, statsSetupRequest);
		return appStatsManager.createStats(instanceKey, statsSetupRequest);
	}

	@ApiOperation(value = "Updates xApp metrics status.")
	@PutMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ STATAPPMETRIC_METHOD)
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public void updateStats(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey,
			@Validated @RequestBody StatsDetailsTransport statsSetupRequest) throws StatsManagerException, IOException {
		logger.debug("updateStats for instance {} request {}", instanceKey, statsSetupRequest);
		appStatsManager.updateStats(instanceKey, statsSetupRequest);
	}

	@ApiOperation(value = "Deletes xApp metric status.")
	@DeleteMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ STATAPPMETRIC_METHOD + "/" + DashboardConstants.APP_ID + "/{" + DashboardConstants.APP_ID + "}")
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public ResponseEntity<String> deleteStats(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey,
			@PathVariable(DashboardConstants.APP_ID) int appId) throws StatsManagerException, IOException {
		logger.debug("deleteStats instance {} request {}", instanceKey, appId);
		appStatsManager.deleteStats(instanceKey, appId);
		return ResponseEntity.ok(null);
	}
}