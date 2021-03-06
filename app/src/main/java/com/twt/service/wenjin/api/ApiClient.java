package com.twt.service.wenjin.api;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.twt.service.wenjin.R;
import com.twt.service.wenjin.WenJinApp;
import com.twt.service.wenjin.support.DeviceUtils;
import com.twt.service.wenjin.support.LogHelper;
import com.twt.service.wenjin.support.MD5Utils;
import com.twt.service.wenjin.support.PrefUtils;
import com.twt.service.wenjin.support.ResourceHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.protocol.ClientContext;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import cz.msebera.android.httpclient.protocol.HttpContext;

/**
 * Created by M on 2015/3/23.
 */
public class ApiClient {

    /*
    接口总是会返回一个json，包含三个字段
    rsm：成功时包含返回的数据，失败时此字段为null
    errno：1-成功，2-失败
    err：成功时为null，失败时包含错误原因，可原样输出
     */
    public static final String RESP_MSG_KEY = "rsm";
    public static final String RESP_ERROR_CODE_KEY = "errno";
    public static final String RESP_ERROR_MSG_KEY = "err";

    public static final String PARAM_SIGN = "mobile_sign";
    public static final String PARAM_TIME = "timestamp";
    public static final String PARAM_NONCE = "nonce";

    public static final int SUCCESS_CODE = 1;
    public static final int ERROR_CODE = -1;

    private static AsyncHttpClient sClient = null;
    private static PersistentCookieStore sCookieStore = new PersistentCookieStore(WenJinApp.getContext());
    public static final int DEFAULT_TIMEOUT = 20000;

    private static final String BASE_URL = "http://api.wenjin.im/";
    private static final String BASE_IMG_URL = "http://wenjin.im/";
//    private static final String BASE_URL = "http://wenjin.test.twtstudio.com/";
    private static final String TOKEN_URL = "v2/inbox/get_token/";
    private static final String LOGIN_URL = "v2/account/login_process/";
    public static final  String GREEN_CHANNEL_URL = "http://wenjin.im/account/green/";
    private static final String HOME_URL = "v2/home/";
    private static final String EXPLORE_URL = "v2/explore/";
    private static final String TOPIC_URL = "v2/topic/hot_topics/";
    private static final String FOCUS_TOPIC_LIST_URL = "v2/people/topics/";
    private static final String TOPIC_DETAIL_URL = "v2/topic/topic/";
    private static final String TOPIC_BEST_ANSWER = "v2/topic/topic_best_answer_list/";
    private static final String FOCUS_TOPIC_URL = "v2/topic/focus_topic/";
    private static final String MY_FOLLOWS_USER_URL = "v2/people/follows/";
    private static final String QUESTION_URL = "v2/question/";
    private static final String FOCUS_QUESTION_URL = "/question/ajax/focus/";
    private static final String ANSWER_DETAIL_URL = "v2/question/answer/";
    private static final String ANSWER_VOTE_URL = "v2/question/answer_vote/";
    private static final String ANSWER_THANK_URL = "v2/question/question_answer_rate/";
    private static final String UPLOAD_FILE_URL = "v2/publish/attach_upload/";
    private static final String PUBLISH_QUESTION_URL = "v2/publish/publish_question/";
    private static final String ANSWER_URL = "v2/publish/save_answer/";
    private static final String USER_INFO_URL = "v2/account/get_userinfo/";
    private static final String FOCUS_USER_URL = "v2/people/follow_people/";
    private static final String COMMENT_URL = "v2/question/answer_comments/";
    private static final String PUBLISH_COMMENT_URL = "v2/question/save_answer_comment/";
    private static final String MY_ACTIONS_URL = "v2/people/user_actions/";
    private static final String MY_ANSWER_URL = "v2/my_answer.php";
    private static final String MY_QUESTION_URL = "v2/my_question.php";
    private static final String FEEDBACK_URL = "v2/ticket/publish/";
    private static final String CHECK_UPDATE_URL = "?/api/update/check/";
    private static final String PROFILE_EDIT_URL = "v2/people/profile_setting/";
    private static final String ARTICLE_ARTICLE_URL = "v2/article/";
    private static final String ARTICLE_COMMENT_URL = "v2/article/article_comments/";
    private static final String PUBLISH_ARTICLE_COMMENT_URL = "v2/article/save_comment/";
    private static final String ARTICLE_VOTE_URL = "v2/article/article_vote/";
    private static final String AVATAR_UPLOAD_URL = "v2/account/avatar_upload/";
    private static final String SEARCH_URL = "v2/search/";

