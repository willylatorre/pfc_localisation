package com.example.pfc_alpha1;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ParkingParser{	
	
	private URL rssUrl;
	
    public ParkingParser(String url)
    {
        try
        {
            this.rssUrl = new URL(url);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
 
    public List<ParkingMarker> parse() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
 
        try
        {
            SAXParser parser = factory.newSAXParser();
            ParkingHandler handler = new ParkingHandler();
            parser.parse(this.getInputStream(), handler);
            return handler.getParkings();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
 
    private InputStream getInputStream()
    {
        try
        {
            return rssUrl.openConnection().getInputStream();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


	

}