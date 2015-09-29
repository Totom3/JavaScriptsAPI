package io.github.totom3.commons.command;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ICommandListener;
import net.minecraft.server.v1_8_R3.TileEntityCommand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

/**
 *
 * @author Totom3
 */
public final class CommandSenders {

    /**
     * Returns the {@code World} of a {@code CommandSender}, or throws an
     * exception if the said sender does not have one. If it is a
     * {@code Player}, the world it currently is in is returned. If it is a
     * {@code BlockCommandSender} (e.g. a command block), the world of the block
     * is returned.
     * <p>
     * @param sender the sender to get the world of.
     * <p>
     * @return the {@code World} the sender is in.
     * <p>
     *
     * @throws IllegalArgumentException if the sender does not have a world. In
     *                                  order to determine the latter, use
     *                                  {@link #hasWorld(org.bukkit.command.CommandSender)}.
     */
    public static World getWorld(CommandSender sender) {
	if (sender == null) {
	    throw new NullPointerException("CommandSender cannot be null");
	}

	if (sender instanceof Entity) {
	    return ((Entity) sender).getWorld();
	} else if (sender instanceof BlockCommandSender) {
	    return ((BlockCommandSender) sender).getBlock().getWorld();
	}

	throw new IllegalArgumentException("CommandSender " + sender + " does not have a world.");
    }

    /**
     * Returns whether or not the a {@code CommandSender} has a {@code World}. A
     * CommandSender is considered to have a world if it is not {@code null},
     * and if one of the following cases are matched:
     * <ul>
     * <li>It is an instance of {@link Entity} or of any of it's subclasses and
     * sub-interfaces;</li>
     * <li>It is an instance of {@link BlockCommandSender}, or any of it's
     * subclasses and sub-interfaces.</li>
     * </ul>
     * <p>
     * @param sender the sender to check.
     * <p>
     * @return {@code true} if the sender has a world, {@code false} otherwise.
     */
    public static boolean hasWorld(CommandSender sender) {
	return sender instanceof Entity || sender instanceof BlockCommandSender;
    }

    public static Location getLocation(CommandSender sender) {
	if (sender == null) {
	    throw new NullPointerException("CommandSender cannot be null");
	}

	if (sender instanceof Entity) {
	    return ((Entity) sender).getLocation();
	} else if (sender instanceof BlockCommandSender) {
	    return ((BlockCommandSender) sender).getBlock().getLocation();
	}

	throw new IllegalArgumentException("CommandSender " + sender + " does not have a location.");
    }

    public static ICommandListener toNMS(CommandSender sender) {
	if (sender == null) {
	    return null;
	}

	if (sender instanceof Entity) {
	    return ((CraftEntity) sender).getHandle();
	}

	if (sender instanceof BlockCommandSender) {
	    Block block = ((BlockCommandSender) sender).getBlock();
	    net.minecraft.server.v1_8_R3.World world = ((CraftWorld) block.getWorld()).getHandle();
	    TileEntityCommand tile = (TileEntityCommand) world.getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
	    return tile.getCommandBlock();
	}

	throw new IllegalArgumentException("cannot convert " + sender + " (" + sender.getClass().getName() + ") to NMS equivalent");
    }

    private CommandSenders() {
    }
}
