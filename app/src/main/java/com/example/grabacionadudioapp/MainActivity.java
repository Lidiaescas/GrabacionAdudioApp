package com.example.grabacionadudioapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Atributo de clase
    MediaPlayer mp = null;
    Button btReproducir, btAvanzar, btRetroceder, btDetener, btPausar;

    private String ruta = null;
    private File f = null;

    //Añadimos el grabador
    MediaRecorder mr = null;
    //Constante para el ID del permiso
    private static final int ID_REQUEST_RECORD_AUDIO_PERMISSION = 100;

    //Layout para poner el botón dinámico de grabación
    LinearLayout ll = null;
    //Atributo de control de aceptación del permiso de grabacón de audio
    private boolean permissionToRecordAccepted = false;
    //Array de permisos
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ruta = getFilesDir() + "/Grabacion.3gp";
        f = new File(ruta);



        //Traemos los botones de la IU
        btReproducir = findViewById(R.id.btReproducir);
        btAvanzar = findViewById(R.id.btAvanzar);
        btRetroceder = findViewById(R.id.btRetroceder);
        btPausar = findViewById(R.id.btPausar);
        btDetener = findViewById(R.id.btDetener);

        //Traemos de la IU el layout sobre el que voy a posicionar el botón de grabación
        ll = findViewById(R.id.layout);
        //Creamos el botón de grabación dinámicamente
        RecordButton recordButton = new RecordButton(this);
        //Añadimos el botón al contenedor
        ll.addView(recordButton);
        //Configuramos la gravedad del layout para centrar el botón en el layout
        ll.setGravity(1);

        //Asignamos un lisener de click a cada uno de los botones
        btReproducir.setOnClickListener(this);
        btAvanzar.setOnClickListener(this);
        btRetroceder.setOnClickListener(this);
        btPausar.setOnClickListener(this);
        btDetener.setOnClickListener(this);

        enableButton(true, false, false, false, false);
    }

    @Override
    public void onClick(View view) {
        //Cuando hagamos click pasa lo siguiente:
        if (view.getId()==R.id.btReproducir) reproducir();
        else if (view.getId()==R.id.btDetener) detener();
        else if (view.getId()==R.id.btAvanzar) avanzar();
        else if (view.getId()==R.id.btRetroceder) retroceder();
        else if (view.getId()==R.id.btPausar) pausar();
    }



    //Gestionamos la visibilidad de los botones, se activan en función de la acción que este ocurriendo
    private void enableButton(boolean btReproducir, boolean btDetener, boolean btAvanzar, boolean btRetroceder, boolean btPausar){
        this.btReproducir.setEnabled(btReproducir);
        this.btDetener.setEnabled(btDetener);
        this.btAvanzar.setEnabled(btAvanzar);
        this.btRetroceder.setEnabled(btRetroceder);
        this.btPausar.setEnabled(btPausar);
    }



    private void avanzar() {
        if (mp != null) {
            if (mp.getCurrentPosition() + 5000 < mp.getDuration()) {
                mp.seekTo(mp.getCurrentPosition() + 5000);
            } else {
                mp.seekTo(mp.getDuration());
            }
        }
    }

    private void retroceder() {
        if (mp != null) {
            if (mp.getCurrentPosition() - 5000 > 0) {
                mp.seekTo(mp.getCurrentPosition() - 5000);
            } else {
                mp.seekTo(0);
            }
        }
    }

    private void pausar() {
        mp.pause();
        enableButton(true, true, true, true, false);
    }

    private void reproducir() {
        try {
            if (mp == null){
                mp = new MediaPlayer();
                mp.setDataSource(ruta);
                mp.prepare();
                mp.setOnCompletionListener(mediaPlayer -> {
                    enableButton(false, true, true, true, true);
                    detener();
                });
            }
        if (!mp.isPlaying()){
            mp.start();
            enableButton(false, true, true, true, true);
        }
        } catch (IOException e) {
            Toast.makeText(this, "No se puede reproducir el audio", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void detener() {
        mp.stop();
        try {
            mp.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        enableButton(true, false, false, false, false);
    }

    public void startRecording() {
        //Chequeamos si tiene el permiso necesario o no
        if (ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            //inicializamos el atributo que es un objeto de tipo MediaRecorder
            mr = new MediaRecorder(this);
            //Seleccionamos la fuente de audio, el micrófono
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            //seleccionamos el formato de salida 3GP (subtipo MP4) recomendado para la voz humana
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //Buscamos la ruta para guardar la grabación (Almacenamiento interno al ser la propia voz del usuario)
            mr.setOutputFile(getFilesDir().getAbsolutePath() + File.separator + "Grabacion.3gp");

            //Seleccionamos el codificador del fichero
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            //Preparamos la grabación y grabamos
            try {
                mr.prepare();
                mr.start();
            } catch (IOException e) {
                //Si falla la liberamos recursos
                mr.reset();
                mr.release();
                mr = null;
                e.printStackTrace();
            }
        }
        else{
            //Si no tenemos el permiso se lo pedimos al usuario explicitamente
            ActivityCompat.requestPermissions(this, permissions, ID_REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }
    //Gestiono la petición
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ID_REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) { //Si no tengo permiso finalizo la actividad
            finish();
        } else { //Si nos conceden el permiso grabamos
            startRecording();
        }
    }

            public void stopRecording() {
        //Paramos la grabación
        mr.stop();
        //Reseteamos el MediaRecorder
        mr.reset();
        //Liberamos el MediaRecorder
        mr.release();
        mr = null;

    }
}