package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class BreakActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView ct_text;
    private CountDownTimer ct;
    private String stopTime = "";
    private boolean isRunning = false;
    private List<Exercise> exerciseList;
    private SharedPreferences sharedPrefs;
    private TextView description, side_repetition, break_exercise_type, execution;
    private int currentExercise, breakTime = 0;
    private ImageView image;
    private String[] exercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentExercise = 0;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int mins = sharedPrefs.getInt("break_value", 5);
        String bufferZeroMinute = "";

        if (mins < 10)
            bufferZeroMinute = "0";

        ct_text = (TextView) findViewById(R.id.textViewBreak);

        DBHandler dbHandler = new DBHandler(this);
        String[] allProfiles = sharedPrefs.getString("profiles", "").split(";");
        String currentProfile = sharedPrefs.getString("name_text", "");

        for (int i = 0; i < allProfiles.length; i++) {
            if (allProfiles[i].split(",")[0].equals(currentProfile) && !allProfiles[i].split(",")[4].equals("-1")) {
                exercises = allProfiles[i].split(",")[4].split("\\.");
            }
        }

        if (exercises == null) {
            setContentView(R.layout.activity_break_no_exercises);
            Button cancelButton = (Button) findViewById(R.id.button_cancel);
            cancelButton.setOnClickListener(this);
            ct_text = (TextView) findViewById(R.id.textViewBreak1);

            ct_text.setText(bufferZeroMinute + mins + ":00");
            ct_text.setOnClickListener(this);
        } else {
            setContentView(R.layout.activity_break);
            Button nextButton = (Button) findViewById(R.id.button_next);
            nextButton.setOnClickListener(this);
            ct_text = (TextView) findViewById(R.id.textViewBreak);
            ct_text.setText(bufferZeroMinute + mins + ":00");
            ct_text.setOnClickListener(this);


            //TODO Iterate over all
            exerciseList = dbHandler.getExercisesFromSection(exercises[currentExercise]);
            description = (TextView) findViewById(R.id.textViewDescription);
            description.setText(exerciseList.get(currentExercise).getDescription());

            execution = (TextView) findViewById(R.id.textViewExecution);
            execution.setText(exerciseList.get(currentExercise).getExecution());

            side_repetition = (TextView) findViewById(R.id.textSideRepetition);
            side_repetition.setText(R.string.exercise_break);

            break_exercise_type = (TextView) findViewById(R.id.break_exercise_type);
            break_exercise_type.setText(exerciseList.get(currentExercise).getSection());

            image = (ImageView) findViewById(R.id.imageMid);
            image.setImageResource(R.drawable.train_left);
        }

        //Keep screen on while on break
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onClick(View v) {
        int mins = sharedPrefs.getInt("break_value", 10);
        String bufferZeroMinute = "";
        String bufferZeroSecond = "";
        int time = mins * 60 * 1000;

        if (stopTime == "" && !isRunning) {
            if (time / 1000 / 60 < 10)
                bufferZeroMinute = "0";

            ct_text.setText(bufferZeroMinute + time / 1000 / 60 + ":00");
        } else if (!isRunning) {
            ct_text.setText(stopTime);
            String stringTime = (String) ct_text.getText();
            String[] timef = stringTime.split(":");
            int minute = Integer.parseInt(timef[0]);
            int second = Integer.parseInt(timef[1]);
            System.out.println("Minute: " + minute + "  Second: " + second);
            time = (1000 * (minute * 60)) + (1000 * second);

            if (minute < 10)
                bufferZeroMinute = "0";
            if (second < 10)
                bufferZeroSecond = "0";

            ct_text.setText(bufferZeroMinute + minute + ":" + bufferZeroSecond + second);

        }


        System.out.println(time + " " + ct_text.getText());


        switch (v.getId()) {

            case R.id.textViewBreak1:
            case R.id.textViewBreak:
                if (isRunning) {
                    ct.cancel();
                    stopTime = (String) ct_text.getText();
                    isRunning = false;
                } else {
                    startTimer(time);
                }
                break;


            case R.id.button_cancel:
                if (ct != null)
                    ct.cancel();
                finish();
                break;

            case R.id.button_next:

                // Next Exercise
                currentExercise++;
                side_repetition.setText(R.string.exercise_break);
                if (currentExercise > exerciseList.size() - 1)
                    currentExercise = 0;
                description.setText(exerciseList.get(currentExercise).getDescription());
                execution.setText(exerciseList.get(currentExercise).getExecution());

                //Update Timer
                String[] currentTime = ((String) ct_text.getText()).split(":");

                int minute = Integer.parseInt(currentTime[0]);
                int second = Integer.parseInt(currentTime[1]);
                System.out.println("Current Minute: " + minute + " and current second: " + second);

                if (second != 0) {
                    ct.cancel();
                    breakTime = 0;
                    if (minute == 0 && second > 0) {
                        minute = 1;
                        second = 0;
                    } else if (minute > 0 && second > 0) {
                        minute++;
                        second = 0;
                    }
                    if (minute < 10)
                        bufferZeroMinute = "0";
                    if (second < 10)
                        bufferZeroSecond = "0";


                    System.out.println("New Time: " + bufferZeroMinute + minute + ":" + bufferZeroSecond + second);
                    if (isRunning) {
                        time = minute * 60 * 1000;
                        startTimer(time);
                    } else {
                        stopTime = bufferZeroMinute + minute + ":" + bufferZeroSecond + second;
                        ct_text.setText(stopTime);
                    }
                }

                break;
        }
    }

    private void update() {

        //FIXME Change to the correct picture and whether its side or repetetion
        breakTime++;
        switch (breakTime) {
            case 10:
                System.out.println("Time for Exercise: Left!");
                side_repetition.setText(R.string.exercise_repetition + " 1");
                break;
            case 30:
                System.out.println("Time for Break between sides!");
                side_repetition.setText(R.string.exercise_break);
                image.setImageResource(R.drawable.train_middle);
                break;
            case 40:
                System.out.println("Time for Exercise: Right!");
                side_repetition.setText(R.string.exercise_repetition + " 2");
                break;
            case 60:
                System.out.println("Next Exercise!");
                image.setImageResource(R.drawable.train_right);
                breakTime = 0;
                currentExercise++;
                if (currentExercise > exerciseList.size() - 1)
                    currentExercise = 0;
                description.setText(exerciseList.get(currentExercise).getDescription());
                execution.setText(exerciseList.get(currentExercise).getExecution());
                side_repetition.setText("Break");
                break;
        }
    }

    private void startTimer(int time) {

        ct = new CountDownTimer(time, 1000) {
            boolean timeLeft = false;

            public void onTick(long millisUntilFinished) {
                String bufferZeroMinute = "";
                String bufferZeroSecond = "";

                if ((millisUntilFinished / 1000) / 60 < 10)
                    bufferZeroMinute = "0";

                if (millisUntilFinished / 1000 % 60 < 10)
                    bufferZeroSecond = "0";

                ct_text.setText(bufferZeroMinute + (millisUntilFinished / 1000) / 60 + ":" + bufferZeroSecond + millisUntilFinished / 1000 % 60);

                // Update image and description of the exercise
                update();

//                //Show how much time is left
//                timeLeft = sharedPrefs.getBoolean("notifications_new_message_timeLeft", false);
//                if (timeLeft) {
//                    Notification notification = new NotificationCompat.Builder(getApplicationContext()).setCategory(Notification.CATEGORY_MESSAGE)
//                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
//                            .setContentTitle("Break Activity Reminder: ")
//                            .setContentText(((millisUntilFinished / 1000) / 60) + "Minutes and " + (millisUntilFinished / 1000 % 60) + " seconds")
//                            .setAutoCancel(true)
//                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();
//                    NotificationManager notificationManager =
//                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                    notificationManager.notify(999, notification);
//                }
            }

            public void onFinish() {
                isRunning = false;
                ct_text.setText("00:00");
                //Trigger the alarm
                String ringPref = sharedPrefs.getString("notifications_new_message_ringtone", "");

                if (!ringPref.equals("")) {
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(ringPref));
                    r.play();
                }

                //Vibration
                boolean vibrateChecked = sharedPrefs.getBoolean("notifications_new_message_vibrate", false);
                if (vibrateChecked) {
                    // Get instance of Vibrator from current Context
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    if (v != null) {
                        // Vibrate for 1500 milliseconds
                        v.vibrate(1500);
                    }
                }

                //Cancel the notification
                if (timeLeft) {
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(999);
                }
                //Remove lag to keep screen on when the break ends
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                finish();
            }
        }.start();
        isRunning = true;
    }
}
