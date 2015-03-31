/*************************************************************************
 *  Compilation:  javac -classpath .:jl1.0.jar MP3.java         (Linux)
 *                javac -classpath .;jl1.0.jar MP3.java         (Windows)
 *  Execution:    java -classpath .:jl1.0.jar MP3               (Linux)
 *                java -classpath .;jl1.0.jar MP3               (Windows)
 *
 *  The MP3.java program:
 *     (1) Uses Google Translate TTS (text to speech) to convert text to an MP3 audio file
 *
 *			http://translate.google.com/translate_tts?q=hello&tl=en&total=1&idx=0&textlen=11
 *
 *			Breaking down the parameters,
 *			   “q” is the text to convert to audio
 *			   “tl” is the text language
 *  						ar	Arabic
 *							hy	Armenian
 *							az	Azerbaijani
 *							eu	Basque
 *							be	Belarusian
 *							bn	Bengali
 *							bh	Bihari
 *							bs	Bosnian
 *							br	Breton
 *							bg	Bulgarian
 *							km	Cambodian
 *							ca	Catalan
 *							zh-CN	Chinese (Simplified)
 *							zh-TW	Chinese (Traditional)
 *							co	Corsican
 *							hr	Croatian
 *							cs	Czech
 *							da	Danish
 *							nl	Dutch
 *							en	English
 *							eo	Esperanto
 *							et	Estonian
 *							fo	Faroese
 *							tl	Filipino
 *							fi	Finnish
 *							fr	French
 *							fy	Frisian
 *							gl	Galician
 *							ka	Georgian
 *							de	German
 *							el	Greek
 *                          ...
 *			   “total” is the total number of chunks (chunk max size = 100)
 *			   “idx” is which chunk we’re on
 *			   “textlen” is the length of the text in that chunk; The Google Translate site itself gets around its own
 *                        character limit by breaking big blocks of text into “chunks”.
 *
 *
 *    (2) Plays the MP3 file using the JLayer MP3 library:
 *             http://www.javazoom.net/javalayer/sources.html
 *
 *************************************************************************/
 //**************************************
 //@ file MP3SAX1.java
 //@ Lixia Zhao
 //@Data 3/2/2015
 //@ Convert the English and Chinese text parsed by SAX into audio.

import java.net.*;
import java.io.*;
import javazoom.jl.player.Player;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;


class MP3SAX 
{
	// the javazoom player
	 static Player player;
   
   //static SaxMP3Parser newSAX = new SaxMP3Parser();

	// this is where the audio file is saved
//	static String filename1 = "sentence.mp3";
 
   //store strings in different lanuages
   static String strEn1 = SAXMP3_3.strEn;
   static String strCn1= SAXMP3_3.strCn;
   //static String strJa1=SAXMP3_3.strJa;
   //static String strFr1=SAXMP3_3.strFr;
  
   	public static void main(String[] args) 
   {
		
      try
      {
         
      //*** create a new instance of a parser
        SAXParser p = new SAXParser();
      
      //*** this object will respond to parser events
         DocumentHandler myDocHandler = new SAXMP3_3();
      
      //*** register the ContentHandler
         p.setDocumentHandler(myDocHandler);
      
      //*** tell parser what file to parse
         p.parse("audio-2.xml");
         strEn1 = SAXMP3_3.strEn;
         strCn1= SAXMP3_3.strCn;
        // strJa1=SAXMP3_3.strJa;
        // strFr1=SAXMP3_3.strFr;
         
         TTS (strEn1,"en", "English3");
         TTS(strCn1, "zh-CN","Chinese3");
        // TTS(strJa1,"ja", "Japanese3");
        // TTS(strFr1,"fr", "French3");
        
         play("English3.mp3");
      }
  
       catch(Exception e)
     {
        e.printStackTrace();
      }

   }
      
      private static void makeMP3 (String destination, String language, List<String> pieces)
      {
      
         try
         {
             //contact Google TTS service and creat a URLConnect
               byte[] buffer = new byte[1024];
               OutputStream outstream = new FileOutputStream(new File(destination + ".mp3"));
               
               boolean appendToFile = true; 
               
               InputStream audioSrc = null;
               for (String piece: pieces)
               {
                  piece = URLEncoder.encode(piece, "UTF-8");
                  URL myURL = new URL("http://www.translate.google.com/translate_tts?ie=utf-8&tl="+ language + "&q=" + piece);
                  HttpURLConnection urlConn = (HttpURLConnection) myURL.openConnection();
                  urlConn.setRequestProperty("User-Agent", "Mozilla");
                  urlConn.connect();
                  
                  audioSrc = urlConn.getInputStream();
                  DataInputStream read = new DataInputStream(audioSrc);
                  
                  int len;
                  while ((len = read.read(buffer)) > 0)
                  {
                      outstream.write(buffer,0,len);
                      outstream.flush();
                  }
                  // close the input stream
                  audioSrc.close();
                  if(appendToFile)
                  {
                     outstream = new FileOutputStream(new File(destination + ".mp3"), true);
                     appendToFile = false;
                  }
               }
                //close the input and output stream
                 audioSrc.close();
                 outstream.close();
		} //end try
      catch(Exception e)
      {
			   System.out.println(e.getMessage());
	   }
    } 

    // Takes in strings of texts in different languages, converts them to URL parameter
     // replace all the spaces in sentence with "+"
     // separate them into a ArrayList of Strings with each string in the list with max
     // 100 characters
    private static ArrayList<String> getText (String text)
    {
         int startInd = 0;
         ArrayList<String> finalText = new ArrayList<String>();
         while (startInd < text.length()-1)
         {
            int endPieceInd = getPieceEnd(text,startInd);// call getPieceEnd();
            String piece =text.substring(startInd,endPieceInd);
            if (piece.length() >= 2)
            {  
            // if first character starts with white space, remove it
              if (piece.substring(0,1).equals(" "))
              {
                  piece = piece.substring(1);
              }
           }
           
           finalText.add(piece);
           startInd = endPieceInd;
       }
       
       return finalText;
   }
   
   private static int getPieceEnd(String text, int startIndex)
   {
      String subtext= (text.substring(startIndex).length()>100)?
                       text.substring(startIndex, startIndex + 100):
                       text.substring(startIndex);
     int end = subtext.length();
     if (text.substring(startIndex).length() > 100)
     { 
        // if the last character is not " ", remove the last character to avoid
        // breaking down in the middle of a word
        while(!subtext.substring(subtext.length()-1, subtext.length()).equals(" "))
        {
            subtext = subtext.substring(0,subtext.length()-1);
            end--;
        }
        subtext = subtext.substring(0,subtext.length()-1);
        end--;
     }
     return end+startIndex;
   }
      
   // send the texts to speech
    public static void TTS (String text, String language, String outputName)
    {
         ArrayList<String> finalText = getText(text);
         
        // for (String piece:finalText)
        //    System.out.println(piece);
            
         makeMP3(outputName,language,finalText);
     }
         
        
    // play the MP3 file to the sound card
    public static void play(String filename) {

        try {
            FileInputStream fis     = new FileInputStream(filename);
            BufferedInputStream bis = new BufferedInputStream(fis);
            player = new Player(bis);
        }
        catch (Exception e) {
            System.out.println("Problem playing file " + filename);
            System.out.println(e);
        }

        // run in new thread to play in background
        new Thread() {
            public void run() {
                try { player.play(); }
                catch (Exception e) { System.out.println(e); }
            }
        }.start();
    }   
   
} // end 
 
  

