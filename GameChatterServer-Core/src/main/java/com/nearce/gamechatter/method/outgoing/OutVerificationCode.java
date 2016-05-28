/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.method.outgoing;

public class OutVerificationCode {
    private String code;

    private OutVerificationCode(String code) {
        this.code = code;
    }

    public static OutMethod getRequest(String code) {
        return new OutMethod("verify", new OutVerificationCode(code));
    }
}
