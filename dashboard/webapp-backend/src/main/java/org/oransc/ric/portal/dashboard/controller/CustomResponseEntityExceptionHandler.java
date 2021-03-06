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

import org.oransc.ric.portal.dashboard.exception.InvalidArgumentException;
import org.oransc.ric.portal.dashboard.exception.StatsManagerException;
import org.oransc.ric.portal.dashboard.exception.UnknownInstanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Catches certain exceptions. This controller advice factors out try-catch
 * blocks in many controller methods.
 * 
 * Also see:<br>
 * https://www.baeldung.com/exception-handling-for-rest-with-spring
 * https://www.springboottutorial.com/spring-boot-exception-handling-for-rest-services
 */
@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	// Superclass has "logger" that is exposed here, so use a different name
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Generates a message with an upper limit on size.
	 * 
	 * @param t
	 *              Throwable
	 * @return Message
	 */
	private String getShortExceptionMessage(Throwable t) {
		final int enough = 256;
		String exString = t.toString();
		return exString.length() > enough ? exString.substring(0, enough) : exString;
	}

	/**
	 * Logs the error and generates a JSON response when a REST controller method
	 * takes a RestClientResponseException. This is thrown by the Http client when a
	 * remote method returns a non-2xx code. All the controller methods are proxies
	 * in that they just forward the request along to a remote system, so if that
	 * remote system fails, return 502 plus some details about the failure, rather
	 * than the generic 500 that Spring-Boot will return on an uncaught exception.
	 * 
	 * Why 502? I quote: <blockquote>HTTP server received an invalid response from a
	 * server it consulted when acting as a proxy or gateway.</blockquote>
	 * 
	 * @param ex
	 *                    The exception
	 * 
	 * @param request
	 *                    The original request
	 * 
	 * @return A response entity with status code 502 and an unstructured message.
	 */
	@ExceptionHandler({ RestClientResponseException.class })
	public final ResponseEntity<String> handleRestClientResponse(Exception ex, WebRequest request) {
		// Capture the full stack trace in the log.
		if (log.isErrorEnabled())
			log.error("handleRestClientResponse: request " + request.getDescription(false), ex);
		if (ex instanceof HttpStatusCodeException) {
			HttpStatusCodeException hsce = (HttpStatusCodeException) ex;
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(hsce.getResponseBodyAsString());
		} else {
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(getShortExceptionMessage(ex));
		}
	}

	/**
	 * Logs the error and generates a response when a REST controller method takes
	 * an UnknownInstanceException, an invalid RIC instance key was used.
	 * 
	 * @param ex
	 *                    The exception
	 * @param request
	 *                    The original request
	 * @return A response entity with status code 400 and an unstructured message.
	 */
	@ExceptionHandler({ UnknownInstanceException.class })
	public final ResponseEntity<String> handleUnknownInstance(Exception ex, WebRequest request) {
		log.warn("handleUnknownInstance: request {}, exception {}", request.getDescription(false), ex.toString());
		return ResponseEntity.badRequest().body(getShortExceptionMessage(ex));
	}

	/**
	 * Logs the error and generates a response when a REST controller method takes
	 * an InvalidArgumentException, an invalid JSON was sent.
	 * 
	 * @param ex
	 *                    The exception
	 * @param request
	 *                    The original request
	 * @return A response entity with status code 400 and an unstructured message.
	 */
	@ExceptionHandler({ InvalidArgumentException.class })
	public final ResponseEntity<String> handleInvalidArgument(Exception ex, WebRequest request) {
		log.warn("handleInvalidArgument: request {}, exception {}", request.getDescription(false), ex.toString());
		return ResponseEntity.badRequest().body(getShortExceptionMessage(ex));
	}

	/**
	 * Logs the error and generates a response when a REST controller method takes
	 * an StatsManagerException.
	 * 
	 * @param ex
	 *                    The exception
	 * @param request
	 *                    The original request
	 * @return A response entity with status code 400 and an unstructured message.
	 */
	@ExceptionHandler({ StatsManagerException.class })
	public final ResponseEntity<String> handleStatsManager(Exception ex, WebRequest request) {
		log.warn("handleStatsManager: request {}, exception {}", request.getDescription(false), ex.toString());
		return ResponseEntity.badRequest().body(getShortExceptionMessage(ex));
	}

}
