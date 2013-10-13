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

package org.fix4j;

/**
 * I am a runtime exception thrown whenever there is a problem during FixSerializer or FixAdapter execution.
 *
 * @author vladyslav.yatsenko
 */
public class FixException extends RuntimeException {
    private static final long serialVersionUID = 1;

    /**
     * @param tag the field tag that has not been found
     * @param fixMessage the FIX message that does not contain the tag
     * @return an instance of FixException for a field-not-found case.
     */
    public static FixException fieldNotFound(int tag, String fixMessage) {
        return new FixException("Field [" + tag + "] was not found in message: " + fixMessage);
    }

    /**
     * @param message the exception message
     * @param cause the exception cause
     */
    public FixException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the exception cause
     */
    public FixException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }

    /**
     * @param message the exception message
     */
    public FixException(final String message) {
        super(message);
    }
}
