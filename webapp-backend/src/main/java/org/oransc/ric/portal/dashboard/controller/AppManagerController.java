/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 * Copyright (C) 2019 AT&T Intellectual Property and Nokia
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oransc.ric.plt.appmgr.client.api.HealthApi;
import org.oransc.ric.plt.appmgr.client.api.XappApi;
import org.oransc.ric.plt.appmgr.client.model.AllDeployableXapps;
import org.oransc.ric.plt.appmgr.client.model.AllDeployedXapps;
import org.oransc.ric.plt.appmgr.client.model.AllXappConfig;
import org.oransc.ric.plt.appmgr.client.model.ConfigMetadata;
import org.oransc.ric.plt.appmgr.client.model.XAppConfig;
import org.oransc.ric.plt.appmgr.client.model.XAppInfo;
import org.oransc.ric.plt.appmgr.client.model.Xapp;
import org.oransc.ric.portal.dashboard.DashboardApplication;
import org.oransc.ric.portal.dashboard.DashboardConstants;
import org.oransc.ric.portal.dashboard.model.AppTransport;
import org.oransc.ric.portal.dashboard.model.DashboardDeployableXapps;
import org.oransc.ric.portal.dashboard.model.SuccessTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
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
 * Proxies calls from the front end to the App Manager API. All methods answer
 * 502 on failure: <blockquote>HTTP server received an invalid response from a
 * server it consulted when acting as a proxy or gateway.</blockquote>
 */
