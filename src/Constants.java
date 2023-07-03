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
interface Constants {
	
	// urls
	static final String iteroni = "http://iteroni.com/";
	
	static final String CONFIG_RECORD_NAME = "ytconfig";
	
	// Limits
	static final int TRENDS_LIMIT = 20;
	static final int SEARCH_LIMIT = 30;
	static final int LATESTVIDEOS_LIMIT = 20;
	
	static final String NAME = "JTube";
	
	static String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0";
	
	static final String VIDEO_EXTENDED_FIELDS = "title,videoId,author,authorId,description,publishedText,lengthSeconds,viewCount";
	static final String CHANNEL_EXTENDED_FIELDS = "author,authorId,description,subCount";
	static final String VIDEO_FIELDS = "title,videoId,author";
	static final String SEARCH_FIELDS = "title,authorId,videoId,author";
	
	static final int VIDEOFORM_AUTHOR_IMAGE_HEIGHT = 32;
	static final int AUTHORITEM_IMAGE_HEIGHT = 48;
	
	static String Path_separator = "/";

}
