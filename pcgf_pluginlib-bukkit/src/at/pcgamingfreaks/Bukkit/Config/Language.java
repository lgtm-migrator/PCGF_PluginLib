/*
 *   Copyright (C) 2022 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Bukkit.Config;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Message.Sender.SendMethod;
import at.pcgamingfreaks.Config.LanguageWithMessageGetter;
import at.pcgamingfreaks.Plugin.IPlugin;
import at.pcgamingfreaks.Reflection;
import at.pcgamingfreaks.Version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Language extends LanguageWithMessageGetter
{
	static
	{
		messageClasses = new MessageClassesReflectionDataHolder(Reflection.getConstructor(Message.class, String.class), Reflection.getMethod(Message.class, "setSendMethod", SendMethod.class), SendMethod.class);
	}

	/**
	 * @param plugin  The instance of the plugin
	 * @param version The current version of the language file
	 */
	public Language(@NotNull IPlugin plugin, Version version)
	{
		super(plugin, version);
	}

	/**
	 * @param plugin           The instance of the plugin
	 * @param version          The current version of the language file
	 * @param path             The sub-folder for the language file
	 * @param prefix           The prefix for the language file
	 */
	public Language(@NotNull IPlugin plugin, Version version, @Nullable String path, @NotNull String prefix)
	{
		super(plugin, version, path, prefix);
	}

	/**
	 * @param plugin           The instance of the plugin
	 * @param version          The current version of the language file
	 * @param path             The sub-folder for the language file
	 * @param prefix           The prefix for the language file
	 * @param inJarPrefix      The prefix for the language file within the jar (e.g.: bukkit_)
	 */
	public Language(@NotNull IPlugin plugin, Version version, @Nullable String path, @NotNull String prefix, @NotNull String inJarPrefix)
	{
		super(plugin, version, path, prefix, inJarPrefix);
	}

	@SuppressWarnings("unchecked")
	@Override
	public @NotNull Message getMessage(@NotNull String path)
	{
		// Only returns null when the messageClasses variable is not set correctly. It is set in this class so this will not return null.
		return super.getMessage(path);
	}
}