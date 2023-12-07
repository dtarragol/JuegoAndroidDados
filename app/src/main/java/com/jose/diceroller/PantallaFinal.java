package com.jose.diceroller;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import static com.jose.diceroller.MenuInicial.REQUEST_CODE;
import android.media.MediaPlayer;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
//-------------------
import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.provider.CalendarContract;
//-------------------
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jose.diceroller.db.DbManager;
import com.jose.diceroller.db.PlayerHistory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class PantallaFinal extends AppCompatActivity {
    //atributos de firebase y google
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private GoogleSignInClient gsc;
    private GoogleSignInOptions gso;
    FirebaseAuth mAuth;
//resto de atributos
    private TextView textView;
    private EditText gamerN;
    private EditText editText;

    private TextView txtPuntuacion;
    private GlobalVariables datos;
    private Button saveName, btnSalir, btnInicio;

    public Activity activity = this;
    View vista;
    private DbManager dbManager;

    public static final int REQUEST_CODE_PERMISSIONS = 1;

    private static final int REQUEST_CODE_PERMISO_ESCRIBIR_EXTERNO = 2;
    private static final int REQUEST_CODE_PERMISO_UBICACION_MEDIOS = 1;
    private static final int REQUEST_CODE_PERMISO_ESCRIBIR_EXT = 2;

    private static final String[] PERMISSION = {
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MEDIA_CONTENT_CONTROL
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;


    // Declaraciones para las notificaciones
    private Button btnNotificacion;
    private static final String CHANNEL_ID = "canal"; // string para el canal (doc android)
    private PendingIntent pendingIntent; // lanzar la actividad al hacer click


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        datos = (GlobalVariables) getApplicationContext();//instanciamos la variable global
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_final);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dbManager = new DbManager(this);
        textView = findViewById(R.id.txt_puntos);//localizamos el txt de los puntos
        gamerN = findViewById(R.id.txtGamerN);
        String puntos = "Monedas ganadas: " + String.valueOf(datos.getPuntuacion());//mostramos el valor de la puntuación global
        textView.setText(puntos);//mensaje de número de monedas
        saveName = findViewById(R.id.SaveButton);
        vista = findViewById(R.id.SaveButton);
        txtPuntuacion = findViewById(R.id.txtScoreTitle);
        btnInicio =findViewById(R.id.btn_volver_jugar);
        btnSalir = findViewById(R.id.btn_salir);

        inicicalizarFireBase();//iniciamos firebase

        mAuth = FirebaseAuth.getInstance();//iniciamos la autenticación
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION}, REQUEST_CODE_PERMISO_ESCRIBIR_EXTERNO);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISO_ESCRIBIR_EXTERNO);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MEDIA_CONTENT_CONTROL}, REQUEST_CODE_PERMISO_ESCRIBIR_EXTERNO);

        verifyPermission(this);
        String nombre;
        nombre = datos.getNombreJugador().toString();
        gamerN.setText(nombre);

        if (datos.getPuntuacion() > 10){
            // NOTIFICACION victoria (más de 10 monedas)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotification();
            } else {
                showNewNotification();
            }
        } // Podriamos añadir un else por si existe mensaje de error

        if (datos.getPuntuacion() > 10){
            // NOTIFICACION victoria (más de 10 monedas)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                showNotification();
            } else {
                showNewNotification();
            }
        } // Podriamos añadir un else por si existe mensaje de error

        saveName.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                if (gamerN.getText().toString().isEmpty()){//comprobamos que la caja de texto nombre tiene datos
                    Toast.makeText(PantallaFinal.this, "Introduce el nombre", Toast.LENGTH_SHORT).show();
                    Bitmap capture = createScreenshot();
                    takeScreenCapture(activity, capture);
                    createFile(getWindow().getDecorView().getRootView(), "result");
                }else {
                    Bitmap capture = createScreenshot();
                    crearJugadaDatos();//creamos la jugada en firebase
                    insertJugadorRx();
                    gamerN.setText("");//vaciamos la caja de texto
                    datos.setPuntuacion(0);//dejamos la puntuación a 0
                    textView.setVisibility(View.INVISIBLE);//dejamos invisibles la puntuacion y el texto de título
                    txtPuntuacion.setVisibility(View.INVISIBLE);
                    saveName.setEnabled(false);//deshabilitamos el botón de guardar
                    takeScreenCapture(activity, capture);
                    insertEvent();

                }


            }
        });
        //botón salir
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();//salimos de la autenticacion de mail y contraseña
                signOut();//salimos de la cuenta de google
                finish();
                //nos volvemos a la ventana del login
                startActivity(new Intent(PantallaFinal.this, LoginActivity.class));
            }
        });
        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PantallaFinal.this, MenuInicial.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * metodo para guardar la jugada en firebase
     */
    private void crearJugadaDatos() {
        UUID uuid = UUID.randomUUID();
        String playerId = uuid.toString();
        String nombre = datos.getNombreJugador();
        int nuevaPuntuacion = datos.getPuntuacion();
        double nuevaLatitud = datos.getLatitud();
        double nuevaLongitud = datos.getLongitud();
        crearJugada(playerId,nombre,nuevaPuntuacion, nuevaLatitud, nuevaLongitud );
    }

    /**
     * función para crear las jugadas
     * @param playerId
     * @param nombre
     * @param nuevaPuntuacion
     * @param nuevaLatitud
     * @param nuevaLongitud
     */
    private void crearJugada(String playerId, String nombre, int nuevaPuntuacion, double nuevaLatitud, double nuevaLongitud) {
        //cogemos la fecha actual
        Date currentDate = new Date();
        // Crea un objeto SimpleDateFormat con el formato deseado
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // convertimos la fecha a una cadena en el formato especificado
        String dateString = dateFormat.format(currentDate);
        databaseReference = FirebaseDatabase.getInstance().getReference(PlayerHistory.TABLE_JUGADORES);
        PlayerHistory playerHistory = new PlayerHistory(playerId, nombre,nuevaPuntuacion, dateString, nuevaLatitud, nuevaLongitud );
        // Crea un nuevo nodo con la UID del usuario y guarda los datos
        databaseReference.child(playerId).setValue(playerHistory);
    }



    //Insertar jugador con RxJava
    public void insertJugadorRx(){
        String nombre = gamerN.getText().toString();
        int puntuacion = datos.getPuntuacion();
        Date fecha = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Ajusta el formato
        String fechaStr = dateFormat.format(fecha);
        double latitud = datos.getLatitud();
        double longitud = datos.getLongitud();
        dbManager.insertJugador(nombre, puntuacion, fechaStr, latitud, longitud)
                .subscribeOn(Schedulers.io()) // Ejecuta la inserción en un hilo diferente
                .observeOn(AndroidSchedulers.mainThread()) // Recibe el resultado en el hilo principal
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }
                    @Override
                    public void onSuccess(@NonNull Long id) {
                        // La inserción fue exitosa, puedes manejar el resultado aquí (id es la clave primaria generada)
                        Log.d("InsertarRegistros", "Jugada registrada con éxito, ID: " + id);
                        Toast.makeText(PantallaFinal.this, "Registro insertado con exito", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        // Hubo un error al insertar, puedes manejar el error aquí
                        Log.e("InsertarRegistros", "Error al insertar persona", e);
                        Toast.makeText(PantallaFinal.this, "Registro NOO insertado", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //añadimos el menún
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int id = item.getItemId();
        if ( id == R.id.menu_inicio){
            Intent intent = new Intent(PantallaFinal.this,MenuInicial.class);
            startActivity(intent);
            finish();
            //Toast.makeText(this, "Has clicado la primera opcion", Toast.LENGTH_SHORT).show();
        }else if ( id == R.id.menu_salir){
            finish();
        }
        else if(id == R.id.menu_salir){
            mAuth.signOut();
            signOut();
            finish();
        }
        return true;
    }
    protected static File createFile(View view, String filename) {
        Date date = new Date();
        CharSequence format = DateFormat.getDateInstance().format("yyyy-MM-dd_hh:mm:ss");
        try {
            String directoryPath = Environment.getExternalStorageDirectory().toString() + "/dashrolls";
            File directorioDashrolls = new File(Environment.getExternalStorageDirectory(), "/dashrolls");
            if (!directorioDashrolls.exists()) {
                directorioDashrolls.mkdirs();
            }
            String path = directoryPath + "/" + filename + ".jpg";
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);

            File image = new File(path);
            FileOutputStream fileOutputStream = new FileOutputStream(image);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return image;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static void verifyPermission(Activity activity){

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, PERMISSION, REQUEST_EXTERNAL_STORAGE);
        }
    }

    public Bitmap createScreenshot() {
        // Obtener la ventana raíz
        Window window = getWindow();
        // Obtener el tamaño de la pantalla
        int width = window.getDecorView().getWidth();
        int height = window.getDecorView().getHeight();
        // Crear una nueva imagen
        Bitmap screenshot = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // Crear un nuevo lienzo
        Canvas canvas = new Canvas(screenshot);
        // Capturar la pantalla
        window.getDecorView().draw(canvas);
        return screenshot;
    }

    public void takeScreenCapture(Activity activity, Bitmap screenshot) {
        //Solicitar permisos
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (activity.checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(permissions, REQUEST_CODE_PERMISSIONS);

        }
        //Crear carpeta

        File diceRollerFolder = new File(
                activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "DiceRoller"
        );
        if (!diceRollerFolder.exists()) {
            diceRollerFolder.mkdirs();
        }
        if (screenshot == null) {
            // Maneja el error
            Log.e("PantallaFinal", "Falló al tomar la captura de pantalla");
            return;
        }
        // Guarda la captura de pantalla en un archivo
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(
                    activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    String.format("dr_%s.jpg", new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()))
            ));
            screenshot.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (IOException e) {
            Log.e("PantallaFinal", "Falló al guardar la captura de pantalla", e);
        }
    }

    public void insertEvent(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR},REQUEST_CODE );
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "Nueva victoria DiceRoller")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Nueva victoria alcanzada")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "En tu dispositivo Android");
        startActivity(intent);
    }

    // Métodos para mostrar notificaciones
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "NEW",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        showNewNotification();
    }

    private void showNewNotification() {
        setPendingIntent(MenuInicial.class); // redirigir a la pantalla NotificacionActivity al hacer click
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Victoria!! Enhorabuena!")
                .setContentText("Conseguiste más de 10 puntos")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        managerCompat.notify(1, builder.build());
    }

    private void setPendingIntent(Class<?> clsActivity){
        Intent intent = new Intent(this, clsActivity);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(clsActivity);
        stackBuilder.addNextIntent(intent);
        pendingIntent = stackBuilder.getPendingIntent(1,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * funcion para iniciar firebase
     */
    private void inicicalizarFireBase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();


    }

    /**
     * función para salir de la cuenta de google
     */
    private void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                finish();
                startActivity(new Intent(PantallaFinal.this, LoginActivity.class));
            }
        });
    }
}

