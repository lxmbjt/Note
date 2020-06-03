package group3.sse.bupt.note.Alarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.ListView;
import android.widget.TimePicker;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import group3.sse.bupt.note.BaseActivity;
import group3.sse.bupt.note.CRUD;
import group3.sse.bupt.note.MainActivity;
import group3.sse.bupt.note.Note;
import group3.sse.bupt.note.R;


public class PlanActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private PlanDatabase dbHepler;

    private EditText editText;
    private View inflate;
    private AlertDialog modifyDialog;
    private AlertDialog date_time_picker;
    private Plan curPlan;
    private Button btn_confirmTime;
    private DatePicker datePickerStart;
    private TimePicker timePickerStart;
    private int mode=1;//1是添加，2是修改；

    //修改时间
    private String oldtime;
    private String newtime;
    private boolean changetime=false;





    private Button btn_setTime;
    private FloatingActionButton new_plan;

    private Context context=this;
    private ListView lv;
    private PlanAdapter adapter;
    private List<Plan> planList=new ArrayList<>();

    private Toolbar myToolbar;



    //系统提醒
    private AlarmManager alarmManager;
    AlarmUtils  alarmUtils;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.bottom_bar_note:
                    Intent intent=new Intent(PlanActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
                    PlanActivity.this.finish();
                    return true;
                case R.id.bottom_bar_plan:

                    return true;


            }



            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        myToolbar = findViewById(R.id.myToolbar);
        myToolbar.setTitle("待办");

        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar
        BottomNavigationView BottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        BottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigation.setSelectedItemId(R.id.bottom_bar_plan);

        //点击toolbar上的返回键，自动保存笔记内容并返回到主页面
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlanActivity.this.finish();

            }
        });
        //系统提醒
        alarmManager= (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmUtils=new AlarmUtils(context);
        //dialog
        AlertDialog.Builder alertbuidler = new AlertDialog.Builder(PlanActivity.this);
        inflate = LayoutInflater.from(this).inflate(R.layout.dialog_plan_edit, null);
        editText=inflate.findViewById(R.id.et_content);
        btn_setTime=inflate.findViewById(R.id.btn_time);
        alertbuidler.setView(inflate);
        //新建/修改plan框
        modifyDialog= alertbuidler.setPositiveButton("完成",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("hcccc","mode:"+mode);
                        switch (mode){
                            case 2:
                                Plan modifiedPlan;
                                if(isTimeChanged()&&changetime){
                                    Log.i("hcccc","newtime:"+newtime);

                                     modifiedPlan=new Plan(editText.getText().toString(), newtime,curPlan.getIsDone());
                                    changetime=false;

                                }
                                else {
                                    modifiedPlan = new Plan(editText.getText().toString(), curPlan.getTime(),curPlan.getIsDone());
                                    Log.i("hcccc","oldtime"+curPlan.getTime());
                                }
                                modifiedPlan.setId(curPlan.getId());
                                modifyPlan(modifiedPlan);
                                break;
                            case 1:
                                Plan newplan=new Plan();
                                int []dateArray=new int[3];
                                int []timeArray=new int[2];
                                dateArray[0]=newplan.getYear();
                                dateArray[1]=newplan.getMonth()+1;
                                dateArray[2]=newplan.getDay();
                                timeArray[0]=newplan.getHour();
                                timeArray[1]=newplan.getMinute();
                                oldtime=dateArray[0]+"-"+dateArray[1]+"-"+dateArray[2]+" "+timeArray[0]+":"+timeArray[1];
                                if(isTimeChanged()&&changetime){
                                    newplan.setTime(newtime);

                                    changetime=false;
                                }
                                else {
                                   newplan.setTime(newplan.getTime());
                                }
                                newplan.setContent(editText.getText().toString());
                                newPlan(newplan);
                                break;

                        }


                    }
                }
        ).create();



        //设定时间
        AlertDialog.Builder timebuilder = new AlertDialog.Builder(this);
        View timeView = View .inflate(this, R.layout.dialog_date_time, null);
        datePickerStart =  timeView .findViewById(R.id.date_picker);
        timePickerStart = timeView .findViewById(R.id.time_picker);
        timebuilder.setView(timeView);

        timePickerStart.setIs24HourView(true);
        hideYear(datePickerStart);


        date_time_picker=timebuilder.create();


        //修改时间确认按钮
        btn_confirmTime=timeView.findViewById(R.id.btn_confirmTime);
        btn_confirmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("hcccccc","选择的时间是："+(datePickerStart.getMonth()+1)+":"+datePickerStart.getDayOfMonth()
                +":"+timePickerStart.getHour()+":"+timePickerStart.getMinute());
                date_time_picker.dismiss();

                //date
                String temp=datePickerStart.getYear()+"-";
                if(datePickerStart.getMonth()+1<10) temp+="0";
                temp+=(datePickerStart.getMonth()+1)+"-";
                if(datePickerStart.getDayOfMonth()<10) temp+="0";
                temp+=datePickerStart.getDayOfMonth();

                //time
                temp+=" ";
                if(timePickerStart.getHour()<10) temp+="0";
                temp+=timePickerStart.getHour()+":";
                if(timePickerStart.getMinute()<10) temp+="0";
                temp+=timePickerStart.getMinute();
               // newtime=simpleDateFormat.format(temp);
                newtime=temp;
                changetime=true;

            }
        });


        lv=findViewById(R.id.lv);
        adapter=new PlanAdapter(getApplicationContext(),planList);
        refreshListView();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        new_plan=findViewById(R.id.new_plan);

        //新建Plan按钮
        new_plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode=1;
                editText.setText("");
                modifyDialog.show();

            }
        });

        //修改时间按钮
        btn_setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date_time_picker.show();
            }
        });

        //长按删除Plan
        AdapterView.OnItemLongClickListener ListViewLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                switch (parent.getId()) {
                    case R.id.lv:
                        new androidx.appcompat.app.AlertDialog.Builder(PlanActivity.this)
                                .setMessage("确定删除该事项吗？")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Plan clickedplan = (Plan) parent.getItemAtPosition(position);//当前
                                        DBConnector dbConnector=new DBConnector(context);
                                        dbConnector.open();
                                        dbConnector.removePlan(clickedplan);
                                        dbConnector.close();
                                        refreshListView();

                                    }
                                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();//关闭对话框
                            }
                        }).create().show();

                        return true;
                }
                return false;
            }
        };
        lv.setOnItemLongClickListener(ListViewLongClickListener);

    }






    //隐藏年份
    private void hideYear(DatePicker datePicker) {
        //安卓5.0以上的处理
        int daySpinnerId = Resources.getSystem().getIdentifier("year", "id", "android");
        if (daySpinnerId != 0) {
            View daySpinner = datePicker.findViewById(daySpinnerId);
            if (daySpinner != null) {
                daySpinner.setVisibility(View.GONE);
            }
        }

//        Field[] datePickerFields = datePicker.getClass().getDeclaredFields();
//        for (Field field : datePickerFields) {
//            // 其中mYearSpinner为DatePicker中为“年”定义的变量名
//            if (field.getName().equals("mYearPicker")
//                    || field.getName().equals("mYearSpinner")) {
//                Log.i("hcccc","myearspinner found");
//                field.setAccessible(true);
//                Object dayPicker = new Object();
//                try {
//                    dayPicker = field.get(datePicker);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                }
//                ((View) dayPicker).setVisibility(View.GONE);
//            }
//        }
    }

    //新添加的plan提交到数据库
    public void newPlan(Plan newPlan){
        DBConnector dbConnector=new DBConnector(context);
        dbConnector.open();
        dbConnector.addPlan(newPlan);
        dbConnector.close();
        refreshListView();
    }

    //修改后的plan提交到数据库
    public void modifyPlan(Plan plan){
        DBConnector dbConnector=new DBConnector(context);
        dbConnector.open();
        dbConnector.updatePlan(plan);
        dbConnector.close();
        refreshListView();
    }

    //添加Plan后刷新界面
    public void refreshListView(){
        DBConnector dbConnector=new DBConnector(context);
        dbConnector.open();
        if(planList.size()>0) {
            alarmUtils.cancelAlarms(planList);
            planList.clear();
        }
        planList.addAll(dbConnector.getAllPlans());
        alarmUtils.startAlarms(planList);

        dbConnector.close();
        adapter.notifyDataSetChanged();
    }

    public boolean isTimeChanged(){
        if(oldtime.equals(newtime))
            return false;
        else
            return true;
    }

    //修改plan
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mode=2;
        //通过dialog修改plan的内容
        switch (parent.getId()){
          case R.id.lv:
              Log.i("hcccc","onclicked");
              curPlan=(Plan) parent.getItemAtPosition(position);
              editText.setText(curPlan.getContent());
              oldtime= String.valueOf(curPlan.getPlanTime());
              modifyDialog.show();
              break;
      }


    }

    @Override
    protected void needRefresh() {
        setNightMode();
        Intent intent = new Intent(this, PlanActivity.class);
        intent.putExtra("opMode", 10);
        startActivity(intent);
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        finish();
    }
}
