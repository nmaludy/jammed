package com.jammed.app;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author nmaludy
 */
public class MediaFilter implements FilenameFilter {

	public boolean accept(File file, String name) {
		return     name.endsWith(".aiff")
				  || name.endsWith(".avi")
				  || name.endsWith(".gsm")
				  || name.endsWith(".mid")
				  || name.endsWith(".mov")
				  || name.endsWith(".mpg")
				  || name.endsWith(".mp2")
				  || name.endsWith(".mvr")
				  || name.endsWith(".wav");
	}
}
