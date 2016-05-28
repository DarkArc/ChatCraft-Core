/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.method.outgoing;

public class OutLeave {
    private String name;
    private boolean remote;

    private OutLeave(String name, boolean remote) {
        this.name = name;
        this.remote = remote;
    }

    public static OutMethod getRequest(String name, boolean remote) {
        return new OutMethod("leave", new OutLeave(name, remote));
    }
}
