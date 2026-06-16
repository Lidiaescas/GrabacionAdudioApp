package com.example.grabacionadudioapp;

import android.content.Context;
import android.view.View;

public class RecordButton extends androidx.appcompat.widget.AppCompatButton {

    //Atributos de clase
    MainActivity activity;
    boolean estaGrabando = true; //Para saber si puede o no grabar

    //Establecer lisener del evento del click
    OnClickListener clicker = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(estaGrabando){
                setText("Parar grabación");
                estaGrabando = false;

                //Comienza a grabar
                activity.startRecording();
            }
            else{
                setText("Comenzar grabación");
                estaGrabando = true;
                activity.stopRecording();
            }

        }
    };
    //Constructor de clase
    public RecordButton(Context ctx){
        super(ctx);
        activity = (MainActivity) ctx;
        setText("Comenzar grabación");
        setOnClickListener(clicker);
    }

}
