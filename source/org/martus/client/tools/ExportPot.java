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
package org.martus.client.tools;

import java.io.File;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.SortedSet;

import org.martus.client.core.MartusApp;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiConstants;
import org.martus.client.swingui.UiSession;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.MiniLocalization;
import org.martus.common.i18n.TranslationEntry;


public class ExportPot
{
	public static void main(String[] args) throws Exception
	{
		new ExportPot().exportPot();
	}

	public ExportPot() throws Exception
	{
		File translationsDirectory = MartusApp.getTranslationsDirectory();
		String[] allEnglishStrings = UiSession.getAllEnglishStrings();
		localization = new MartusLocalization(translationsDirectory, allEnglishStrings);
	}
	
	public void exportPot() throws Exception
	{
		exportHeader(System.out);
		exportEntries(System.out);
	}

	private void exportHeader(PrintStream out)
	{
		LocalDate now = LocalDate.now();
		String today = DateTimeFormatter.ISO_LOCAL_DATE.format(now);
		out.println("msgid \"\"");
		out.println("msgstr \"\"");
		write_quoted(out, "Project-Id-Version: Martus " + UiConstants.versionLabel + "\\n");
		write_quoted(out, "Report-Msgid-Bugs-To: martus@benetech.org\\n");
		write_quoted(out, "POT-Creation-Date: " + today + "\\n");
		write_quoted(out, "MIME-Version: 1.0\\n");
		write_quoted(out, "Content-Type: text/plain; charset=UTF-8\\n");
		write_quoted(out, "Content-Transfer-Encoding: 8bit\\n");
		//#write_quoted output, "Plural-Forms: nplurals=2; plural=(n != 1);\\n"
		out.println();
		out.println();
		
	}

	private void write_quoted(PrintStream out, String string)
	{
		out.println('"' + string + '"');
	}

	public void exportEntries(PrintStream out)
	{
		SortedSet<String> strings = localization.getAllKeysSorted();
		for (String key : strings)
		{
			String english = localization.getLabel(MiniLocalization.ENGLISH, key);
			String hex = localization.getHashOfEnglish(key);
			
			TranslationEntry entry = new TranslationEntry();
			entry.append(TranslationEntry.MSGID, english);
			entry.append(TranslationEntry.MSGCTXT, key);
			entry.setHex(hex);
			
			out.println("#: " + entry.getHex());
			out.print("msgctxt ");
			write_quoted(out, entry.getContext());
			out.print("msgid ");
			write_quoted(out, "");
			write_quoted(out, entry.getMsgid());
			out.println();
		}
	}
	
	private MtfAwareLocalization localization;
}
