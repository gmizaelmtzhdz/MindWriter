package com.gmmh.mindwriter;


import android.os.AsyncTask;
import android.util.Log;

public class AsyncTaskHiloFann extends AsyncTask<Void,Void,Boolean>{
	private MainActivity principal;
	public AsyncTaskHiloFann()
	{
		
	}
	
	@Override
	protected void onPreExecute()
	{		
		System.out.println("[Pre de AsyncTask]");
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		this.principal.llamarFann();
		return true;
	}
	@Override
	protected void onPostExecute(Boolean resultado)
	{
		this.principal.establecerTextoEnEditText();
		try
		{
			Thread.sleep(Math.abs((this.principal.getTiempo()*120)-1200));
			//Thread.sleep(0);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		} 
		this.principal.loop();
	}

	public MainActivity getPrincipal() {
		return principal;
	}

	public void setPrincipal(MainActivity principal) {
		this.principal = principal;
	}
	
}