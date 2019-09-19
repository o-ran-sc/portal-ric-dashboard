/*-
 * ========================LICENSE_START=================================
 * O-RAN-SC
 * %%
 * Copyright (C) 2019 AT&T Intellectual Property
 * Modifications Copyright (C) 2019 Nordix Foundation
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.oransc.ric.a1med.client.api.A1MediatorApi;
import org.oransc.ric.a1med.client.model.PolicyTypeSchema;
import org.oransc.ric.portal.dashboard.DashboardApplication;
import org.oransc.ric.portal.dashboard.DashboardConstants;
import org.oransc.ric.portal.dashboard.model.PolicyInstance;
import org.oransc.ric.portal.dashboard.model.PolicyInstances;
import org.oransc.ric.portal.dashboard.model.SuccessTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Proxies calls from the front end to the A1 Mediator API to get and put
 * policies. The first application managed via this path is Admission Control.
 * 
 * If a method throws RestClientResponseException, it is handled by
 * {@link CustomResponseEntityExceptionHandler#handleProxyMethodException(Exception, org.springframework.web.context.request.WebRequest)}
 * which returns status 502. All other exceptions are handled by Spring which
 * returns status 500.
 */
@RestController
@RequestMapping(value = A1MediatorController.CONTROLLER_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class A1MediatorController {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	// Publish paths in constants so tests are easy to write
	public static final String CONTROLLER_PATH = DashboardConstants.ENDPOINT_PREFIX + "/a1-p";
	// Endpoints
	public static final String VERSION_METHOD = DashboardConstants.VERSION_METHOD;
	// Path parameters
	public static final String PP_POLICIES = "policies";
	public static final String POLICY_TYPES_METHOD = "policytypes";
	public static final String POLICY_TYPE_ID_NAME = "policy_type_id";
	public static final String POLICIES_NAME = "policies";
	public static final String POLICY_INSTANCE_ID_NAME = "policy_instance_id";

	// Populated by the autowired constructor
	private final A1MediatorApi a1MediatorApi;

	@Autowired
	public A1MediatorController(final A1MediatorApi a1MediatorApi) {
		Assert.notNull(a1MediatorApi, "API must not be null");
		this.a1MediatorApi = a1MediatorApi;
		if (logger.isDebugEnabled())
			logger.debug("ctor: configured with client type {}", a1MediatorApi.getClass().getName());
	}

	@ApiOperation(value = "Gets the A1 client library MANIFEST.MF property Implementation-Version.", response = SuccessTransport.class)
	@GetMapping(VERSION_METHOD)
	// No role required
	public SuccessTransport getA1MediatorClientVersion() {
		return new SuccessTransport(200, DashboardApplication.getImplementationVersion(A1MediatorApi.class));
	}

	/*
	 * This method is deliberately kept ignorant of the data passing thru.
	 */
	@ApiOperation(value = "Gets the specified policy from the A1 Mediator")
	@GetMapping(PP_POLICIES + "/{" + PP_POLICIES + "}")
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public Object getPolicy(@PathVariable(PP_POLICIES) String policyName) {
		logger.debug("getPolicy {}", policyName);
		return null;//a1MediatorApi.a1ControllerGetHandler(policyName); Commented out due to the uplift of the A1 specification to latest version.
	}

	/*
	 * This method is deliberately kept ignorant of the data passing thru.
	 */
	@ApiOperation(value = "Puts the specified policy to the A1 Mediator")
	@PutMapping(PP_POLICIES + "/{" + PP_POLICIES + "}")
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public void putPolicy(@PathVariable(PP_POLICIES) String policyName,
			@ApiParam(value = "Policy body") @RequestBody String policy, //
			HttpServletResponse response) {
		logger.debug("putPolicy name {} value {}", policyName, policy);
//		a1MediatorApi.a1ControllerPutHandler(policyName, policy); Commented out due to the uplift of the A1 specification to latest version.
		response.setStatus(a1MediatorApi.getApiClient().getStatusCode().value());
	}

	/*
	 * The fields are defined in the A1Control Typescript interface.
	 */
	@ApiOperation(value = "Gets the policy types from Near Realtime-RIC via the A1 Controller API")
	@GetMapping(POLICY_TYPES_METHOD)
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public Object getAllPolicyTypes(HttpServletResponse response) {
		logger.debug("getAllPolicyTypes");
		List<Integer> policyTypeIds = a1MediatorApi.a1ControllerGetAllPolicyTypes();
		List<PolicyTypeSchema> policyTypes = new ArrayList<>();
		for (Integer policyTypeId : policyTypeIds) {
			PolicyTypeSchema policyTypeSchema = a1MediatorApi.a1ControllerGetPolicyType(policyTypeId);
			policyTypes.add(policyTypeSchema);
		}
		return policyTypes;
	}

	@ApiOperation(value = "Returns the policy instances for the given policy type.")
	@GetMapping(POLICY_TYPES_METHOD + "/{" + POLICY_TYPE_ID_NAME + "}/" + POLICIES_NAME)
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public Object getPolicyInstances(@PathVariable(POLICY_TYPE_ID_NAME) Integer policyTypeId) {
		logger.debug("getPolicyInstances {}", policyTypeId);
		List<String> instancesForType = a1MediatorApi.a1ControllerGetAllInstancesForType(policyTypeId);
		PolicyInstances instances = new PolicyInstances();
		for (String instanceId : instancesForType) {
			Object policyInstance = a1MediatorApi.a1ControllerGetPolicyInstance(policyTypeId, instanceId);
			PolicyInstance instance = new PolicyInstance(instanceId, policyInstance);
			instances.add(instance);
		}
		return instances;
	}

	@ApiOperation(value = "Returns a policy instance of a type")
	@GetMapping(POLICY_TYPES_METHOD + "/{" + POLICY_TYPE_ID_NAME + "}/" + POLICIES_NAME + "/{" + POLICY_INSTANCE_ID_NAME
			+ "}")
	@Secured({ DashboardConstants.ROLE_ADMIN, DashboardConstants.ROLE_STANDARD })
	public Object getPolicyInstance(@PathVariable(POLICY_TYPE_ID_NAME) Integer policyTypeId, @PathVariable(POLICY_INSTANCE_ID_NAME) String policyInstanceId) {
		logger.debug("getPolicyInstance {}:{}", policyTypeId, policyInstanceId);
		return a1MediatorApi.a1ControllerGetPolicyInstance(policyTypeId, policyInstanceId);
	}

	@ApiOperation(value = "Creates the policy instances for the given policy type.")
	@PutMapping(POLICY_TYPES_METHOD + "/{" + POLICY_TYPE_ID_NAME + "}/" + POLICIES_NAME + "/{" + POLICY_INSTANCE_ID_NAME
			+ "}")
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public void putPolicyInstance(@PathVariable(POLICY_TYPE_ID_NAME) Integer policyTypeId,
			@PathVariable(POLICY_INSTANCE_ID_NAME) String policyInstanceId, @RequestBody String instance) {
		logger.debug("putPolicyInstance typeId: {}, instanceId: {}, instance: {}", policyTypeId, policyInstanceId, instance);
		a1MediatorApi.a1ControllerCreateOrReplacePolicyInstance(policyTypeId, policyInstanceId, instance);
	}

	@ApiOperation(value = "Deletes the policy instances for the given policy type.")
	@DeleteMapping(POLICY_TYPES_METHOD + "/{" + POLICY_TYPE_ID_NAME + "}/" + POLICIES_NAME + "/{" + POLICY_INSTANCE_ID_NAME
			+ "}")
	@Secured({ DashboardConstants.ROLE_ADMIN })
	public void deletePolicyInstance(@PathVariable(POLICY_TYPE_ID_NAME) Integer policyTypeId,
			@PathVariable(POLICY_INSTANCE_ID_NAME) String policyInstanceId) {
		logger.debug("deletePolicyInstance typeId: {}, instanceId: {}", policyTypeId, policyInstanceId);
		a1MediatorApi.a1ControllerDeletePolicyInstance(policyTypeId, policyInstanceId);
	}
}
