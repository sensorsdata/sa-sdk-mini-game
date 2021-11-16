package com.cocos.game;

import android.content.Context;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SensorsAnalyticsAutoTrackEventType;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CCSensorsDataAPI {

    private static final String VERSION = "0.0.1";
    private static boolean isAddPluginVersion = false;

    private static JSONObject addPluginVersion(JSONObject jsonObject) {
        if (!isAddPluginVersion) {
            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }
            try {
                jsonObject.put("$lib_plugin_version", new JSONArray().put("app_cocos_creator:" + VERSION));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isAddPluginVersion = true;
            return jsonObject;
        }
        return jsonObject;
    }

    private static JSONObject strToJson(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static Context getContext() {
        //3.x 版本
        Class<?> SDKWrapper = getCurrentClass("com.cocos.service.SDKWrapper");
        if (SDKWrapper != null) {
            Object object = callStaticMethod(SDKWrapper, "shared");
            if (object != null) {
                return callMethod(object, "getActivity");
            }
        } else {
            //2.x 版本
            SDKWrapper = getCurrentClass("org.cocos2dx.javascript.SDKWrapper");
            if (SDKWrapper != null) {
                Object object = callStaticMethod(SDKWrapper, "getInstance");
                if (object != null) {
                    return callMethod(object, "getContext");
                }
            }
        }
        return null;
    }

    private static Class<?> getCurrentClass(String className) {
        if (TextUtils.isEmpty(className)) {
            return null;
        }
        Class<?> currentClass = null;
        try {
            currentClass = Class.forName(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentClass;
    }

    private static <T> T callStaticMethod(Class<?> clazz, String methodName) {
        if (clazz != null) {
            Method method = getMethod(clazz, methodName);
            if (method != null) {
                try {
                    return (T) method.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Method getMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> T callMethod(Object instance, String methodName) {
        Method method = getMethod(instance.getClass(), methodName);
        if (method != null) {
            try {
                return (T) method.invoke(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void initSA(String json) {
        try {
            JSONObject jsonObject = strToJson(json);
            if (jsonObject != null) {
                String serverUlr = "";
                boolean appStart = false, appEnd = false, enableLog = false, enableEncrypt = false;
                Object serverUlrObj = jsonObject.opt("serverUrl");
                Object appStartObj = jsonObject.opt("appStart");
                Object appEndObj = jsonObject.opt("appEnd");
                Object enableLogObj = jsonObject.opt("enableLog");
                Object enableEncryptObj = jsonObject.opt("enableEncrypt");
                if (serverUlrObj instanceof String) {
                    serverUlr = (String) serverUlrObj;
                }
                if (appStartObj instanceof Boolean) {
                    appStart = (boolean) appStartObj;
                }
                if (appEndObj instanceof Boolean) {
                    appEnd = (boolean) appEndObj;
                }
                if (enableLogObj instanceof Boolean) {
                    enableLog = (boolean) enableLogObj;
                }
                if (enableEncryptObj instanceof Boolean) {
                    enableEncrypt = (boolean) enableEncryptObj;
                }
                SAConfigOptions configOptions = new SAConfigOptions(serverUlr);
                configOptions.enableLog(enableLog)
                        .enableEncrypt(enableEncrypt);
                int autoTrackType = 0;
                if (appStart) {
                    autoTrackType = SensorsAnalyticsAutoTrackEventType.APP_START;
                }
                if (appEnd) {
                    autoTrackType = autoTrackType | SensorsAnalyticsAutoTrackEventType.APP_END;
                }
                configOptions.setAutoTrackEventType(autoTrackType);
                SensorsDataAPI.startWithConfigOptions(getContext(), configOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void track(String eventName, String json) {
        try {
            SensorsDataAPI.sharedInstance().track(eventName, addPluginVersion(strToJson(json)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void login(String loginId) {
        try {
            SensorsDataAPI.sharedInstance().login(loginId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logout() {
        try {
            SensorsDataAPI.sharedInstance().logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void identify(String distinctId) {
        try {
            SensorsDataAPI.sharedInstance().identify(distinctId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setOnceProfile(String json) {
        try {
            JSONObject jsonObject = strToJson(json);
            if(jsonObject != null){
                SensorsDataAPI.sharedInstance().profileSetOnce(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setProfile(String json) {
        try {
            JSONObject jsonObject = strToJson(json);
            if(jsonObject != null){
                SensorsDataAPI.sharedInstance().profileSet(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerApp(String json) {
        try {
            SensorsDataAPI.sharedInstance().registerSuperProperties(strToJson(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearAppRegister(String superPropertyKey) {
        try {
            SensorsDataAPI.sharedInstance().unregisterSuperProperty(superPropertyKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setIncrementProfile(String key, float value) {
        try {
            SensorsDataAPI.sharedInstance().profileIncrement(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void setAppendProfile(String json) {
        try {
            JSONObject jsonObject = strToJson(json);
            if (jsonObject != null) {
                Iterator<String> keys = jsonObject.keys();
                String key = keys.next();
                if (!TextUtils.isEmpty(key)) {
                    JSONArray array = jsonObject.optJSONArray(key);
                    int size = array.length();
                    Set<String> valueSet = new HashSet<>();
                    for (int i = 0; i < size; i++) {
                        if (!(array.get(i) instanceof CharSequence)) {
                            throw new Exception("The array property value must be an instance of "
                                    + "JSONArray only contains String. [key='" + key
                                    + "', value='" + array.get(i).toString()
                                    + "']");
                        } else {
                            valueSet.add(array.optString(i));
                        }
                    }
                    SensorsDataAPI.sharedInstance().profileAppend(key, valueSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void profileUnset(String key) {
        try {
            SensorsDataAPI.sharedInstance().profileUnset(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void profileDelete() {
        try {
            SensorsDataAPI.sharedInstance().profileDelete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void flush() {
        try {
            SensorsDataAPI.sharedInstance().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String trackTimerStart(String eventName) {
        try {
            return SensorsDataAPI.sharedInstance().trackTimerStart(eventName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void trackTimerPause(String eventName) {
        try {
            SensorsDataAPI.sharedInstance().trackTimerPause(eventName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackTimerResume(String eventName) {
        try {
            SensorsDataAPI.sharedInstance().trackTimerResume(eventName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackTimerEnd(String eventName, String json) {
        try {
            SensorsDataAPI.sharedInstance().trackTimerEnd(eventName, addPluginVersion(strToJson(json)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackAppInstall(String json) {
        try {
            SensorsDataAPI.sharedInstance().trackAppInstall(strToJson(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackAppViewScreen(String url, String json) {
        try {
            JSONObject jsonObject = strToJson(json);
            if(jsonObject == null){
                // url 和 json 都为空时，也需要触发事件
                jsonObject = new JSONObject();
            }
            SensorsDataAPI.sharedInstance().trackViewScreen(url, jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDistinctId() {
        try {
            return SensorsDataAPI.sharedInstance().getDistinctId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAnonymousId() {
        try {
            return SensorsDataAPI.sharedInstance().getAnonymousId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getLoginId() {
        try {
            return SensorsDataAPI.sharedInstance().getLoginId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPresetProperties() {
        try {
            JSONObject presetProperties = SensorsDataAPI.sharedInstance().getPresetProperties();
            if (presetProperties != null) {
                return presetProperties.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}