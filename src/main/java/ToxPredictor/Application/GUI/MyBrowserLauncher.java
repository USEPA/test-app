package ToxPredictor.Application.GUI;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

import edu.stanford.ejalbert.BrowserLauncher;

public class MyBrowserLauncher {

	public static void launch(URI uri) {
		if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
//                System.out.println("desktop worked!");
                return;
            } catch (Exception ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        } else {

        	try {
        		BrowserLauncher launcher=new BrowserLauncher();
        		launcher.openURLinBrowser(uri.toURL().toString());
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			URL myURL=new URL("https://www.google.com/");
			MyBrowserLauncher.launch(myURL.toURI());
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
