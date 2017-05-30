/*
 * Copyright (C) 2017 MarkusWME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Updater.UpdateProviders;

import at.pcgamingfreaks.TestClasses.TestUtils;
import at.pcgamingfreaks.Updater.UpdateResult;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, JenkinsUpdateProvider.class })
public class JenkinsUpdateProviderTest
{
	@Test
	public void testQuery() throws Exception
	{
		int currentWarnings = 0;
		int currentSevere = 0;
		final int[] counts = { 0, 0 };
		Logger mockedLogger = mock(Logger.class);
		doAnswer(new Answer()
		{
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable
			{
				System.out.println(invocationOnMock.getArguments()[0]);
				counts[0]++;
				return null;
			}
		}).when(mockedLogger).warning(anyString());
		doAnswer(new Answer()
		{
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable
			{
				System.out.println(invocationOnMock.getArguments()[0]);
				counts[1]++;
				return null;
			}
		}).when(mockedLogger).severe(anyString());
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		assertEquals("The invalid query should return a failure", UpdateResult.FAIL_FILE_NOT_FOUND, updater.query());
		assertEquals("A warning should be shown", ++currentWarnings, counts[0]);
		updater = new JenkinsUpdateProvider("ci.pcgamingfreaks.at", "PluginLib", "", mockedLogger);
		assertEquals("The invalid query should return a failure", UpdateResult.FAIL_NO_VERSION_FOUND, updater.query());
		assertEquals("A warning should be shown", ++currentWarnings, counts[0]);
		updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", "", mockedLogger);
		assertEquals("The query should be successful", UpdateResult.SUCCESS, updater.query());
		assertEquals("No warning should be shown", currentWarnings, counts[0]);
		TestUtils.initReflection();
		URL mockedURL = PowerMockito.mock(URL.class);
		PowerMockito.doThrow(new IOException("HTTP response code: 403")).when(mockedURL).openConnection();
		Field urlField = TestUtils.setAccessible(JenkinsUpdateProvider.class, updater, "url", mockedURL);
		assertEquals("The query should return a failure", UpdateResult.FAIL_API_KEY, updater.query());
		currentSevere += 2;
		assertEquals("The logger should log the error", currentSevere, counts[1]);
		Field tokenField = TestUtils.setAccessible(JenkinsUpdateProvider.class, updater, "token", null);
		assertEquals("The query should return a failure", UpdateResult.FAIL_API_KEY, updater.query());
		currentSevere += 2;
		assertEquals("The logger should log the error", currentSevere, counts[1]);
		TestUtils.setUnaccessible(tokenField, updater, true);
		PowerMockito.doThrow(new IOException("")).when(mockedURL).openConnection();
		assertEquals("The query should return a offline message", UpdateResult.FAIL_SERVER_OFFLINE, updater.query());
		currentSevere += 2;
		assertEquals("The logger should log the error", currentSevere, counts[1]);
		TestUtils.setUnaccessible(urlField, updater, true);
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestVersionAsStringFailure() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestVersionAsString();
	}

	@Test
	public void testGetLatestVersionAsStringSuccess() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest version string should not be null", updater.getLatestVersionAsString());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestVersionFailure() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestVersion();
	}

	@Test
	public void testGetLatestVersionSuccess() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest version should not be null", updater.getLatestVersion());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestFileURLFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestFileURL();
	}

	@Test
	public void testGetLatestFileURLSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest file URL should not be null", updater.getLatestFileURL());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestVersionFileNameFailure() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestVersionFileName();
	}

	@Test
	public void testGetLatestVersionFileNameSuccess() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest version file name should not be null", updater.getLatestVersionFileName());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestNameFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestName();
	}

	@Test
	public void testGetLatestNameSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest version should not be null", updater.getLatestName());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestChecksumFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestChecksum();
	}

	@Test
	public void testGetLatestChecksumSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest checksum should not be null", updater.getLatestChecksum());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestChangelogFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestChangelog();
	}

	@Test
	public void testGetLatestChangelogSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest changelog should not be null", updater.getLatestChangelog());
	}

	@Test(expected = RequestTypeNotAvailableException.class)
	public void testGetLatestMinecraftVersion() throws RequestTypeNotAvailableException, NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.getLatestMinecraftVersion();
	}

	@Test(expected = RequestTypeNotAvailableException.class)
	public void testGetLatestDependencies() throws RequestTypeNotAvailableException, NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.getLatestDependencies();
	}

	@Test(expected = RequestTypeNotAvailableException.class)
	public void testGetUpdateHistory() throws RequestTypeNotAvailableException, NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.getUpdateHistory();
	}

	@Test
	public void testSettings()
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		assertTrue("The JenkinsUpdateProvider should provide a download URL", updater.provideDownloadURL());
		assertTrue("The JenkinsUpdateProvider should provide a changelog", updater.provideChangelog());
		assertTrue("The JenkinsUpdateProvider should provide a checksum", updater.provideMD5Checksum());
		assertFalse("The JenkinsUpdateProvider should not provide a Minecraft version", updater.provideMinecraftVersion());
		assertFalse("The JenkinsUpdateProvider should not provide a update history", updater.provideUpdateHistory());
		assertFalse("The JenkinsUpdateProvider should not provide dependencies", updater.provideDependencies());
		JenkinsUpdateProvider.class.getConstructors();
	}
}