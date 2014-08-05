/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.client.core.templates;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

import org.martus.common.FieldSpecCollection;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

public class TestFormTemplateManager extends TestCaseEnhanced
{
	public TestFormTemplateManager(String name)
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		
		security = MockMartusSecurity.createClient();
	}
	
	public void testCreateFails() throws Exception
	{
		File badDirectory = createTempFile();
		try
		{
			FormTemplateManager.openExisting(security, badDirectory);
			fail("Should have thrown for not a directory");
		}
		catch(FileNotFoundException ignoreExpected)
		{
		}
		finally
		{
			badDirectory.delete();
		}
		
		try
		{
			FormTemplateManager.openExisting(security, badDirectory);
			fail("Should have thrown for not existing");
		}
		catch(FileNotFoundException ignoreExpected)
		{
		}
	}
	
	public void testCreateNewDirectoryWithoutExisting() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createNewDirectory(security, templateDirectory, null);
			
			Set<String> names = manager.getAvailableTemplateNames();
			assertEquals(1, names.size());
			String onlyTemplateName = names.iterator().next();
			assertEquals(FormTemplateManager.MARTUS_DEFAULT_FORM_TEMPLATE_NAME, onlyTemplateName);
			
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}

	public void testCreateNewDirectoryWithExisting() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			String title = "title";
			String description = "description";
			FormTemplate template = createFormTemplate(title, description);
			FormTemplateManager manager = FormTemplateManager.createNewDirectory(security, templateDirectory, template);
			
			Set<String> names = manager.getAvailableTemplateNames();
			assertEquals(2, names.size());
			
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}

	public void testSaveAndLoad() throws Exception
	{
		File tempDirectory = createTempDirectory();
		try
		{
			File templateDirectory = new File(tempDirectory, "templates");
			FormTemplateManager manager = FormTemplateManager.createNewDirectory(security, templateDirectory, null);
			
			assertEquals(1, manager.getAvailableTemplateNames().size());
			FormTemplate template = createFormTemplate("t1", "d1");
			manager.putTemplate(template);
			assertEquals(2, manager.getAvailableTemplateNames().size());
			FormTemplate got = manager.getTemplate(template.getTitle());
			assertEquals(template.getDescription(), got.getDescription());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}
	
	private FormTemplate createFormTemplate(String title, String description) throws Exception
	{
		FieldSpecCollection top = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection bottom = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		FormTemplate template = new FormTemplate(title, description, top, bottom);
		return template;
	}
	
	public MockMartusSecurity security;
}

