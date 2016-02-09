package com.example.dam.chatterbot;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dam.chatterbot.bot.ChatterBot;
import com.example.dam.chatterbot.bot.ChatterBotFactory;
import com.example.dam.chatterbot.bot.ChatterBotSession;
import com.example.dam.chatterbot.bot.ChatterBotType;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private android.widget.EditText editText;
    private android.widget.Button button;
    private android.widget.TextView textView;
    private android.widget.ScrollView scrollView;
    private final int INIT=1, SPEAK=2;
    private TextToSpeech tts;
    static private boolean ok;
    static String speak;
    private static Tarea t;
    private ChatterBotFactory factory;
    private ChatterBot bot1;
    private ChatterBotSession bot1session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.scrollView = (ScrollView) findViewById(R.id.scrollView);
        this.textView = (TextView) findViewById(R.id.textView);
        this.button = (Button) findViewById(R.id.button);
        this.editText = (EditText) findViewById(R.id.editText);
        t= new Tarea();
        init();
    }

    public void init(){
        textView.setText("");
        Bot b=new Bot();
        b.execute();

        Intent intent= new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, INIT);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            ok = true;
        } else{
            ok = false;
        }
    }

    public void enviar(View v){
        String s = editText.getText().toString();
        t.execute(s);
        textView.append("tu: " + s + "\n");
        editText.setText("");
    }

    public void enviarAudio(View v){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla");
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        startActivityForResult(i, SPEAK);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void hablarBot(String bot){
        if(ok) {
            tts.setLanguage(new Locale("es","ES"));
            tts.setPitch((float) 1.0);
            tts.speak(bot, TextToSpeech.QUEUE_FLUSH, null,null);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case INIT:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    tts = new TextToSpeech(this, this);
                    tts.setLanguage(Locale.getDefault());
                } else {
                    Intent intent = new Intent();
                    intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(intent);
                }
                    break;
            case SPEAK:
                ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String text = textos.get(0);
                textView.append("tu: " + text + "\n");
                t=new Tarea();
                t.execute(text);

                break;
            default:
                Intent i = new Intent();
                i.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(i);
                break;
        }
    }

    public void createBot(){
        factory = new ChatterBotFactory();
        bot1 = null;
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch(Exception e){
            e.printStackTrace();
        }
        bot1session = bot1.createSession();
    }


    public class Tarea extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String s="";
            if (params.length!=0) {
                s = params[0];
            }
            try {
                return bot1session.think(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String out) {
            textView.append("bot: " + out + "\n");
            speak=out;
            hablarBot(speak);
        }
    }

    public class Bot extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            createBot();
            return null;
        }

        @Override
        protected void onPostExecute(String out) {
        }
    }
}
