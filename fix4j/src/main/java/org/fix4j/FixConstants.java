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
 * I am a namespace for FIX protocol related constants used in the implementations of FixAdapter.
 *
 * @author vladyslav.yatsenko
 */
public final class FixConstants {
    public static final int BEGIN_STRING_TAG = 8;
    public static final int BODY_LENGTH_TAG = 9;
    public static final int MSG_TYPE_TAG = 35;
    public static final int CHECKSUM_TAG = 10;

    private FixConstants() {
    }
}
