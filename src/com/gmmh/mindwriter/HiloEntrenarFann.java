package com.gmmh.mindwriter;

import android.os.AsyncTask;

public class HiloEntrenarFann extends AsyncTask<Void,Void,Boolean>{
	private MainActivity principal;
	public HiloEntrenarFann()
	{
		
	}
	
	@Override
	protected void onPreExecute()
	{		
		System.out.println("[Pre de HiloEntrenarFann]");
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		this.principal.llamarFann();
		return true;
	}
	@Override
	protected void onPostExecute(Boolean resultado)
	{
		System.out.println("Termino entrenamiento =]");
	}

	public MainActivity getPrincipal() {
		return principal;
	}

	public void setPrincipal(MainActivity principal) {
		this.principal = principal;
	}
	
}