    private static final String NOTIFICATIONS_URL = "v2/notification/notifications/";
    private static final String NOTIFICATIONS_LIST_URL = "v2/notification/list/";
    private static final String NOTIFICATIONS_MARKASREAD_URL = "v2/notification/read_notification/";

//    static {
//        sClient.setTimeout(DEFAULT_TIMEOUT);
//        sClient.setCookieStore(sCookieStore);
//        sClient.addHeader("User-Agent", getUserAgent());
//        Log.d("ApiClient", "static initializer: started ");
//    }
    public static void createClient(){
        sClient = new AsyncHttpClient();
        sClient.setTimeout(DEFAULT_TIMEOUT);
        for (int i = 0; i < sCookieStore.getCookies().size(); i++) {
            Log.d("lqy",sCookieStore.getCookies().get(i).getName());
        }
        sClient.setCookieStore(sCookieStore);
        sClient.addHeader("User-Agent", getUserAgent());
        Log.d("ApiClient", "static initializer: started ");
    }
    public static AsyncHttpClient getInstance() {
        return sClient;
    }

    public static String getUserAgent() {
        // User-Agent Wenjin/1.0.2 (Adnroid; 4.4.4; ...)
        String isRooted = DeviceUtils.isRooted() ? "rooted" : "unrooted";
        String userAgent = "Wenjin/" + DeviceUtils.getVersionName() + " (" +
                "Android; " +
                DeviceUtils.getSystemVersion() + ")";
        return userAgent;
    }

    public static void userLogin(String username, String password, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("user_name", username);
        params.put("password", password);

        sClient.post(BASE_URL + buildPostSignatureToURL(LOGIN_URL,false), params, handler);
    }

    public static void userLogout() {
        sCookieStore.clear();
        PrefUtils.setLogin(false);
    }

    public static void uploadFile(String type, String attachKey, File file, JsonHttpResponseHandler handler) {
        Uri url = Uri.parse(BASE_URL + UPLOAD_FILE_URL).buildUpon()
                .appendQueryParameter("id", type)
                .appendQueryParameter("attach_access_key", attachKey)
                .build();

        RequestParams params = new RequestParams();
        try {
            params.put("qqfile", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        sClient.post(url.toString(), params, handler);
    }

    public static void publishQuestion(String title, String content, String attachKey, String topics, boolean isAnonymous, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("question_content", title);
        params.put("question_detail", content);
        params.put("attach_access_key", attachKey);
        params.put("topics", topics);
        if (isAnonymous) {
            params.put("anonymous", 1);
        } else {
            params.put("anonymous", 0);
        }

        sClient.post(BASE_URL + buildPostSignatureToURL(PUBLISH_QUESTION_URL,false), params, handler);
    }

    public static void getHome(int perPage, int page, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, HOME_URL);
        params.put("per_page", perPage);
        params.put("page", page);
        //test
        sClient.setCookieStore(sCookieStore);
        sClient.get(BASE_URL + HOME_URL, params, handler);
    }

    /*
    获取私信token
     */
    public static void getToken(JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, TOKEN_URL);
        sClient.get(BASE_URL + TOKEN_URL,params, handler);

    }


    public static void searchContent(String content,String type, int page, int limit, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, SEARCH_URL);
        params.put("q",content);
        params.put("page", page);
        params.put("limit",limit);
        params.put("type", type);

        sClient.get(BASE_URL + SEARCH_URL, params, handler);
    }

    public static void getExplore(int perPage, int page, int day, int isRecommend, String sortType, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, EXPLORE_URL);
        params.put("per_page", perPage);
        params.put("page", page);
        params.put("day", day);
        params.put("is_recommend", isRecommend);
        params.put("sort_type", sortType);

        sClient.setCookieStore(sCookieStore);
        //测试cookie代码
        HttpContext httpContext=sClient.getHttpContext();
        CookieStore cookies= (CookieStore) httpContext.getAttribute(ClientContext.COOKIE_STORE);
        if (cookies!=null)
        {
            for (Cookie c:cookies.getCookies()) {
                Log.d("ApiClient", "cookie: "+c.getName()+"--->"+c.getValue()+"--->"+c.getExpiryDate() + "--->" + c.getDomain());
            }
        }
        sClient.get(BASE_URL + EXPLORE_URL, params, handler);

    }

    public static void getTopics(String day, int page, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, TOPIC_URL);
        params.put("day", day);
        params.put("page", page);

        sClient.get(BASE_URL + TOPIC_URL, params, handler);
    }

    public static void getFoucsTopics(int uid,int page,int per_page,JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,FOCUS_TOPIC_LIST_URL);
        params.put("uid",uid);
        params.put("page",page);
        params.put("per_page",per_page);

        sClient.get(BASE_URL + FOCUS_TOPIC_LIST_URL,params,handler);
    }

    public static void getTopicDetail(int topicId, int uid, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, TOPIC_DETAIL_URL);
