/*
 * Copyright (c) 2011-2013 Tyler Blair
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package org.getlwc.roles;

import org.getlwc.Engine;
import org.getlwc.ProtectionRole;
import org.getlwc.entity.Player;
import org.getlwc.model.Protection;

public class PlayerProtectionRole extends ProtectionRole {

    public PlayerProtectionRole(Engine engine, Protection protection, String roleName, Access roleAccess) {
        super(engine, protection, roleName, roleAccess);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return 1; // adapted from LWCv4
    }

    /**
     * {@inheritDoc}
     */
    public Access getAccess(Protection protection, Player player) {
        if (player.getUUID().equalsIgnoreCase(getName())) {
            return getAccess();
        } else if (player.getName().equalsIgnoreCase(getName())) {
            setName(player.getUUID());
            save();
            return getAccess();
        } else {
            return Access.NONE;
        }
    }

}
