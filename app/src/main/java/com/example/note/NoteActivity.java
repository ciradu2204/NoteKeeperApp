package com.example.note;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSTION = "com.com.example.note.NOTE_Postion";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsnewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteposition;
    private Boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                 ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(savedInstanceState !=null && mViewModel.mIsNewlyCreated)
            mViewModel.restoreState(savedInstanceState);

        mViewModel.mIsNewlyCreated =false;


        mSpinnerCourses = findViewById(R.id.spinner_course);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);
        
        readDisplayStateValues();
        SaveOriginalNoteValues();


        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text );
        if(!mIsnewNote)
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

    

    }

    private void SaveOriginalNoteValues() {

        if(mIsnewNote){

            mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
            mViewModel.mOriginalNoteTitle = mNote.getTitle();
            mViewModel.mOriginalNoteText = mNote.getText();
        }

    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());

    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        // if it doesn't find the extra, then it will return -1
        int position = intent.getIntExtra(NOTE_POSTION, POSITION_NOT_SET);
        mIsnewNote = position == POSITION_NOT_SET;

        if(mIsnewNote){
           createNewNote();
        }else{
            mNote = DataManager.getInstance().getNotes().get(position);
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNoteposition = dm.createNewNote();
        mNote = dm.getNotes().get(mNoteposition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
             if(mIsnewNote) {
                 DataManager.getInstance().removeNote(mNoteposition);
             }else{
                 storePreviousNoteValue();
             }
        }else{
            saveNote();
        }

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(savedInstanceState != null){

            mViewModel.saveState(savedInstanceState);

        }


    }

    private void storePreviousNoteValue() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteTitle.getText().toString());


    }

    @Override
    //this one gets called each time the user calls the menu option
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        // Step1: first specify the id needed
        if (id == R.id.action_send_email) {
            sendEmail();
            return true;
        }else if(id== R.id.action_cancel){

            mIsCancelling = true;
            finish();




        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
//        String email ="c.iradukund@alustudent.com";
        String subject  = mTextNoteTitle.getText().toString();
        String body = "Checkout what I learned in the Pluralsight course \"" + course.getTitle() + "\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        //we need to specify which type of send action like mime type
        intent.setType("message/rfc2822");
//        intent.putExtra(Intent.EXTRA_EMAIL, email);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(intent);

    }
}