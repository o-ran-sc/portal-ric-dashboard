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
import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.oransc.ric.portal.dashboard.DashboardConstants;
import org.oransc.ric.portal.dashboard.config.RICInstanceMockConfiguration;
import org.oransc.ric.portal.dashboard.model.DashboardDeployableXapps;
import org.oransc.ric.portal.dashboard.model.SuccessTransport;
import org.oransc.ricplt.appmgr.client.model.AllDeployedXapps;
import org.oransc.ricplt.appmgr.client.model.AllXappConfig;
import org.oransc.ricplt.appmgr.client.model.XAppConfig;
import org.oransc.ricplt.appmgr.client.model.Xapp;
import org.oransc.ricplt.appmgr.client.model.XappDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

public class AppManagerControllerTest extends AbstractControllerTest {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Test
	public void versionTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.VERSION_METHOD);
		logger.info("Invoking {}", uri);
		SuccessTransport st = restTemplate.getForObject(uri, SuccessTransport.class);
		Assertions.assertFalse(st.getData().toString().isEmpty());
	}

	@Test
	public void healthAliveTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.HEALTH_ALIVE_METHOD);
		logger.info("Invoking {}", uri);
		ResponseEntity<Void> voidResponse = restTemplate.getForEntity(uri, Void.class);
		Assertions.assertTrue(voidResponse.getStatusCode().is2xxSuccessful());
	}

	@Test
	public void healthReadyTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.HEALTH_READY_METHOD);
		logger.info("Invoking {}", uri);
		ResponseEntity<Void> voidResponse = restTemplate.getForEntity(uri, Void.class);
		Assertions.assertTrue(voidResponse.getStatusCode().is2xxSuccessful());
	}

	@Test
	public void appListTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.XAPPS_LIST_METHOD);
		logger.info("Invoking {}", uri);
		DashboardDeployableXapps apps = testRestTemplateStandardRole().getForObject(uri,
				DashboardDeployableXapps.class);
		Assertions.assertFalse(apps.isEmpty());
	}

	@Test
	public void appStatusesTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.XAPPS_METHOD);
		logger.info("Invoking {}", uri);
		AllDeployedXapps apps = testRestTemplateStandardRole().getForObject(uri, AllDeployedXapps.class);
		Assertions.assertFalse(apps.isEmpty());
	}

	@Test
	public void appStatusTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.XAPPS_METHOD, "app");
		logger.info("Invoking {}", uri);
		Xapp app = testRestTemplateStandardRole().getForObject(uri, Xapp.class);
		Assertions.assertFalse(app.getName().isEmpty());
	}

	@Test
	public void deployAppTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.XAPPS_METHOD);
		logger.info("Invoking {}", uri);
		XappDescriptor descr = new XappDescriptor().xappName("app");
		Xapp app = testRestTemplateAdminRole().postForObject(uri, descr, Xapp.class);
		Assertions.assertNotNull(app);
		Assertions.assertFalse(app.getName().isEmpty());

		// An invalid request must be rejected
		Assertions.assertThrows(RestClientException.class, () -> {
			testRestTemplateAdminRole().postForObject(uri, new XappDescriptor(), Xapp.class);
		});
	}

	@Test
	public void undeployAppTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.XAPPS_METHOD, "app");
		logger.info("Invoking {}", uri);
		ResponseEntity<Void> voidResponse = testRestTemplateAdminRole().exchange(uri, HttpMethod.DELETE, null,
				Void.class);
		Assertions.assertTrue(voidResponse.getStatusCode().is2xxSuccessful());
	}

	@Test
	public void getConfigTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.CONFIG_METHOD);
		logger.info("Invoking {}", uri);
		AllXappConfig config = testRestTemplateStandardRole().getForObject(uri, AllXappConfig.class);
		Assertions.assertFalse(config.isEmpty());
	}

	@Test
	public void modifyConfigTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY,
				RICInstanceMockConfiguration.INSTANCE_KEY_1, AppManagerController.CONFIG_METHOD);
		logger.info("Invoking {}", uri);
		XAppConfig modConfig = new XAppConfig();
		HttpEntity<XAppConfig> entity = new HttpEntity<>(modConfig);
		ResponseEntity<Void> voidResponse = testRestTemplateAdminRole().exchange(uri, HttpMethod.PUT, entity,
				Void.class);
		Assertions.assertTrue(voidResponse.getStatusCode().is2xxSuccessful());
	}

	@Test
	public void invalidInstanceTest() {
		URI uri = buildUri(null, AppManagerController.CONTROLLER_PATH, DashboardConstants.RIC_INSTANCE_KEY, "bogus",
				AppManagerController.CONFIG_METHOD);
		logger.info("Invoking {}", uri);
		ResponseEntity<Void> voidResponse = testRestTemplateAdminRole().exchange(uri, HttpMethod.GET, null, Void.class);
		Assertions.assertTrue(voidResponse.getStatusCode().is5xxServerError());
	}

}