//        params.put("uid", uid);
//        params.put("topic_id", topicId);
        params.put("id",topicId);

        sClient.get(BASE_URL + TOPIC_DETAIL_URL, params, handler);
    }

    public static void getTopicBestAnswer(int topicId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, TOPIC_BEST_ANSWER);
        params.put("topic_id", topicId);

        sClient.get(BASE_URL + TOPIC_BEST_ANSWER, params, handler);
    }

    public static void focusTopic(int topicId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,FOCUS_TOPIC_URL);
        params.put("topic_id", topicId);

        sClient.get(BASE_URL + FOCUS_TOPIC_URL, params, handler);
    }

    public static String getTopicPicUrl(String url) {
        return BASE_IMG_URL + "uploads/topic/"+url;
    }

    public static void getQuestion(int questionId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, QUESTION_URL);
        params.put("id", questionId);

        sClient.get(BASE_URL + QUESTION_URL, params, handler);
    }

    public static void focusQuestion(int questionId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("question_id", questionId);

        sClient.get(BASE_URL + FOCUS_QUESTION_URL, params, handler);
    }

    public static void getArticle(int articleId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, ARTICLE_ARTICLE_URL);
        params.put("id", articleId);
        sClient.get(BASE_URL + ARTICLE_ARTICLE_URL, params, handler);
    }

    public static void getArticleComment(int articleId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, ARTICLE_COMMENT_URL);
        params.put("id", articleId);
        params.put("page", 0);
        sClient.get(BASE_URL + ARTICLE_COMMENT_URL, params, handler);
    }

    public static void publishArticleComment(int articleId, String message, JsonHttpResponseHandler handler) {
        //Uri url = Uri.parse(BASE_URL + PUBLISH_ARTICLE_COMMENT_URL).buildUpon().appendQueryParameter("articleId", String.valueOf(articleId)).build();
        RequestParams params = new RequestParams();
        params.put("article_id", articleId);
        params.put("message", message);
        sClient.post(BASE_URL + buildPostSignatureToURL(PUBLISH_ARTICLE_COMMENT_URL,false), params, handler);

    }

    public static void getAnswer(int answerId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, ANSWER_DETAIL_URL);
        params.put("answer_id", answerId);

        sClient.get(BASE_URL + ANSWER_DETAIL_URL, params, handler);
    }

    public static void voteAnswer(int answerId, int value) {
        RequestParams params = new RequestParams();
        params.put("answer_id", answerId);
        params.put("value", value);
        sClient.post(BASE_URL + buildPostSignatureToURL(ANSWER_VOTE_URL,false), params, new JsonHttpResponseHandler());
    }

    public static void thankAnswer(int answerId, String type, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        params.put("answer_id", answerId);
        params.put("type", "thanks");

        sClient.post(BASE_URL + buildPostSignatureToURL(ANSWER_THANK_URL,false), params, new JsonHttpResponseHandler());
    }

    public static void voteArticle(int articleId, int value) {
        RequestParams params = new RequestParams();
        params.put("type", "article");
        params.put("item_id", articleId);
        params.put("rating", value);
        sClient.post(BASE_URL + buildPostSignatureToURL(ARTICLE_VOTE_URL,false), params, new JsonHttpResponseHandler());

    }

    public static void answer(int questionId, String content, String attachKey, boolean isAnonymous, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("question_id", questionId);
        params.put("answer_content", content);
        params.put("attach_access_key", attachKey);
        if (isAnonymous) {
            params.put("anonymous", 1);
        } else {
            params.put("anonymous", 0);
        }

        sClient.post(BASE_URL + buildPostSignatureToURL(ANSWER_URL,false), params, handler);
    }

    public static void getUserInfo(int uid, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, USER_INFO_URL);
        params.put("uid", uid);

        sClient.get(BASE_URL + USER_INFO_URL, params, handler);
    }


    public static void focusUser(int uid, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,FOCUS_USER_URL);
        params.put("uid", uid);

        sClient.get(BASE_URL + FOCUS_USER_URL, params, handler);
    }

    public static String getAvatarUrl(String url) {
//        return BASE_IMG_URL + "uploads/avatar/" + url;
        return url;
    }

    public static void getComments(int answerId, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, COMMENT_URL);
        params.put("answer_id", answerId);

        sClient.get(BASE_URL + COMMENT_URL, params, handler);
    }

    public static void publishComment(int answerId, String content, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("answer_id", answerId);
        params.put("message", content);

        sClient.post(BASE_URL + buildPostSignatureToURL(PUBLISH_COMMENT_URL,false), params, handler);
    }

    public static void getActions(int actions, int uid, int page, int perPage, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,MY_ACTIONS_URL);
        params.put("actions",actions);
        params.put("uid", uid);
        params.put("page", page);
        params.put("per_page", perPage);

        sClient.get(BASE_URL + MY_ACTIONS_URL, params, handler);
    }
    public static void getMyAnswer(int uid, int page, int perPage, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, MY_ANSWER_URL);
        params.put("uid", uid);
        params.put("page", page);
        params.put("per_page", perPage);

        sClient.get(BASE_URL + MY_ANSWER_URL, params, handler);
    }

    public static void getMyQuestion(int uid, int page, int perPage, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params, MY_QUESTION_URL);
        params.put("uid", uid);
        params.put("page", page);
        params.put("per_page", perPage);

        sClient.get(BASE_URL + MY_QUESTION_URL, params, handler);
    }

    public static void publishFeedback(String title, String message, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("title", title);
        params.put("message", message);
        params.put("version", DeviceUtils.getVersionName());
        params.put("system", DeviceUtils.getSystemVersion());
        params.put("source", DeviceUtils.getSource());

        sClient.post(BASE_URL + buildPostSignatureToURL(FEEDBACK_URL,false), params, handler);
    }

    public static void getMyFollowsUser(int uid, String type, int page, int per_page, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,MY_FOLLOWS_USER_URL);
        params.put("uid",uid);
        params.put("type",type);
        params.put("page",page);
        params.put("per_page",per_page);
        sClient.get(BASE_URL + MY_FOLLOWS_USER_URL, params, handler);
    }

    public static void checkNewVersion(String version, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("version", version);

        sClient.post(BASE_IMG_URL + CHECK_UPDATE_URL, params, handler);
    }

    public static void editProfile(int uid, String username, String signature, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("nick_name", username);
        if (!TextUtils.isEmpty(signature)) {
            params.put("signature", signature);
        }

        sClient.post(BASE_URL + buildPostSignatureToURL(PROFILE_EDIT_URL,false), params, handler);
    }

    public static void avatarUpload(int uid,  String user_avatar, JsonHttpResponseHandler handler) throws FileNotFoundException {
        RequestParams params = new RequestParams();
            params.put("user_avatar", new File(user_avatar));
        sClient.post(BASE_URL + buildPostSignatureToURL(AVATAR_UPLOAD_URL,false), params, handler);
    }

    public static void getNotificationsNumberInfo(long argTimestampNow, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,NOTIFICATIONS_URL);
        params.put("time", argTimestampNow);
        sClient.get(BASE_URL + NOTIFICATIONS_URL, params, handler);
    }

    public static void getNotificationsList(int argPageNum, int argIsUnreadFlag, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,NOTIFICATIONS_LIST_URL);
        params.put("page", argPageNum);
        params.put("flag", argIsUnreadFlag);  //0:未读  1:已读

        sClient.get(BASE_URL + NOTIFICATIONS_LIST_URL, params, handler);
    }

    public static void setNotificationsMarkasread(int argNotificationId, JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,NOTIFICATIONS_MARKASREAD_URL);
        params.put("notification_id", argNotificationId);

        sClient.get(BASE_URL + NOTIFICATIONS_MARKASREAD_URL, params, handler);
    }

    public static void setNotificationsMarkAllasread(JsonHttpResponseHandler handler){
        RequestParams params = new RequestParams();
        buildGetSignatureToURL(params,NOTIFICATIONS_LIST_URL);
        sClient.get(BASE_URL + NOTIFICATIONS_MARKASREAD_URL,handler);
    }

    private static String buildPostSignatureToURL(String url, boolean useNonce) {
        String rootName = url.split("/")[1];
        String msg = rootName + ResourceHelper.getString(R.string.WENJIN_APPKEY);
        String urlExtra = "";
        if (useNonce) {
            long time = System.currentTimeMillis()/1000;
            int nonce = (new Random()).nextInt();
            if (nonce < 10000) nonce += 10000;

            msg += time + nonce;
            urlExtra = "&time=" + time + "&nonce=" + nonce;
        }
        String md = MD5Utils.hashKeyFromUrl(msg);

        return url + "?mobile_sign=" + md + urlExtra;
    }

    private static void buildGetSignatureToURL(RequestParams argParams, String url){
        String rootName = url.split("/")[1];
        String msg = rootName + ResourceHelper.getString(R.string.WENJIN_APPKEY);
        String md = MD5Utils.hashKeyFromUrl(msg);

        argParams.put(PARAM_SIGN, md);
    }

    private static void buildGetSignatureRootName(RequestParams argParams, String rootName){
        String msg = rootName + ResourceHelper.getString(R.string.WENJIN_APPKEY);
        String md = MD5Utils.hashKeyFromUrl(msg);
        argParams.put(PARAM_SIGN, md);
    }

    public static void setcookie(List<BasicClientCookie> clientCookieList)
    {
        sCookieStore.clear();
        for(BasicClientCookie cookie:clientCookieList)
        {
            sCookieStore.addCookie(cookie);
        }
    }
}
