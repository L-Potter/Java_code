### JNLP -> JAR

解壓 JAR 文件：

> jar -xf mcs_applet.jar

修改 Manifest.mf 文件：
確保 META-INF/MANIFEST.MF 文件包含以下內容：

Manifest-Version: 1.0
Main-Class: JAppletView

重新打包 JAR 文件：
在解壓目錄下執行以下命令重新打包 JAR 文件：

> jar -cfm mcs_applet.jar META-INF/MANIFEST.MF -C . .

測試 JAR 文件：

使用以下命令在本地測試 JAR 文件：

> java -jar mcs_applet.jar

這樣可以確保應用程序能夠在離線狀態下正常運行。如果還需要修改 JNLP 文件來支持離線運行，則應該相應地修改其配置並確保所有資源都包含在 JAR 文件中。

以下為Javaws.exe -> start script (Jnlp)
```
<Jnlp spec="1.0+" codebase="http://10.114.10.2/">
    <information>
        <title>GUI</title>
        <vendor>Potter</vendor>
    </information>
    <security>
        <sandbox>
    </security>
    <resources>
        <j2se version="1.6+ 1.7+ 1.8+">
        <jar  herf="applet/classes/applet.jar">
    </resources>
        <application-desc main-class="JAppletView">
        </application-desc>
</Jnlp>
```

* \<sandbox>：
 在沙盒模式下運行，表示它將受到 Java 安全管理器的限制。 Java 安全管理器透過使用政策檔案（policy file）來管理和控制應用程式的權限。

JNLP 檔案中的 codebase 屬性：用於指定應用程式的基礎 URL，用於載入應用程式的資源文件，例如 JAR 檔案和其他資源。 

policyfile檔案中的 codeBase 參數：用於指定程式碼來源的位置，以便 Java 安全管理器可以根據程式碼來源授予特定的權限。此處的 codeBase 是一個參數，不是 JNLP 檔案中的元素。



以下為Jar's META-INF/MANIFEST.MF
Main-Class: JAppletView # equal jnlp's main-class
SplashScreen-Image: Package's PATH(dot)
會在 Java 虛擬機器（JVM）啟動並載入應用程式的主類別之前顯示，用於在應用程式啟動過程中給予使用者視覺回饋，以提升使用者體驗。


