/*
 * Copyright 2011 Tyler Blair. All rights reserved.
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

package com.griefcraft.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;

public class LWCBlockListener extends BlockListener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;

    public LWCBlockListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        LWCRedstoneEvent evt = new LWCRedstoneEvent(event, protection);
        lwc.getModuleLoader().dispatchEvent(evt);

        if (evt.isCancelled()) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);

        if (!canAccess) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        boolean ignoreBlockDestruction = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreBlockDestruction"));

        if (ignoreBlockDestruction) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);
        boolean canAdmin = lwc.canAdminProtection(player, protection);

        try {
            LWCProtectionDestroyEvent evt = new LWCProtectionDestroyEvent(player, protection, LWCProtectionDestroyEvent.Method.BLOCK_DESTRUCTION, canAccess, canAdmin);
            lwc.getModuleLoader().dispatchEvent(evt);

            if (evt.isCancelled()) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            event.setCancelled(true);
            lwc.sendLocale(player, "protection.internalerror", "id", "BLOCK_BREAK");
            e.printStackTrace();
        }
    }

    @Override
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block piston = event.getBlock();
        BlockState state = piston.getState();
        MaterialData data = state.getData();
        BlockFace direction = null;

        // Check the block it pushed directly
        if(data instanceof PistonBaseMaterial) {
            direction = ((PistonBaseMaterial) data).getFacing();
        }

        if (direction == null) {
            return;
        }

        // the block that the piston moved
        Block moved = piston.getRelative(direction, 2);

        if (lwc.findProtection(moved) != null) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block piston = event.getBlock();
        BlockState state = piston.getState();
        MaterialData data = state.getData();
        BlockFace direction = null;

        // Check the block it pushed directly
        if(data instanceof PistonBaseMaterial) {
            direction = ((PistonBaseMaterial) data).getFacing();
            Block block = event.getBlock().getRelative(direction);

            Protection protection = lwc.findProtection(block);

            if (protection != null) {
                event.setCancelled(true);
                return;
            }
        }

        // if no direction was found, no point in going on
        if (direction == null) {
            return;
        }

        // Check the affected blocks
        for (int i = 0; i < event.getLength() + 2; i++) {
            Block block = piston.getRelative(direction, i);
            Protection protection = lwc.findProtection(block);

            // We don't want that!
            if(block.getType() == Material.AIR) {
                break;
            }

            if (protection != null) {
                event.setCancelled(true);
                break;
            }
        }
    }

    /**
     * Used for auto registering placed protections
     */
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        // water exploit (placing 3+ chests in a row inside water, fire, etc)
        if (block.getType() == Material.CHEST) {
            if (lwc.findAdjacentDoubleChest(block) != null) {
                event.setCancelled(true);
                return;
            }
        }

        // The placable block must be protectable
        if (!lwc.isProtectable(block)) {
            return;
        }

        String autoRegisterType = plugin.getLWC().resolveProtectionConfiguration(block.getType(), "autoRegister");

        // is it auto registerable?
        if (/* !lwc.hasPermission(player, "lwc.autoprotect") && */ !autoRegisterType.equalsIgnoreCase("private") && !autoRegisterType.equalsIgnoreCase("public")) {
            return;
        }

        if (!lwc.hasPermission(player, "lwc.create." + autoRegisterType, "lwc.create", "lwc.protect")) {
            return;
        }

        // set the auto register type if they have the perm node
        if (lwc.hasPermission(player, "lwc.autoprotect") && false) {
            autoRegisterType = "private";
        }

        // default to public
        int type = ProtectionTypes.PUBLIC;

        if (autoRegisterType.equalsIgnoreCase("private")) {
            type = ProtectionTypes.PRIVATE;
        }

        // If it's a chest, make sure they aren't placing it beside an already registered chest
        if (block.getType() == Material.CHEST) {
            BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

            for (BlockFace blockFace : faces) {
                Block face = block.getRelative(blockFace);

                //They're placing it beside a chest, check if it's already protected
                if (face.getType() == Material.CHEST) {
                    if (lwc.getPhysicalDatabase().loadProtection(face.getWorld().getName(), face.getX(), face.getY(), face.getZ()) != null) {
                        return;
                    }
                }
            }
        }

        try {
            LWCProtectionRegisterEvent evt = new LWCProtectionRegisterEvent(player, block);
            lwc.getModuleLoader().dispatchEvent(evt);

            // something cancelled registration
            if (evt.isCancelled()) {
                return;
            }

            // All good!
            Protection protection = lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), type, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());
            lwc.sendLocale(player, "protection.onplace.create.finalize", "type", lwc.getLocale(autoRegisterType.toLowerCase()), "block", LWC.materialToString(block));

            if (protection != null) {
                lwc.getModuleLoader().dispatchEvent(new LWCProtectionRegistrationPostEvent(protection));
            }
        } catch (Exception e) {
            lwc.sendLocale(player, "protection.internalerror", "id", "PLAYER_INTERACT");
            e.printStackTrace();
        }
    }

}
