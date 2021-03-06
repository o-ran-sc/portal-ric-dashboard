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
package org.oransc.ric.portal.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

public class TestUtils {

	public static String readDataFromPath(String path) throws IOException {
		InputStream is = MethodHandles.lookup().lookupClass().getClassLoader().getResourceAsStream(path);
		if (is == null)
			throw new IOException("readDataFromPath: Failed to find resource on classpath: " + path);
		InputStreamReader reader = new InputStreamReader(is, "UTF-8");
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[16384];
		int i;
		while ((i = reader.read(buf)) > 0)
			sb.append(buf, 0, i);
		reader.close();
		is.close();
		return sb.toString();
	}

}
