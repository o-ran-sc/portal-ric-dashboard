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

import java.lang.invoke.MethodHandles;

import org.oransc.ric.portal.dashboard.DashboardApplication;
import org.oransc.ric.portal.dashboard.DashboardConstants;
import org.oransc.ric.portal.dashboard.config.AppManagerApiBuilder;
import org.oransc.ric.portal.dashboard.model.AppTransport;
import org.oransc.ric.portal.dashboard.model.DashboardDeployableXapps;
import org.oransc.ric.portal.dashboard.model.SuccessTransport;
import org.oransc.ricplt.appmgr.client.api.HealthApi;
import org.oransc.ricplt.appmgr.client.api.XappApi;
import org.oransc.ricplt.appmgr.client.model.AllDeployableXapps;
import org.oransc.ricplt.appmgr.client.model.AllDeployedXapps;
import org.oransc.ricplt.appmgr.client.model.AllXappConfig;
import org.oransc.ricplt.appmgr.client.model.ConfigValidationErrors;
import org.oransc.ricplt.appmgr.client.model.XAppConfig;
import org.oransc.ricplt.appmgr.client.model.Xapp;
import org.oransc.ricplt.appmgr.client.model.XappDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
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
 * Proxies calls from the front end to the App Manager API.
 * 
 * If a method throws RestClientResponseException, it is handled by a method in
 * {@link CustomResponseEntityExceptionHandler} which returns status 502. All
 * other exceptions are handled by Spring which returns status 500.
 */
