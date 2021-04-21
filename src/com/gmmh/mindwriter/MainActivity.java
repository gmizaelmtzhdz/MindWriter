package com.gmmh.mindwriter;

import java.util.Random;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;
import com.neurosky.thinkgear.TGRawMulti;

import com.gmmh.mindwriter.R;


import com.facebook.*;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareDialog;
public class MainActivity extends Activity {
	/*
	 * 
	 * Facebook
	 * 
	 *  */
	private static final String PERMISSION = "publish_actions";
    private static final Location SEATTLE_LOCATION = new Location("") {
        {
            setLatitude(47.6097);
            setLongitude(-122.3331);
        }
    };
    private Random aleatorio;
    private final String PENDING_ACTION_BUNDLE_KEY =
            "com.gmmh.mindwriter:PendingAction";

    private Button postStatusUpdateButton;
    private Button postPhotoButton;
    private ProfilePictureView profilePictureView;
    private TextView greeting;
    private PendingAction pendingAction = PendingAction.NONE;
    private boolean canPresentShareDialog;
    private boolean canPresentShareDialogWithPhotos;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private ShareDialog shareDialog;
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d("HelloFacebook", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
            String title = getString(R.string.error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("HelloFacebook", "Success!");
            if (result.getPostId() != null) {
                String title = getString(R.string.success);
                String id = result.getPostId();
                String alertMessage = getString(R.string.successfully_posted_post, id);
                showResult(title, alertMessage);
            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    };

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

	
		//Fin
	
	private static final int MODE_WRITABLE = 0;
	private BluetoothAdapter bluetoothAdapter;	
	private TGDevice tgDevice;
	private final String []letras={"a","b","c","d","e","f","g","h","i","j","k","l","m","n","ñ","o","p","q","r","s","t","u","v","w","x","y","z"};
	private final boolean rawEnabled = false;
	
	private double delta;
	private double lowAlpha;
	private double highAlpha;
	private double lowBeta;
	private double highBeta;
	private double lowGamma;
	private double midGamma;
	private double theta;
	
	private AssetManager assetManager;
	private InputStream input;
	private String texto;
	private String urlArchivo;
	
	private String cadenaEditText; 
	private boolean estado;
	
	private SeekBar seekBar;
	private TextView textViewVelocidad;
	
	private Button conectar;
	private Button capturar;
	private Button limpiar;
	
	private Button twitter;
	private Button facebook;
	
	private int tiempo;
	private EditText editText;
	
	private AlertDialog dialogo;
	
	static {
		try {  
			System.loadLibrary("gnustl_shared");
			System.loadLibrary("fann-test"); 
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.aleatorio=new Random();
		//Facebook
		FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handlePendingAction();
                        updateUI();
                    }

                    @Override
                    public void onCancel() {
                        if (pendingAction != PendingAction.NONE) {
                            showAlert();
                            pendingAction = PendingAction.NONE;
                        }
                        updateUI();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        if (pendingAction != PendingAction.NONE
                                && exception instanceof FacebookAuthorizationException) {
                            showAlert();
                            pendingAction = PendingAction.NONE;
                        }
                        updateUI();
                    }

                    private void showAlert() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.cancelled)
                                .setMessage(R.string.permission_not_granted)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });

        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
                // It's possible that we were waiting for Profile to be populated in order to
                // post a status update.
                handlePendingAction();
            }
        };

        profilePictureView = null;//(ProfilePictureView) findViewById(R.id.profilePicture);
        greeting = null;//(TextView) findViewById(R.id.greeting);

        postStatusUpdateButton =null;/* = (Button) findViewById(R.id.postStatusUpdateButton);
        postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickPostStatusUpdate();
            }
        });
         */
        postPhotoButton = null;/*(Button) findViewById(R.id.postPhotoButton);
        postPhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickPostPhoto();
            }
        });
*/
        // Can we present the share dialog for regular links?
        canPresentShareDialog = ShareDialog.canShow(
                ShareLinkContent.class);

        // Can we present the share dialog for photos?
        canPresentShareDialogWithPhotos = ShareDialog.canShow(
                SharePhotoContent.class);
		//Fin
		
		this.cadenaEditText="";
		this.estado=false;
		this.seekBar=(SeekBar) findViewById(R.id.seekBarVelocidad);
		this.seekBar.setProgress(2);
		this.setProgress(2);
		
		this.eventoSeek(); 
		this.editText=(EditText) findViewById(R.id.editTextSenalConvertidaEnTexto);
		//this.editText.setEnabled(false);
		this.editText.setKeyListener(null);
		
		
		//Para entrenar
		//this.obtenerTextoDeArchivoInterno("rna.data");
		//this.setUrlArchivo("/storage/sdcard0/Download/rna.data");
		
		//Para probar
		this.obtenerTextoDeArchivoInterno("rna.net");
		this.setUrlArchivo("/storage/sdcard0/Download/rna.net");
		
		this.crearArchivo();
		
		/*
		 * Descomentar para entrenar
		HiloEntrenarFann h=new HiloEntrenarFann();
		h.setPrincipal(this);
		h.execute();
		*/
		
		// llamando al metodo nativo ndk de fann
		//System.out.println("Mandando llamar a testFANN()");
		//System.out.println("RESULTADO DE LLAMADA MÉTODO DE [C]: "+testFann(99,this.urlArchivo,(float)this.delta,(float)this.lowAlpha,(float)this.highAlpha,(float)this.lowBeta,(float)this.highBeta,(float)this.lowGamma,(float)this.midGamma,(float)this.theta));
		
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
        if(this.bluetoothAdapter == null)
        {
        	this.alertExito("Bluetooth","Bluetooth, no disponible, verifica la conexión");
        	return;
        }
        else
        {	
        	System.out.println("Conectado al bluetooth");
        	this.tgDevice = new TGDevice(this.bluetoothAdapter, this.handler);
        }  
        
        this.conectar=(Button)findViewById(R.id.conectar);
        this.conectar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				conectar();
			}
		});
        this.capturar=(Button)findViewById(R.id.capturar);
        this.capturar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(estado)
					capturar.setText("Capturar");
				else
					capturar.setText("Detener");
				setEstado(!estado);
				loop();
			}
		});
        this.limpiar=(Button)findViewById(R.id.limpiar);
        this.limpiar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cadenaEditText="";
				establecerTextoEnEditText("");
			}
		});
        
        
        this.twitter=(Button)findViewById(R.id.twitter);
        this.twitter.setOnClickListener(new View.OnClickListener() {
			@Override 
			public void onClick(View v) {
				compartirEnTwitter();
			}
		});
        this.facebook=(Button)findViewById(R.id.facebook);
        this.facebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//onClickPostStatusUpdate();
				
				compartirEnFacebook();
			}
		});
        
        this.bloquearBotones();
	}

	
	private static native String testFann(int entero,String entrenamiento,float delta, float lowAlpha, float highAlpha, float lowBeta, float highBeta, float lowGamma, float midGamma, float theta);
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
            case TGDevice.MSG_STATE_CHANGE:

                switch (msg.arg1) {
	                case TGDevice.STATE_IDLE:
	                    break;
	                case TGDevice.STATE_CONNECTING:		                	
	                	System.out.println("Connecting...\n");
	                	break;		                    
	                case TGDevice.STATE_CONNECTED:
	                	ocultarAlert();
	                	System.out.println("Connected.\n");
	                	desbloquearBotones();
	                	tgDevice.start();
	                    break;
	                case TGDevice.STATE_NOT_FOUND:
	                	ocultarAlert();
	                	bloquearBotones();
	                	alertExito("Conexion [STATE NOT FOUND]","Verifique que la diadema este encendida y emparejada por bluetooth");
	                	System.out.println("Can't find\n");
	                	break;
	                case TGDevice.STATE_NOT_PAIRED:
	                	bloquearBotones();
	                	ocultarAlert();
	                	alertExito("Conexion [STATE NOT PAIRED]","Verifique que la diadema este encendida y emparejada por bluetooth");
	                	System.out.println("not paired\n");
	                	break;
	                case TGDevice.STATE_DISCONNECTED:
	                	ocultarAlert();
	                	bloquearBotones();
	                	alertExito("Conexion [DISCONNECTED]","Se desconecó la diadema.");
	                	System.out.println("Disconnected mang\n");
                }

                break;
            case TGDevice.MSG_POOR_SIGNAL:
            	System.out.println("PoorSignal: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_RAW_DATA:	  
            	//System.out.println("Got raw: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_HEART_RATE:
            	System.out.println("Heart rate: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_ATTENTION:
            		System.out.println("Attention: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_MEDITATION:
            	
            	break;
            case TGDevice.MSG_BLINK:
            	System.out.println("Blink: " + msg.arg1 + "\n"); 
            	break;
            case TGDevice.MSG_RAW_COUNT:
            	System.out.println("Raw Count: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_LOW_BATTERY:
            	Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
            	break;
            case TGDevice.MSG_RAW_MULTI:
            	TGRawMulti rawM = (TGRawMulti)msg.obj;
            	System.out.println("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
            	break;
            case TGDevice.MSG_EEG_POWER:
            	TGEegPower ep=(TGEegPower)msg.obj;
            	System.out.println("[Delta]: "+ep.delta);
            	delta=ep.delta;
            	
            	System.out.println("[LowAlpha]: "+ep.lowAlpha);
            	lowAlpha=ep.lowAlpha;
            	
            	System.out.println("[HighAlpha]: "+ep.highAlpha);
            	highAlpha=ep.highAlpha;
            	
            	System.out.println("[LowBeta]: "+ep.lowBeta);
            	lowBeta=ep.lowBeta;
            	
            	System.out.println("[HighBeta]: "+ep.highBeta);
            	highBeta=ep.highBeta;
            	
            	System.out.println("[lowGamma]: "+ep.lowGamma);
            	lowGamma=ep.lowGamma;
            	
            	System.out.println("[midGamma]: "+ep.midGamma);
            	midGamma=ep.midGamma;
            	
            	System.out.println("[theta]: "+ep.theta);
            	theta=ep.theta;
            	System.out.println("\n");            	
            	break;
            default:
            	break;
        }
        }
    };
	
    public void conectar()
    {
    	//tgDevice.connect(true);
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
    	{
    		tgDevice.connect(true);
    		this.alertProgreso();
    	}
    		
    }
    public String convertirSenalATexto(int indice)
    {
    	return this.letras[indice];
    }
    public void alertExito(String titulo,String mensaje)
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle(titulo);
		builder.setMessage(mensaje);
		builder.setCancelable(true);
		builder.setPositiveButton("ACEPTAR",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
		builder.setIcon(R.drawable.ic_launcher_cerebro);
		this.dialogo=builder.create();
		this.dialogo.show();
	}
    public boolean verificarSiExisteArchivo(String url)
    {
	    File fichero = new File(url);
	    return fichero.isFile();
    }
    public boolean verificarSiExisteDirectorio(String url)
    {
    	File fichero = new File(url);
	    return fichero.isDirectory();
    }
    public void crearArchivo()
    {
    	System.out.println("[BOOLEAN]: "+this.verificarSiExisteArchivo("/storage/sdcard0/Download/rna.net"));
		System.out.println("[BOOLEAN]: "+this.verificarSiExisteDirectorio("/storage/sdcard0/Download/"));
    	//if(this.verificarSiExisteDirectorio("/storage/sdcard0/Download/") && !this.verificarSiExisteArchivo("/storage/sdcard0/Download/datos.net"))
		if(this.verificarSiExisteDirectorio("/storage/sdcard0/Download/"))
    	{
    		try{
    			PrintWriter writer = new PrintWriter(this.urlArchivo, "UTF-8");
    			writer.print(this.texto); 
    			writer.flush();
    			writer.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
    	}
    }
    public void obtenerTextoDeArchivoInterno(String assets)
    {
    	this.assetManager=getAssets();
		try {
			 
			this.input = this.assetManager.open(assets);
			int size = this.input.available();
			byte[] buffer = new byte[size];
			this.input.read(buffer);
			this.input.close();  
			this.texto = new String(buffer);
			
			System.out.println("Texto: "+this.texto);
		}catch (IOException e) { 
			e.printStackTrace();
		}
    } 
    public void llamarFann()
    {
    	double deltaMax=3473322.0;
    	double thetaMax=3072570.0;
    	double lowAlphaMax=809805.0;
    	double highAlphaMax=762007.0;
    	double lowBetaMax=643499.0;
    	double highBetaMax=679478.0;
    	double lowGammaMax=456807.0;
    	double midGammaMax=433577.0;
 
    	double deltaMin=3.0;
    	double thetaMin=3.0;
    	double lowAlphaMin=0.0;
    	double highAlphaMin=0.0;
    	double lowBetaMin=0.0;
    	double highBetaMin=1.0;
    	double lowGammaMin=1.0;
    	double midGammaMin=1.0;
				
    	double delta=(this.delta - deltaMin)/(deltaMax - deltaMin);
    	double theta=(this.theta - thetaMin)/(thetaMax - thetaMin);
    	double lowAlpha=(this.lowAlpha - lowAlphaMin)/(lowAlphaMax - lowAlphaMin);
    	double highAlpha=(this.highAlpha - highAlphaMin)/(highAlphaMax - highAlphaMin);
    	double lowBeta=(this.lowBeta - lowBetaMin)/(lowBetaMax - lowBetaMin);
    	double highBeta=(this.highBeta - highBetaMin)/(highBetaMax - highBetaMin);
    	double lowGamma=(this.lowGamma - lowGammaMin)/(lowGammaMax - lowGammaMin);
    	double midGamma=(this.midGamma - midGammaMin)/(midGammaMax - midGammaMin);
    	
    	String resultado=testFann(99,this.urlArchivo,(float)delta,(float)lowAlpha,(float)highAlpha,(float)lowBeta,(float)highBeta,(float)lowGamma,(float)midGamma,(float)theta);
    	int resultado_entero=Integer.parseInt(resultado, 2);
    	if(resultado_entero > 26)
    	{
    		resultado_entero=this.aleatorio.nextInt(27);
    	}
    	System.out.println("CADENA DE LLAMADA FANN C to JAVA [STRING]: "+resultado);
    	System.out.println("CADENA DE LLAMADA FANN C to JAVA [ENTERO]: "+resultado_entero);
    	System.out.println("LETRA: "+this.convertirSenalATexto(resultado_entero));
    	this.cadenaEditText=this.cadenaEditText+this.convertirSenalATexto(resultado_entero);
    	//System.out.println("RESULTADO DE LLAMADA MÉTODO DE [C]: "+resultado);
    } 
    public void loop()
    {
    	if(this.estado)
    	{
    		AsyncTaskHiloFann hilo= new AsyncTaskHiloFann();
    		hilo.setPrincipal(this);
    		hilo.execute();
    	}
    }
    public void establecerTextoEnEditText()
    {
    	this.editText.setText(this.cadenaEditText);
    }
    public void establecerTextoEnEditText(String texto)
    {
    	this.editText.setText(texto);
    }
    public void eventoSeek()
    {
    	this.textViewVelocidad=(TextView) findViewById(R.id.textViewLabelVelocidad);
    	this.seekBar.setMax(10);
    	this.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub	
				setTiempo(progress);
				textViewVelocidad.setText("Velocidad de recepción ["+progress+"/10]");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}});       
    }
    public void compartirEnTwitter()
    {
    	if(this.cadenaEditText.equals(""))
    		this.alertExito("Compartir en Twitter","No hay texto que compartir.");
    	else
    	{
	 		Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setData(Uri.parse("https://twitter.com/intent/tweet?text="+this.cadenaEditText+"+%0d%0a+%23MindWriter"));
			startActivity(intent);
    	}
    }
    public void compartirEnFacebook()
    {
    	if(this.cadenaEditText.equals(""))
    		this.alertExito("Compartir en Facebook","No hay texto que compartir.");
    	else
    	{
    		onClickPostStatusUpdate(); 
    		/*
	 		Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setData(Uri.parse("https://www.facebook.com/dialog/share_open_graph?app_id=145634995501895&display=popup&action_type=og.likes&action_properties={%22object%22%3A%22https%3A%2F%2Fdevelopers.facebook.com%2Fdocs%2F%22}&redirect_uri=https%3A%2F%2Fdevelopers.facebook.com%2Ftools%2Fexplorer"));
			startActivity(intent);
			*/ 
    	}
    }
	public String getUrlArchivo() {
		return urlArchivo;
	}
	public void setUrlArchivo(String urlArchivo) {
		this.urlArchivo = urlArchivo;
	}
	public void setEstado(boolean estado) {
		this.estado = estado;
	}

	public void ocultarAlert()
	{
		this.dialogo.hide();
	}
	public void alertProgreso(String titulo, String texto)
	{
		if(this.dialogo!=null)
			this.ocultarAlert();
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle(titulo);
		builder.setMessage(texto);
		builder.setCancelable(false);
		builder.setIcon(R.drawable.ic_launcher_cerebro);
		builder.setView(new ProgressBar(this));
		this.dialogo=builder.create();
		this.dialogo.show();

	}
	public int getTiempo() {
		return tiempo;
	}
	public void setTiempo(int tiempo) {
		this.tiempo = tiempo;
	}
	public void bloquearBotones(){
		this.estado=false;
		this.capturar.setEnabled(false);
		this.limpiar.setEnabled(false);
	}
	public void desbloquearBotones(){
		this.capturar.setEnabled(true);
		this.limpiar.setEnabled(true);
	}
	public void alertProgreso()
	{
		if(this.dialogo!=null)
			this.ocultarAlert();
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("MindWriter");
		builder.setMessage("Conectando a la diadema");
		builder.setCancelable(false);
		builder.setIcon(R.drawable.ic_launcher_cerebro);
		builder.setView(new ProgressBar(this));
		this.dialogo=builder.create();
		this.dialogo.show();
	}
	/* 
	 * 
	 * 
	 * 
	 * 
	 * Agregado de Facebook 
	 * 
	 * 
	 * 
	 * 
	 * */
	@Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        //postStatusUpdateButton.setEnabled(enableButtons || canPresentShareDialog);
        //postPhotoButton.setEnabled(enableButtons || canPresentShareDialogWithPhotos);

        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            profilePictureView.setProfileId(profile.getId());
            greeting.setText(getString(R.string.hello_user, profile.getFirstName()));
        } else {
            //profilePictureView.setProfileId(null);
            //greeting.setText(null);
        }
    }

    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case NONE:
                break;
            case POST_PHOTO:
                postPhoto();
                break;
            case POST_STATUS_UPDATE:
                postStatusUpdate();
                break;
        }
    }

    private void onClickPostStatusUpdate() {
        performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
    }

    private void postStatusUpdate() {
        Profile profile = Profile.getCurrentProfile();
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
        		.setContentTitle("#MindWriter")
        		.setContentDescription(""+this.cadenaEditText)
        		.setContentUrl(Uri.parse("http://mindwriter.esy.es/?q="+this.cadenaEditText))
                .build();
        if (canPresentShareDialog) {
            shareDialog.show(linkContent);
        } else if (profile != null && hasPublishPermission()) {
            ShareApi.share(linkContent, shareCallback);
        } else {
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }

    private void onClickPostPhoto() {
        performPublish(PendingAction.POST_PHOTO, canPresentShareDialogWithPhotos);
    }

    private void postPhoto() {
        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.braingames);
        SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(image).build();
        ArrayList<SharePhoto> photos = new ArrayList<SharePhoto>();
        photos.add(sharePhoto);

        SharePhotoContent sharePhotoContent =
                new SharePhotoContent.Builder().setPhotos(photos).build();
        if (canPresentShareDialogWithPhotos) {
            shareDialog.show(sharePhotoContent);
        } else if (hasPublishPermission()) {
            ShareApi.share(sharePhotoContent, shareCallback);
        } else {
            pendingAction = PendingAction.POST_PHOTO;
            // We need to get new permissions, then complete the action when we get called back.
            LoginManager.getInstance().logInWithPublishPermissions(
                    this,
                    Arrays.asList(PERMISSION));
        }
    }

    private boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private void performPublish(PendingAction action, boolean allowNoToken) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null || allowNoToken) {
            pendingAction = action;
            handlePendingAction();
        }
    }
}