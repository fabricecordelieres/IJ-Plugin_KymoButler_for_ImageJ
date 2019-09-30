import java.io.File;

import ij.ImageJ;

public class test_KB {

	public static void main(String[] args) {
		System.getProperties().setProperty("plugins.dir", System.getProperty("user.dir")+File.separator+"target"+File.separator);
        ImageJ ij=new ImageJ();
        ij.exitWhenQuitting(true);
	}

}