@Configuration
@RestController
@RequestMapping(value = AppManagerController.CONTROLLER_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class AppManagerController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// Publish paths in constants so tests are easy to write
	public static final String CONTROLLER_PATH = DashboardConstants.ENDPOINT_PREFIX + "/appmgr";
	public static final String HEALTH_ALIVE_METHOD = "health/alive";
	public static final String HEALTH_READY_METHOD = "health/ready";
	public static final String CONFIG_METHOD = "config";
	public static final String XAPPS_METHOD = "xapps";
	public static final String XAPPS_LIST_METHOD = XAPPS_METHOD + "/list";
	// Path parameters
	public static final String PP_XAPP_NAME = "xAppName";
	// minimize repeats
	private static final String CONFIG_METHOD_PATH = DashboardConstants.RIC_INSTANCE_KEY + "/{"
			+ DashboardConstants.RIC_INSTANCE_KEY + "}/" + CONFIG_METHOD;
	private static final String XAPPS_METHOD_PATH = DashboardConstants.RIC_INSTANCE_KEY + "/{"
			+ DashboardConstants.RIC_INSTANCE_KEY + "}/" + XAPPS_METHOD;

	// Populated by the autowired constructor
	private final AppManagerApiBuilder appManagerApiBuilder;

	@Autowired
	public AppManagerController(final AppManagerApiBuilder appManagerApiBuilder) {
		Assert.notNull(appManagerApiBuilder, "builder must not be null");
		this.appManagerApiBuilder = appManagerApiBuilder;
		if (logger.isDebugEnabled())
			logger.debug("ctor: configured with builder type {}", appManagerApiBuilder.getClass().getName());
	}

	@ApiOperation(value = "Gets the App manager client library MANIFEST.MF property Implementation-Version.", response = SuccessTransport.class)
	@GetMapping(DashboardConstants.VERSION_METHOD)
	// No role required
	public SuccessTransport getClientVersion() {
		return new SuccessTransport(200, DashboardApplication.getImplementationVersion(HealthApi.class));
	}

	@ApiOperation(value = "Health check of App Manager - Liveness probe.")
	@GetMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ HEALTH_ALIVE_METHOD)
	// No role required
	public ResponseEntity<String> getHealth(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey) {
		logger.debug("getHealthAlive instance {}", instanceKey);
		HealthApi api = appManagerApiBuilder.getHealthApi(instanceKey);
		api.getHealthAlive();
		return ResponseEntity.status(api.getApiClient().getStatusCode().value()).body(null);
	}

	@ApiOperation(value = "Readiness check of App Manager - Readiness probe.")
	@GetMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ HEALTH_READY_METHOD)
	// No role required
	public ResponseEntity<String> getHealthReady(
			@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey) {
		logger.debug("getHealthReady instance {}", instanceKey);
		HealthApi api = appManagerApiBuilder.getHealthApi(instanceKey);
		api.getHealthReady();
		return ResponseEntity.status(api.getApiClient().getStatusCode().value()).body(null);
	}

	@ApiOperation(value = "Returns the configuration of all Xapps.", response = AllXappConfig.class)
	@GetMapping(CONFIG_METHOD_PATH)
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public AllXappConfig getAllXappConfig(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey) {
		logger.debug("getAllXappConfig instance {}", instanceKey);
		return appManagerApiBuilder.getXappApi(instanceKey).getAllXappConfig();
	}

	@ApiOperation(value = "Modify XApp config.", response = ConfigValidationErrors.class)
	@PutMapping(CONFIG_METHOD_PATH)
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public ConfigValidationErrors modifyXappConfig(
			@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey, //
			@Validated @RequestBody XAppConfig xAppConfig) {
		logger.debug("modifyXappConfig instance {} config {}", instanceKey, xAppConfig);
		return appManagerApiBuilder.getXappApi(instanceKey).modifyXappConfig(xAppConfig);
	}

	@ApiOperation(value = "Returns a list of deployable xapps.", response = DashboardDeployableXapps.class)
	@GetMapping(DashboardConstants.RIC_INSTANCE_KEY + "/{" + DashboardConstants.RIC_INSTANCE_KEY + "}/"
			+ XAPPS_LIST_METHOD)
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public DashboardDeployableXapps getAvailableXapps(
			@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey) {
		logger.debug("getAvailableXapps instance {}", instanceKey);
		AllDeployableXapps deployableXapps = appManagerApiBuilder.getXappApi(instanceKey).listAllXapps();
		// Answer a collection of structure instead of string
		// because I expect the AppMgr to be extended with
		// additional properties for each one.
		DashboardDeployableXapps apps = new DashboardDeployableXapps();
		for (String n : deployableXapps)
			apps.add(new AppTransport(n));
		return apps;
	}

	@ApiOperation(value = "Returns the status of all deployed xapps.", response = AllDeployedXapps.class)
	@GetMapping(XAPPS_METHOD_PATH)
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public AllDeployedXapps getDeployedXapps(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey) {
		logger.debug("getDeployedXapps instance {}", instanceKey);
		return appManagerApiBuilder.getXappApi(instanceKey).getAllXapps();
	}

	@ApiOperation(value = "Returns the status of a given xapp.", response = Xapp.class)
	@GetMapping(XAPPS_METHOD_PATH + "/{" + PP_XAPP_NAME + "}")
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public Xapp getXapp(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey,
			@PathVariable(PP_XAPP_NAME) String appName) {
		logger.debug("getXapp instance {} name {}", instanceKey, appName);
		return appManagerApiBuilder.getXappApi(instanceKey).getXappByName(appName);
	}

	@ApiOperation(value = "Deploy a xapp.", response = Xapp.class)
	@PostMapping(XAPPS_METHOD_PATH)
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public Xapp deployXapp(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey, //
			@Validated @RequestBody XappDescriptor appDescriptor) {
		logger.debug("deployXapp instance {} descriptor {}", instanceKey, appDescriptor);
		return appManagerApiBuilder.getXappApi(instanceKey).deployXapp(appDescriptor);
	}

	@ApiOperation(value = "Undeploy an existing xapp.")
	@DeleteMapping(XAPPS_METHOD_PATH + "/{" + PP_XAPP_NAME + "}")
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public ResponseEntity<String> undeployXapp(@PathVariable(DashboardConstants.RIC_INSTANCE_KEY) String instanceKey,
			@PathVariable(PP_XAPP_NAME) String appName) {
		logger.debug("undeployXapp instance {} name {}", instanceKey, appName);
		XappApi api = appManagerApiBuilder.getXappApi(instanceKey);
		api.undeployXapp(appName);
		return ResponseEntity.status(api.getApiClient().getStatusCode().value()).body(null);
	}

}