@Configuration
@RestController
@RequestMapping(value = AppManagerController.CONTROLLER_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class AppManagerController extends AbstractController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// Publish paths in constants so tests are easy to write
	public static final String CONTROLLER_PATH = DashboardConstants.ENDPOINT_PREFIX + "/appmgr";
	// Endpoints
	public static final String HEALTH_ALIVE_METHOD = "/health/alive";
	public static final String HEALTH_READY_METHOD = "/health/ready";
	public static final String CONFIG_METHOD = "/config";
	public static final String XAPPS_METHOD = "/xapps";
	public static final String XAPPS_LIST_METHOD = XAPPS_METHOD + "/list";
	public static final String VERSION_METHOD = DashboardConstants.VERSION_METHOD;
	// Path parameters
	public static final String PP_XAPP_NAME = "xAppName";

	// Populated by the autowired constructor
	private final HealthApi healthApi;
	private final XappApi xappApi;

	@Autowired
	public AppManagerController(final HealthApi healthApi, final XappApi xappApi) {
		Assert.notNull(healthApi, "health API must not be null");
		Assert.notNull(xappApi, "xapp API must not be null");
		this.healthApi = healthApi;
		this.xappApi = xappApi;
		if (logger.isDebugEnabled())
			logger.debug("ctor: configured with client types {} and {}", healthApi.getClass().getName(),
					xappApi.getClass().getName());
	}

	@ApiOperation(value = "Gets the XApp manager client library MANIFEST.MF property Implementation-Version.", response = SuccessTransport.class)
	@GetMapping(VERSION_METHOD)
	public SuccessTransport getClientVersion() {
		// No role requirement
		return new SuccessTransport(200, DashboardApplication.getImplementationVersion(HealthApi.class));
	}

	@ApiOperation(value = "Health check of xApp Manager - Liveness probe.")
	@GetMapping(HEALTH_ALIVE_METHOD)
	public void getHealth(HttpServletResponse response) {
		logger.debug("getHealthAlive");
		// No role requirement
		healthApi.getHealthAlive();
		response.setStatus(healthApi.getApiClient().getStatusCode().value());
	}

	@ApiOperation(value = "Readiness check of xApp Manager - Readiness probe.")
	@GetMapping(HEALTH_READY_METHOD)
	public void getHealthReady(HttpServletResponse response) {
		logger.debug("getHealthReady");
		// No role requirement
		healthApi.getHealthReady();
		response.setStatus(healthApi.getApiClient().getStatusCode().value());
	}

	@ApiOperation(value = "Returns the configuration of all xapps.", response = AllXappConfig.class)
	@GetMapping(CONFIG_METHOD)
	public AllXappConfig getAllXappConfig(HttpServletRequest request) {
		logger.debug("getAllXappConfig");
		checkRoles(request, DashboardConstants.USER_ROLE_UNPRIV, DashboardConstants.USER_ROLE_PRIV);
		return xappApi.getAllXappConfig();
	}

	@ApiOperation(value = "Create xApp config.", response = XAppConfig.class)
	@PostMapping(CONFIG_METHOD)
	public XAppConfig createXappConfig(HttpServletRequest request, //
			@RequestBody XAppConfig xAppConfig) {
		logger.debug("createXappConfig {}", xAppConfig);
		checkRoles(request, DashboardConstants.USER_ROLE_PRIV);
		return xappApi.createXappConfig(xAppConfig);
	}

	@ApiOperation(value = "Modify xApp config.", response = XAppConfig.class)
	@PutMapping(CONFIG_METHOD)
	public XAppConfig modifyXappConfig(HttpServletRequest request, //
			@RequestBody XAppConfig xAppConfig) {
		logger.debug("modifyXappConfig {}", xAppConfig);
		checkRoles(request, DashboardConstants.USER_ROLE_PRIV);
		return xappApi.modifyXappConfig(xAppConfig);
	}

	@ApiOperation(value = "Delete xApp configuration.")
	@DeleteMapping(CONFIG_METHOD + "/{" + PP_XAPP_NAME + "}")
	public void deleteXappConfig(HttpServletRequest request, //
			@RequestBody ConfigMetadata configMetadata, HttpServletResponse response) {
		logger.debug("deleteXappConfig {}", configMetadata);
		checkRoles(request, DashboardConstants.USER_ROLE_PRIV);
		xappApi.deleteXappConfig(configMetadata);
		response.setStatus(healthApi.getApiClient().getStatusCode().value());
	}

	@ApiOperation(value = "Returns a list of deployable xapps.", response = DashboardDeployableXapps.class)
	@GetMapping(XAPPS_LIST_METHOD)
	public Object getAvailableXapps(HttpServletRequest request) {
		logger.debug("getAvailableXapps");
		checkRoles(request, DashboardConstants.USER_ROLE_UNPRIV, DashboardConstants.USER_ROLE_PRIV);
		AllDeployableXapps appNames = xappApi.listAllXapps();
		// Answer a collection of structure instead of string
		// because I expect the AppMgr to be extended with
		// additional properties for each one.
		DashboardDeployableXapps apps = new DashboardDeployableXapps();
		for (String n : appNames)
			apps.add(new AppTransport(n));
		return apps;
	}

	@ApiOperation(value = "Returns the status of all deployed xapps.", response = AllDeployedXapps.class)
	@GetMapping(XAPPS_METHOD)
	public AllDeployedXapps getDeployedXapps(HttpServletRequest request) {
		logger.debug("getDeployedXapps");
		checkRoles(request, DashboardConstants.USER_ROLE_UNPRIV, DashboardConstants.USER_ROLE_PRIV);
		return xappApi.getAllXapps();
	}

	@ApiOperation(value = "Returns the status of a given xapp.", response = Xapp.class)
	@GetMapping(XAPPS_METHOD + "/{" + PP_XAPP_NAME + "}")
	public Xapp getXapp(HttpServletRequest request, //
			@PathVariable("xAppName") String xAppName) {
		logger.debug("getXapp {}", xAppName);
		checkRoles(request, DashboardConstants.USER_ROLE_UNPRIV, DashboardConstants.USER_ROLE_PRIV);
		return xappApi.getXappByName(xAppName);
	}

	@ApiOperation(value = "Deploy a xapp.", response = Xapp.class)
	@PostMapping(XAPPS_METHOD)
	public Xapp deployXapp(HttpServletRequest request, //
			@RequestBody XAppInfo xAppInfo) {
		logger.debug("deployXapp {}", xAppInfo);
		checkRoles(request, DashboardConstants.USER_ROLE_PRIV);
		return xappApi.deployXapp(xAppInfo);
	}

	@ApiOperation(value = "Undeploy an existing xapp.")
	@DeleteMapping(XAPPS_METHOD + "/{" + PP_XAPP_NAME + "}")
	public void undeployXapp(HttpServletRequest request, //
			@PathVariable("xAppName") String xAppName, // .
			HttpServletResponse response) {
		logger.debug("undeployXapp {}", xAppName);
		checkRoles(request, DashboardConstants.USER_ROLE_PRIV);
		xappApi.undeployXapp(xAppName);
		response.setStatus(healthApi.getApiClient().getStatusCode().value());
	}

}
