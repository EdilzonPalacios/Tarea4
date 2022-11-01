package com.example.tarea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button btnguardar,foto,lis;
    EditText nombre, des;
    ImageView ObjetoImagen;
    static final int peticion_captura_imagen = 100;
    static final int peticion_acceso_cam = 201;

    private void config() {
        btnguardar = (Button) findViewById(R.id.btnguardarn);
        nombre = (EditText) findViewById(R.id.nombre);
        des = (EditText) findViewById(R.id.des);
        ObjetoImagen=(ImageView) findViewById(R.id.imageView) ;
        foto=(Button) findViewById(R.id.foto);
        lis=(Button)  findViewById(R.id.f);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        config();

        btnguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AgregarPersona();
            }
        });

        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();
                tomarfoto();

            }
        });

        lis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),lista.class);
                startActivity(intent);
            }
        });
    }

    private void AgregarPersona() {
        SQLiteConexion conexion = new SQLiteConexion(this, Operaciones.NameDatabase, null, 1);
        SQLiteDatabase db = conexion.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put(Operaciones.nombres, nombre.getText().toString());
        valores.put(Operaciones.descripcion, des.getText().toString());

        Long resultado = db.insert(Operaciones.TbAgenda, Operaciones.id, valores);

        Toast.makeText(getApplicationContext(), "Registro Ingresado", Toast.LENGTH_SHORT).show();

    }

    private void ClearScreen() {
        nombre.setText("");
        des.setText("");
    }

    private void permisos() {
        // Validar si el permiso esta otorgado o no para tomar fotos
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            // Otorgar el permiso si no se tiene el mismo
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, peticion_acceso_cam);
        }
        else
        {
            tomarfoto();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_cam)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                tomarfoto();
            }
        }
    }
    private void tomarfoto()
    {
        Intent intentfoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intentfoto.resolveActivity(getPackageManager())!= null)
        {
            startActivityForResult(intentfoto, peticion_captura_imagen);
        }
        try {
            // externalStorage
            String ExternalStorageDirectory = Environment.getExternalStorageDirectory() + File.separator;
            // uri de la imagen seleccionada
            Uri uri = Uri.fromFile(new File(ExternalStorageDirectory + "Download/imagenseleccionada.jpg"));
            //carpeta "imagenesguardadas"
            String rutacarpeta = "imagenesguardadas/";
            // nombre del nuevo png
            String nombre = "nuevo.png";

            // Compruebas si existe la carpeta "imagenesguardadas", sino, la crea
            File directorioImagenes = new File(ExternalStorageDirectory + rutacarpeta);
            if (!directorioImagenes.exists())
                directorioImagenes.mkdirs();

            // le pasas al bitmap la uri de la imagen seleccionada
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            // pones las medidas que quieras del nuevo .png
            int bitmapWidth = 120; // para utilizar width de la imagen original: bitmap.getWidth();
            int bitmapHeight = 120; // para utilizar height de la imagen original: bitmap.getHeight();
            Bitmap bitmapout = Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false);
            //creas el nuevo png en la nueva ruta
            bitmapout.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(ExternalStorageDirectory + rutacarpeta + nombre));

            // le pones parametros necesarios a la imagen para que se muestre en cualquier galería

            File filefinal = new File(ExternalStorageDirectory + rutacarpeta + nombre);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Titulo");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Descripción");
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
            values.put(MediaStore.Images.ImageColumns.BUCKET_ID, filefinal.toString().toLowerCase(Locale.getDefault()).hashCode());
            values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, filefinal.getName().toLowerCase(Locale.getDefault()));
            values.put("_data", filefinal.getAbsolutePath());
            ContentResolver cr = getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            //

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == peticion_captura_imagen)
        {
            Bundle extras = data.getExtras();
            Bitmap imagen = (Bitmap) extras.get("data");
            ObjetoImagen.setImageBitmap(imagen);
        }
    }







}







