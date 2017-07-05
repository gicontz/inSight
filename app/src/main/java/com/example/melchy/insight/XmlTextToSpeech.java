package com.example.melchy.insight;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;

import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Melchy on 3/3/2017.
 */

public class XmlTextToSpeech extends AppCompatActivity {
    public Synthesizer m_syn = Splashscreen.m_syn;


    public void speakVoice(String words) {


        /*if (m_syn == null) {
            // Create Text To Speech Synthesizer.
            m_syn = new Synthesizer("12c7ac9ed3194f3db28b252ff1713cae");
        }*/


        m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);

        Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, BenjaminRUS)", Voice.Gender.Male, true);
        //Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)", Voice.Gender.Female, true);
        m_syn.SetVoice(v, null);

        // Use a string for speech.

       // m_syn.SpeakToAudio(words);

        // Use SSML for speech.
        //String text = "<speak version='1.0' xml:lang='en-US'><voice xml:lang='en-US' xml:gender='Male' name='Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)'>Microsoft Bing Voice Output API</voice></speak>";
        //m_syn.SpeakSSMLToAudio(text);

        final String body = XmlDom.createDom("en-US", "Male", "Microsoft Server Speech Text to Speech Voice (en-US, BenjaminRUS)", words);
        byte[] xmlBytes = body.getBytes();


        String ttsServiceUri = "https://speech.platform.bing.com/synthesize";
        try {
            URL url = new URL(ttsServiceUri);
            HttpsURLConnection webRequest = (HttpsURLConnection) url.openConnection();
            URLConnection urlConnection = webRequest;
            urlConnection.setRequestProperty("content-length", String.valueOf(xmlBytes.length));
        }catch (Exception e){
            e.printStackTrace();
        }

        m_syn.SpeakSSMLToAudio(body);

    }
}
