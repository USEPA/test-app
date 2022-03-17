package ToxPredictor.Application.model;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimilarChemical {

    
    private String DSSTOXSID;
    private String DSSTOXCID;
    public String getDSSTOXCID() {
		return DSSTOXCID;
	}

	public void setDSSTOXCID(String dSSTOXCID) {
		DSSTOXCID = dSSTOXCID;
	}

	private String CAS;
    private String backgroundColor;
    private String similarityCoefficient;
    private String expVal;
    private String predVal;
    private String imageUrl;


    public String getDSSTOXSID() {
        return DSSTOXSID;
    }

    @JsonProperty("DSSTOXSID")
    public void setDSSTOXSID(String DSSTOXSID) {
        this.DSSTOXSID = DSSTOXSID;
    }

    public String getCAS() {
        return CAS;
    }

    @JsonProperty("CAS")
    public void setCAS(String CAS) {
        this.CAS = CAS;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getSimilarityCoefficient() {
        return similarityCoefficient;
    }

    public void setSimilarityCoefficient(String similarityCoefficient) {
        this.similarityCoefficient = similarityCoefficient;
    }

    public String getExpVal() {
        return expVal;
    }

    public void setExpVal(String expVal) {
        this.expVal = expVal;
    }

    public String getPredVal() {
        return predVal;
    }

    public void setPredVal(String predVal) {
        this.predVal = predVal;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    
    public static ImageIcon decodeBase64ToImageIcon(String imageString, int size) {
    	 
        BufferedImage image = null;
        byte[] imageByte;
        try {
//        	System.out.println(imageString);
            imageByte = Base64.getDecoder().decode(imageString.replace("data:image/png;base64, ", ""));
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            
            int heightOriginal=image.getHeight();
            int heightFinal=(int)((double)size/(double)image.getWidth()*heightOriginal);
            
            Image newimg = image.getScaledInstance(size, heightFinal,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
            bis.close();
            return new ImageIcon(newimg);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }        
    }
    
    
    public static ImageIcon urlToImageIcon(String url, int size) {
    	
    	try {
			ImageIcon imageIcon = new ImageIcon(new URL(url));
			Image image = imageIcon.getImage(); // transform it 
			Image newimg = image.getScaledInstance(size, size,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
			imageIcon = new ImageIcon(newimg);  // transform it back
			return imageIcon;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			System.out.println(url);
			return null;
		}
    }
    
	public LinkedHashMap<String, Object> convertToLinkedHashMap(JTable table) {
		// TODO Auto-generated method stub
		LinkedHashMap<String,Object> lhm=new LinkedHashMap<>();
		lhm.put("CAS",CAS);
				
		
		int size=table.getColumnModel().getColumn(1).getWidth();
//		System.out.println(size);
		
		
		if (imageUrl.contains("base64")) {			
			lhm.put("Structure",decodeBase64ToImageIcon(imageUrl,size));			
		} else {
			lhm.put("Structure",urlToImageIcon(imageUrl,size));	
		}
						
//		lhm.put("Structure",imageUrl);//previously we stored just url instead of imageIcon- which made scrolling the table slow
		
		
		lhm.put("Similarity", similarityCoefficient);
		lhm.put("Experimental value", expVal);
		lhm.put("Predicted value", predVal);
		lhm.put("backgroundColor", backgroundColor);
		return lhm;
	}
}
