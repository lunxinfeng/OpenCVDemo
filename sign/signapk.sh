#!/bin/sh

# 转换系统签名命令
java -jar signapk.jar platform.x509.pem platform.pk8 app-debug.apk new.apk

# platform.pk8、platform.x509.pem : 系统签名文件

