package com.izis.yzext.update;

public class VersionMessage {
    private int type;// 1 = 手机版本;// 2 = 电脑版本
    private int versionCode;// 服务器上的版本
    private String app_name;
    private String app_describe;



    public VersionMessage() {
        super();
    }

    public VersionMessage(int type, int versionCode, String app_name,
                          String app_describe) {
        super();
        this.type = type;
        this.versionCode = versionCode;
        this.app_name = app_name;
        this.app_describe = app_describe;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getVersionCode() {
        return versionCode;
    }
    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
    public String getApp_name() {
        return app_name;
    }
    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }
    public String getApp_describe() {
        return app_describe;
    }
    public void setApp_describe(String app_describe) {
        this.app_describe = app_describe;
    }
    @Override
    public String toString() {
        return "VersionMessage [type=" + type + ", versionCode=" + versionCode
                + ", app_name=" + app_name + ", app_describe=" + app_describe
                + "]";
    }

}
