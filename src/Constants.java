/*
Copyright (c) 2022 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
import javax.microedition.lcdui.Command;

public interface Constants extends LocaleConstants {
	
	// urls
	static final String iteroni = "http://iteroni.com/";
	static final String streamphp = "http://nnp.nnchan.ru/stream.php?url=";
	
	static final String CONFIG_RECORD_NAME = "ytconfig";
	
	// Main form commands
	static final Command settingsCmd = new Command(Locale.s(CMD_Settings), Command.SCREEN, 4);
	static final Command idCmd = new Command(Locale.s(CMD_OpenByID), Command.SCREEN, 8);
	static final Command searchCmd = new Command(Locale.s(CMD_Search), Command.SCREEN, 2);
	static final Command aboutCmd = new Command(Locale.s(CMD_About), Command.SCREEN, 10);
	static final Command switchToPopularCmd = new Command(Locale.s(CMD_SwitchToPopular), Command.SCREEN, 3);
	static final Command switchToTrendsCmd = new Command(Locale.s(CMD_SwitchToTrends), Command.SCREEN, 3);
	
	static final Command searchOkCmd = new Command(Locale.s(CMD_Search), Command.OK, 1);
	static final Command exitCmd = new Command(Locale.s(CMD_Exit), Command.EXIT, 2);
	static final Command goCmd = new Command(Locale.s(CMD_Go), Command.OK, 1);
	static final Command cancelCmd = new Command(Locale.s(CMD_Cancel), Command.CANCEL, 2);
	static final Command backCmd = new Command(Locale.s(CMD_Back), Command.BACK, 1);
	
	static final Command applyCmd = new Command(Locale.s(CMD_Apply), Command.BACK, 1);
	
	// Video page commands
	static final Command watchCmd = new Command(Locale.s(CMD_Watch), Command.OK, 10);
	static final Command downloadCmd = new Command(Locale.s(CMD_Download), Command.SCREEN, 9);
	//static final Command browserCmd = new Command("Open with browser", Command.SCREEN, 3);
	
	// Downloader alert commands
	static final Command dlOkCmd = new Command(Locale.s(CMD_OK), Command.CANCEL, 1);
	//static final Command dlWatchCmd = new Command("Watch", Command.SCREEN, 2);
	static final Command dlCancelCmd = new Command(Locale.s(CMD_Cancel), Command.CANCEL, 1);
	
	public static Command vOpenCmd = new Command(Locale.s(CMD_View), Command.ITEM, 10);
	public static Command vOpenChannelCmd = new Command(Locale.s(CMD_ViewChannel), Command.ITEM, 4);
	
	// Limits
	static final int TRENDS_LIMIT = 20;
	static final int SEARCH_LIMIT = 30;
	static final int LATESTVIDEOS_LIMIT = 20;
	
	static final String NAME = "JTube";
	
	public static String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0";
	
	static final String VIDEO_EXTENDED_FIELDS = "title,videoId,author,authorId,description,publishedText,lengthSeconds,viewCount";
	static final String CHANNEL_EXTENDED_FIELDS = "author,authorId,description";
	static final String VIDEO_FIELDS = "title,videoId,author";
	static final String SEARCH_FIELDS = "title,authorId,videoId,author";
	
	public static final int VIDEOFORM_AUTHOR_IMAGE_HEIGHT = 32;
	public static final int AUTHORITEM_IMAGE_HEIGHT = 48;
	
	public static String Path_separator = "/";

}
