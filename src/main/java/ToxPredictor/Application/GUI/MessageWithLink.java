package ToxPredictor.Application.GUI;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;


import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.stanford.ejalbert.BrowserLauncher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MessageWithLink extends JEditorPane {

	BrowserLauncher launcher = null;
	
	private static final long serialVersionUID = 1L;

    public MessageWithLink(String htmlBody) {
        super("text/html", "<html><body style=\"" + getStyle() + "\">" + htmlBody + "</body></html>");
        
        try {
        	launcher=new BrowserLauncher(null);
        } catch (Exception ex) {
        	
        }
        
        addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    // Process the click event on the link (for example with java.awt.Desktop.getDesktop().browse())

                	try {
                		MyBrowserLauncher.launch(e.getURL().toURI());
                	} catch (Exception ex) {
                		ex.printStackTrace();
                	}
                }
            }
        });
        setEditable(false);
        setBorder(null);
    }

    static StringBuffer getStyle() {
        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();
        Color color = label.getBackground();

        // create some css from the label's font
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");
        style.append("background-color: rgb("+color.getRed()+","+color.getGreen()+","+color.getBlue()+");");
        return style;
    }
    
}