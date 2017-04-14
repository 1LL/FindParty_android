package ga.findparty.findparty;

import java.util.HashMap;

/**
 * Created by tw on 2017-03-28.
 */

public class Information {

    public static String MAIN_SERVER_ADDRESS = "http://findparty.ga/service.php";

    public static final String NAVER_CLIENT_ID = "acDBC9H1QNtS3d0TvGW2";
    public static final String NAVER_CLIENT_SECRET = "nRpN2G2P_l";

    // 관리자 이메일
    public static String ADMINISTRATOR_EMAIL = "ldaytw@gmail.com";

    public static String[] RECOMMEND_FIELD_LIST = {
            "팀장",
            "발표",
            "자료관리",
            "자료조사",
            "PPT제작",
            "커뮤니케이션",
            "무임승차"
    };

    public static String[] SCHOOL_LIST = {
            "세종대학교"
    };
    public static String getSchoolCode(String name){
        switch (name){
            case "세종대학교":
                return "sju";
            default:
                return "none";
        }
    }
    public static String getSchoolName(String code){
        switch (code){
            case "sju":
                return "세종대학교";
            default:
                return "none";
        }
    }

}
