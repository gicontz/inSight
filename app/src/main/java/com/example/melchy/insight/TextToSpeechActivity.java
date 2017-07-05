package com.example.melchy.insight;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

/**
 * Created by Melchy on 12/02/2017.
 */

public class TextToSpeechActivity extends AppCompatActivity {
    public Synthesizer m_syn;

    public void speakVoice(String words) {


    if (m_syn == null) {
        // Create Text To Speech Synthesizer.
        m_syn = new Synthesizer("YOUR_BING_API_HERE");
    }


    m_syn.SetServiceStrategy(Synthesizer.ServiceStrategy.AlwaysService);

    Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, BenjaminRUS)", Voice.Gender.Male, true);
    //Voice v = new Voice("en-US", "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)", Voice.Gender.Female, true);
    m_syn.SetVoice(v, null);

    // Use a string for speech.

    m_syn.SpeakToAudio(words);

    // Use SSML for speech.
    //String text = "<speak version='1.0' xml:lang='en-US'><voice xml:lang='en-US' xml:gender='Male' name='Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)'>Microsoft Bing Voice Output API</voice></speak>";
    //m_syn.SpeakSSMLToAudio(text);

}



}
