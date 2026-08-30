package app.devpulse.signtest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Main extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText("PulseClash");
        view.setPadding(48, 48, 48, 48);
        setContentView(view);
    }
}
