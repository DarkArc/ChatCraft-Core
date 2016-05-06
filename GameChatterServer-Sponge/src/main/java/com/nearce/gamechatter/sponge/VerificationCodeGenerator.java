/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge;

import java.util.UUID;

public class VerificationCodeGenerator {
    public static String generate() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
