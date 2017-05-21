package ga.findparty.findparty.util;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ga.findparty.findparty.R;

/**
 * Created by tw on 2017-03-28.
 */

public class AdditionalFunc {

    public static void showSnackbar(Activity activity, String msg){
        Snackbar snackbar = Snackbar.make(activity.getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }
    public static void showSnackbar(View v, Context context, String msg){
        Snackbar snackbar = Snackbar.make(v, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }

    public static String replaceNewLineString(String s){

        String str = s.replaceAll("\n", "\\\\n");
        return str;

    }

    public static long getMilliseconds(int year, int month, int day){

        long days = 0;

        try {
            String cdate = String.format("%d%02d%02d", year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date = sdf.parse(cdate);
            days = date.getTime();
            System.out.println(days);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;

    }

    public static long getTodayMilliseconds(){
        Calendar now = Calendar.getInstance();
        return getMilliseconds(now.get(Calendar.YEAR), now.get(Calendar.MONTH)+1, now.get(Calendar.DAY_OF_MONTH));
    }

    public static int getDday(long eTime){

        long cTime = System.currentTimeMillis();
        Date currentDate = new Date(cTime);
        Date finishDate = new Date(eTime);
//        System.out.println(cTime + ", " + eTime);

        DateFormat df = new SimpleDateFormat("yyyy");
        int currentYear = Integer.parseInt(df.format(currentDate));
        int finishYear = Integer.parseInt(df.format(finishDate));
        df = new SimpleDateFormat("MM");
        int currentMonth = Integer.parseInt(df.format(currentDate));
        int finishMonth = Integer.parseInt(df.format(finishDate));
        df = new SimpleDateFormat("dd");
        int currentDay = Integer.parseInt(df.format(currentDate));
        int finishDay = Integer.parseInt(df.format(finishDate));

//        System.out.println(currentYear + ", " + currentMonth + ", " + currentDay);
//        System.out.println(finishYear + ", " + finishMonth + ", " + finishDay);

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.set(currentYear, currentMonth, currentDay);
        end.set(finishYear, finishMonth, finishDay);

        Date startDate = start.getTime();
        Date endDate = end.getTime();

        long startTime = startDate.getTime();
        long endTime = endDate.getTime();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (1000 * 60 * 60 * 24);


        return (int)diffDays;
    }

    public static String getDateString(long time){

        Date currentDate = new Date(time);
        DateFormat df = new SimpleDateFormat("yyyy년 MM월 dd일");
        return df.format(currentDate);

    }

    public static ArrayList<String> stringToArrayList(String str){

        ArrayList<String> list = new ArrayList<>();

        for(String s : str.split(",")){
            if(!"".equals(s)){
                list.add(s);
            }
        }

        return list;
    }

    public static String arrayListToString(ArrayList<String> list) {

        String str = "";
        for (int i = 0; i < list.size(); i++) {
            str += list.get(i);
            if (i + 1 < list.size()) {
                str += ",";
            }
        }

        return str;

    }

    public static String integerArrayListToString(ArrayList<Integer> list){

        String str = "";
        for(int i=0; i<list.size(); i++){
            str += list.get(i);
            if(i+1<list.size()){
                str += ",";
            }
        }
        return str;
    }

    public static String addFieldToString(ArrayList<HashMap<String, String>> list){

        String str = "";

        for(int i=0; i<list.size(); i++){

            HashMap<String, String> item = list.get(i);
            str += (item.get("title") + ":" + item.get("number"));

            if(i<list.size()-1){
                str += ";";
            }

        }

        return str;

    }

    public static String interestListToString(ArrayList<String> list){

        String str = "";

        for(String s : list){
            str += (" # " + s);
        }

        return str;

    }

    public static String makeBoardFieldIdListToString(ArrayList<HashMap<String, Object>> list){

        String str = "";
        for(int i=0; i<list.size(); i++){

            HashMap<String, Object> map = list.get(i);
            str += (String)map.get("id");

            if(i<list.size()-1){
                str += ",";
            }
        }

        return str;

    }

    public static String makeHistoryMeetingParticipantList(HashMap<String, String> map){

        String str="";

        int i=0;
        for(String key : map.keySet()){
            String status = map.get(key);

            str = str + key + ":" + status;

            if(i < map.keySet().size()-1){
                str += ",";
            }

            i+=1;
        }

        return str;

    }


    public static HashMap<String, Object> getUserInfo(String data){

        HashMap<String, Object> item = new HashMap<>();

        try {
            // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
            JSONObject jObject = new JSONObject(data);
            // results라는 key는 JSON배열로 되어있다.
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

//                HashMap<String, String> hashTemp = new HashMap<>();
            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

//                        HashMap<String, String> hashTemp = new HashMap<>();
                item.put("id", (String)temp.get("id"));
                item.put("name", (String)temp.get("name"));
                item.put("email", (String)temp.get("email"));
                item.put("img", (String)temp.get("img"));
                item.put("school", (String) temp.get("school"));
                item.put("studentId", (String) temp.get("studentId"));
                item.put("contact", (String)temp.get("contact"));
                item.put("intro", (String)temp.get("intro"));

                ArrayList<String> in = new ArrayList<String>();
                String interest = (String)temp.get("interest");
                if(!interest.equals("")){
                    for(String s : interest.split(",")){
                        in.add(s);
                    }
                }
                item.put("interest", in);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            item.clear();
        }

        return item;

    }

    public static ArrayList<HashMap<String, String>> getCourseInfo(String data){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, String> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("school", (String)temp.get("school"));
                hashTemp.put("department", (String)temp.get("department"));
                hashTemp.put("no", (String)temp.get("no"));
                hashTemp.put("class", (String)temp.get("class"));
                hashTemp.put("title", (String)temp.get("title"));
                hashTemp.put("classification", (String)temp.get("classification"));
                hashTemp.put("day", (String)temp.get("day"));
                hashTemp.put("room", (String) temp.get("room"));
                hashTemp.put("lecturer", (String) temp.get("lecturer"));

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, String>> getUserCourseInfo(String data){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, String> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("courseId", (String)temp.get("courseId"));
                hashTemp.put("school", (String)temp.get("school"));
                hashTemp.put("department", (String)temp.get("department"));
                hashTemp.put("no", (String)temp.get("no"));
                hashTemp.put("class", (String)temp.get("class"));
                hashTemp.put("title", (String)temp.get("title"));
                hashTemp.put("classification", (String)temp.get("classification"));
                hashTemp.put("day", (String)temp.get("day"));
                hashTemp.put("room", (String) temp.get("room"));
                hashTemp.put("lecturer", (String) temp.get("lecturer"));

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getBoardListInfo(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("courseId", (String)temp.get("courseId"));
                hashTemp.put("userId", (String)temp.get("userId"));
                hashTemp.put("name", (String)temp.get("name"));
                hashTemp.put("img", (String)temp.get("img"));
                hashTemp.put("email", (String)temp.get("email"));
                hashTemp.put("content", (String)temp.get("content"));
                hashTemp.put("start", Long.parseLong((String)temp.get("start")));
                hashTemp.put("finish", Long.parseLong((String)temp.get("finish")));
                hashTemp.put("current", Integer.parseInt((String)temp.get("current")));
                hashTemp.put("total", Integer.parseInt((String)temp.get("total")));
                hashTemp.put("isDecision", Integer.parseInt((String)temp.get("isDecision")));

                ArrayList<String> question = new ArrayList<>();
                String question1 = (String)temp.get("question1");
                String question2 = (String)temp.get("question2");
                String question3 = (String)temp.get("question3");
                if(!"".equals(question1)){
                    question.add(question1);
                    if(!"".equals(question2)){
                        question.add(question2);
                        if(!"".equals(question3)){
                            question.add(question3);
                        }
                    }
                }
                hashTemp.put("question", question);

                ArrayList<String> in = new ArrayList<String>();
                String interest = (String)temp.get("interest");
                if(!interest.equals("")){
                    for(String s : interest.split(",")){
                        in.add(s);
                    }
                }
                hashTemp.put("interest", in);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getBoardFieldListInfo(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("boardId", (String)temp.get("boardId"));
                hashTemp.put("field", (String)temp.get("field"));
                hashTemp.put("number", Integer.parseInt((String)temp.get("number")));

                String participant = (String)temp.get("participant");
                ArrayList<String> par = new ArrayList<>();
                if(!participant.equals("")){
                    String[] p = participant.split(",");

                    for(String s : p){
                        par.add(s);
                    }
                }
                if (par.size() > 0 && par.get(0).equals("")) {
                    par.remove(0);
                }
                hashTemp.put("participant", par);

                String member = (String)temp.get("member");
                ArrayList<String> mem = new ArrayList<>();
                if(!member.equals("")){
                    String[] p = member.split(",");

                    for(String s : p){
                        mem.add(s);
                    }
                }
                if (mem.size() > 0 && mem.get(0).equals("")) {
                    mem.remove(0);
                }
                hashTemp.put("member", mem);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static String[] getSampleQuestionList(String data){

        String[] list;

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            list = new String[count];

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                list[i] = (String)temp.get("content");

            }

        } catch (JSONException e) {
            e.printStackTrace();
            list = new String[0];
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getApplyParticipantList(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("courseId", (String)temp.get("courseId"));
                hashTemp.put("boardFieldId", (String)temp.get("boardFieldId"));
                hashTemp.put("skill", Integer.parseInt((String)temp.get("skill")));
                hashTemp.put("content", (String)temp.get("content"));
                hashTemp.put("userId", (String)temp.get("userId"));
                hashTemp.put("name", (String)temp.get("name"));
                hashTemp.put("img", (String)temp.get("img"));
                hashTemp.put("email", (String)temp.get("email"));

                ArrayList<String> answer = new ArrayList<>();
                String answer1 = (String)temp.get("answer1");
                String answer2 = (String)temp.get("answer2");
                String answer3 = (String)temp.get("answer3");
                if(!"".equals(answer1)){
                    answer.add(answer1);
                    if(!"".equals(answer2)){
                        answer.add(answer2);
                        if(!"".equals(answer3)){
                            answer.add(answer3);
                        }
                    }
                }
                hashTemp.put("answer", answer);

                String mon = (String)temp.get("mon");
                ArrayList<Integer> monList = new ArrayList<>();
                for(String s : mon.split(",")){
                    if("".equals(s)){
                        break;
                    }
                    monList.add(Integer.parseInt(s));
                }
                hashTemp.put("mon", monList);

                String tue = (String)temp.get("tue");
                ArrayList<Integer> tueList = new ArrayList<>();
                for(String s : tue.split(",")){
                    if("".equals(s)){
                        break;
                    }
                    tueList.add(Integer.parseInt(s));
                }
                hashTemp.put("tue", tueList);

                String wed = (String)temp.get("wed");
                ArrayList<Integer> wedList = new ArrayList<>();
                for(String s : wed.split(",")){
                    if("".equals(s)){
                        break;
                    }
                    wedList.add(Integer.parseInt(s));
                }
                hashTemp.put("wed", wedList);

                String thu = (String)temp.get("thu");
                ArrayList<Integer> thuList = new ArrayList<>();
                for(String s : thu.split(",")){
                    if("".equals(s)){
                        break;
                    }
                    thuList.add(Integer.parseInt(s));
                }
                hashTemp.put("thu", thuList);

                String fri = (String)temp.get("fri");
                ArrayList<Integer> friList = new ArrayList<>();
                for(String s : fri.split(",")){
                    if("".equals(s)){
                        break;
                    }
                    friList.add(Integer.parseInt(s));
                }
                hashTemp.put("fri", friList);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getTeamList(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                String boardUserId = (String)temp.get("boardUserId");
                String boardUserName = (String)temp.get("boardUserName");
                String boardUserEmail = (String)temp.get("boardUserEmail");
                String boardUserImg = (String)temp.get("boardUserImg");

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("boardId", (String)temp.get("boardId"));
                hashTemp.put("boardStart", Long.parseLong((String)temp.get("boardStart")));
                hashTemp.put("boardFinish", Long.parseLong((String)temp.get("boardFinish")));
                hashTemp.put("boardUserId", boardUserId);
                hashTemp.put("boardUserName", boardUserName);
                hashTemp.put("boardUserEmail", boardUserEmail);
                hashTemp.put("boardUserImg", boardUserImg);
                hashTemp.put("table", "teamTbl");
                hashTemp.put("review", (String)temp.get("review"));
                String courseId = (String)temp.get("courseId");
                hashTemp.put("courseId", courseId);
                hashTemp.put("courseTitle", (String)temp.get("courseTitle"));
                hashTemp.put("courseClass", (String)temp.get("courseClass"));

                HashMap<String, Object> memberMap = new HashMap<>();
                memberMap.put("userId", boardUserId);
                memberMap.put("name", boardUserName);
                memberMap.put("email", boardUserEmail);
                memberMap.put("img", boardUserImg);
                memberMap.put("field", "대표");
                hashTemp.put("boardUserMap", memberMap);

                String member = (String)temp.get("member");
                ArrayList<String> mem = new ArrayList<>();
                if(!member.equals("")){
                    String[] p = member.split(",");

                    for(String s : p){
                        mem.add(s);
                    }
                }
                if (mem.size() > 0 && mem.get(0).equals("")) {
                    mem.remove(0);
                }
                hashTemp.put("member", mem);

                JSONObject jObjectMem = (JSONObject)temp.get("memberList");
                JSONArray resultsMem = jObjectMem.getJSONArray("result");
                String countTempMem = (String)jObjectMem.get("num_member");
                int countMem = Integer.parseInt(countTempMem);

                ArrayList<HashMap<String, Object>> memberList = new ArrayList<>();
                for(int j=0; j<countMem; j++){

                    JSONObject tempMem = resultsMem.getJSONObject(j);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", (String)tempMem.get("applyFormId"));
                    map.put("courseId", courseId);
                    map.put("boardFieldId", (String)tempMem.get("boardFieldId"));
                    map.put("userId", (String)tempMem.get("memberUserId"));
                    map.put("name", (String)tempMem.get("memberName"));
                    map.put("email", (String)tempMem.get("memberEmail"));
                    map.put("img", (String)tempMem.get("memberImg"));
                    map.put("field", (String)tempMem.get("field"));
                    map.put("skill", Integer.parseInt((String)tempMem.get("skill")));
                    map.put("content", (String)tempMem.get("content"));
                    map.put("table", "applyFormTbl");
                    map.put("review", (String)tempMem.get("review"));

                    String mon = (String)tempMem.get("mon");
                    ArrayList<Integer> monList = new ArrayList<>();
                    for(String s : mon.split(",")){
                        if("".equals(s)){
                            break;
                        }
                        monList.add(Integer.parseInt(s));
                    }
                    map.put("mon", monList);

                    String tue = (String)tempMem.get("tue");
                    ArrayList<Integer> tueList = new ArrayList<>();
                    for(String s : tue.split(",")){
                        if("".equals(s)){
                            break;
                        }
                        tueList.add(Integer.parseInt(s));
                    }
                    map.put("tue", tueList);

                    String wed = (String)tempMem.get("wed");
                    ArrayList<Integer> wedList = new ArrayList<>();
                    for(String s : wed.split(",")){
                        if("".equals(s)){
                            break;
                        }
                        wedList.add(Integer.parseInt(s));
                    }
                    map.put("wed", wedList);

                    String thu = (String)tempMem.get("thu");
                    ArrayList<Integer> thuList = new ArrayList<>();
                    for(String s : thu.split(",")){
                        if("".equals(s)){
                            break;
                        }
                        thuList.add(Integer.parseInt(s));
                    }
                    map.put("thu", thuList);

                    String fri = (String)tempMem.get("fri");
                    ArrayList<Integer> friList = new ArrayList<>();
                    for(String s : fri.split(",")){
                        if("".equals(s)){
                            break;
                        }
                        friList.add(Integer.parseInt(s));
                    }
                    map.put("fri", friList);

                    memberList.add(map);

                }

                hashTemp.put("memberList", memberList);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getHistoryList(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("teamId", (String)temp.get("teamId"));
                hashTemp.put("userId", (String)temp.get("userId"));
                hashTemp.put("name", (String)temp.get("name"));
                hashTemp.put("email", (String)temp.get("email"));
                hashTemp.put("img", (String)temp.get("img"));
                hashTemp.put("classification", (String)temp.get("classification"));
                hashTemp.put("title", (String)temp.get("title"));
                hashTemp.put("content", (String)temp.get("content"));
                hashTemp.put("date", Long.parseLong((String)temp.get("date")));

                String referenceTemp = (String)temp.get("reference");
                String[] reference = {};
                if(!"".equals(referenceTemp)){
                    reference = referenceTemp.split(",");
                }
                hashTemp.put("reference", reference);

                JSONObject jObjectMem = (JSONObject)temp.get("participant");
                JSONArray resultsMem = jObjectMem.getJSONArray("result");
                String countTempMem = (String)jObjectMem.get("num_participant");
                int countMem = Integer.parseInt(countTempMem);

                ArrayList<HashMap<String, Object>> participant = new ArrayList<>();
                for(int j=0; j<countMem; j++){

                    JSONObject tempMem = resultsMem.getJSONObject(j);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("userId", (String)tempMem.get("userId"));
                    map.put("name", (String)tempMem.get("name"));
                    map.put("email", (String)tempMem.get("email"));
                    map.put("img", (String)tempMem.get("img"));
                    map.put("status", (String)tempMem.get("status"));

                    participant.add(map);

                }

                hashTemp.put("participant", participant);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, String>> getRecommendList(String data){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, String> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("userId", (String)temp.get("userId"));
                hashTemp.put("name", (String)temp.get("userName"));
                hashTemp.put("img", (String)temp.get("userImg"));
                hashTemp.put("email", (String)temp.get("userEmail"));
                hashTemp.put("recipientId", (String)temp.get("recipientId"));
                hashTemp.put("field", (String)temp.get("field"));

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, String>> getUserRecommendList(String data){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, String> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("userId", (String)temp.get("userId"));
                hashTemp.put("recipientId", (String)temp.get("recipientId"));
                hashTemp.put("name", (String)temp.get("recipientName"));
                hashTemp.put("img", (String)temp.get("recipientImg"));
                hashTemp.put("email", (String)temp.get("recipientEmail"));
                hashTemp.put("field", (String)temp.get("field"));

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getQAListItem(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("question", (String)temp.get("question"));

                JSONObject jObjectAns = (JSONObject)temp.get("answer");
                JSONArray resultsAns = jObjectAns.getJSONArray("result");
                String countTempAns = (String)jObjectAns.get("num_answer");
                int countAns = Integer.parseInt(countTempAns);

                String[] answerList = new String[countAns];

                for(int j=0; j<countAns; j++){

                    JSONObject tempans = resultsAns.getJSONObject(j);

                    String ans = (String) tempans.get("ans");
                    answerList[j] = ans;

                }

                hashTemp.put("answer", answerList);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static HashMap<String, Object> getQAHashItem(String data){

        HashMap<String, Object> list = new HashMap<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                String id = (String)temp.get("id");
                HashMap<String, Object> hashTemp = new HashMap<>();
                //hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("question", (String)temp.get("question"));

                JSONObject jObjectAns = (JSONObject)temp.get("answer");
                JSONArray resultsAns = jObjectAns.getJSONArray("result");
                String countTempAns = (String)jObjectAns.get("num_answer");
                int countAns = Integer.parseInt(countTempAns);

                String[] answerList = new String[countAns];

                for(int j=0; j<countAns; j++){

                    JSONObject tempans = resultsAns.getJSONObject(j);

                    String ans = (String) tempans.get("ans");
                    answerList[j] = ans;

                }

                hashTemp.put("total", 0);
                hashTemp.put("answer", answerList);
                hashTemp.put("answerCount", new int[answerList.length]);

                list.put(id, hashTemp);
                //list.add(hashTemp);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, Object>> getUserReviewListItem(String data){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, Object> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("targetId", (String)temp.get("targetId"));
                hashTemp.put("date", Long.parseLong((String)temp.get("date")));
                if("1".equals((String)temp.get("isSecret"))){
                    hashTemp.put("isSecret", true);
                }else{
                    hashTemp.put("isSecret", false);
                }

                String content = (String)temp.get("content");
                String[] contentListTemp = content.split(";");
                ArrayList<String[]> contentList = new ArrayList<>();
                for(int k=0; k<contentListTemp.length-1; k++){
                    String s = contentListTemp[k];
                    contentList.add(s.split(":"));
                }
                hashTemp.put("content", contentList);

                hashTemp.put("writeAnswer", contentListTemp[contentListTemp.length-1].split(":"));

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ArrayList<HashMap<String, String>> getRecommendCourseUserList(String data){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        try {
            JSONObject jObject = new JSONObject(data);
            JSONArray results = jObject.getJSONArray("result");
            String countTemp = (String)jObject.get("num_result");
            int count = Integer.parseInt(countTemp);

            for ( int i = 0; i < count; ++i ) {
                JSONObject temp = results.getJSONObject(i);

                HashMap<String, String> hashTemp = new HashMap<>();
                hashTemp.put("id", (String)temp.get("id"));
                hashTemp.put("name", (String)temp.get("name"));
                hashTemp.put("img", (String)temp.get("img"));
                hashTemp.put("email", (String)temp.get("email"));

                String interest = (String)temp.get("interest");
                String[] interestList = interest.split(",");
                interest = "";
                for(int k=0; k<interestList.length; k++){
                    interest += ("# " + interestList[k]);
                    if(k < interestList.length-1){
                        interest += " ";
                    }
                }
                hashTemp.put("interest", interest);

                list.add(hashTemp);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;

    }

}
