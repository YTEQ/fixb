/*
 * Copyright 2013 YTEQ Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fix4j.test;

import static org.fix4j.impl.FormatConstants.SOH;

public class TestHelper {

    public static String fix(String... fields) {
        StringBuilder result = new StringBuilder();
        for (String field : fields) {
            if (result.length() > 0) result.append(SOH);
            result.append(field);
        }
        return result.toString();
    }
}
