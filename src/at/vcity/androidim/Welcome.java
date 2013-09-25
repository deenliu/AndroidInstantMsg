package at.vcity.androidim;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class Welcome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_page);
	}
	
	public void startSignUp(View v){
		Intent signup = new Intent(this, SignUp.class);
		startActivity(signup);
		finish();
	}
	
	public void startLogin(View v){
		Intent login = new Intent(this, Login.class);
		startActivity(login);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome_page, menu);
		return true;
	}

}
