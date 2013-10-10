package com.example.pfc_alpha1;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ParkingHandler extends DefaultHandler {
	
	private List<ParkingMarker> parkings;
	private ParkingMarker parkingActual;
    private StringBuilder sbText;
    public Boolean parsingError = false;
	
	

	public List<ParkingMarker> getParkings(){
		return parkings;
	}
	
	
	@Override
    public void startDocument() throws SAXException {
 
        super.startDocument();
 
        parkings = new ArrayList<ParkingMarker>();
        sbText = new StringBuilder();
    }
 
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
 
        super.startElement(uri, localName, name, attributes);
 
        if (localName.equals("parking")) {
            parkingActual = new ParkingMarker(name, 0, 0, true);
        }
    }
	
    @Override
    public void characters(char[] ch, int start, int length)
                   throws SAXException {
 
        super.characters(ch, start, length);
 
        if (this.parkingActual != null)
            sbText.append(ch, start, length);
    }
	
	
	@Override
    public void endElement(String uri, String localName, String name)
                   throws SAXException {
 
        super.endElement(uri, localName, name);
        double value;
        if (this.parkingActual != null) {
 
            if (localName.equals("name")) {
            	parkingActual.setName(sbText.toString().trim());
              } else if (localName.equals("lat")) {
            	value = Double.parseDouble(sbText.toString().trim());	
                parkingActual.setLat(value);
            } else if (localName.equals("lng")) {
            	value = Double.parseDouble(sbText.toString().trim());	
                parkingActual.setLng(value);
            } else if (localName.equals("free")) {
            	
            	value = Double.parseDouble(sbText.toString().trim());
            	
            	if(value==1)parkingActual.setFree(Boolean.TRUE);
            	else if(value==0)parkingActual.setFree(Boolean.FALSE);

            	else{
            		// NOT CORRECT STRUCTURE
            		parsingError = true;
            	}
             
            } else if (localName.equals("parking")) {
                parkings.add(parkingActual);
            }
 
            sbText.setLength(0);
        }
    }
	
	
	
	
	
}
