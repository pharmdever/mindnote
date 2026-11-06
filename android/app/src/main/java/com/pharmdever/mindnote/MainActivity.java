package com.pharmdever.mindnote;
import android.app.*;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import androidx.webkit.*;
import org.json.JSONObject;
import java.io.OutputStream;
import java.util.Collections;
public class MainActivity extends Activity {
 private WebView web;
 private ValueCallback<Uri[]> chooser;
 private byte[] pendingDownload;
 private static final String ORIGIN="https://appassets.androidplatform.net";
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  web=new WebView(this);setContentView(web);
  web.setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();});
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(false);
  web.getSettings().setAllowContentAccess(false);
  web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
    WebResourceResponse r=loader.shouldInterceptRequest(request.getUrl());
    return r!=null?r:new WebResourceResponse("text/plain","UTF-8",403,"Blocked",Collections.emptyMap(),new java.io.ByteArrayInputStream(new byte[0]));
   }
   @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
    Uri uri=request.getUrl();
    if(ORIGIN.equals(uri.getScheme()+"://"+uri.getAuthority())&&uri.getPath()!=null&&uri.getPath().startsWith("/assets/"))return false;
    if(request.isForMainFrame()&&("https".equals(uri.getScheme())||"http".equals(uri.getScheme())))try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception ignored){}
    return true;
   }
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onJsAlert(WebView v,String u,String m,JsResult r){new AlertDialog.Builder(MainActivity.this).setMessage(m).setPositiveButton("ตกลง",(d,w)->r.confirm()).setOnCancelListener(d->r.cancel()).show();return true;}
   @Override public boolean onJsConfirm(WebView v,String u,String m,JsResult r){new AlertDialog.Builder(MainActivity.this).setMessage(m).setPositiveButton("ตกลง",(d,w)->r.confirm()).setNegativeButton("ยกเลิก",(d,w)->r.cancel()).setOnCancelListener(d->r.cancel()).show();return true;}
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> callback,FileChooserParams params){
    if(chooser!=null)chooser.onReceiveValue(null);chooser=callback;
    try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),100);}catch(Exception e){chooser.onReceiveValue(null);chooser=null;}
    return true;
   }
  });
  if(WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER))WebViewCompat.addWebMessageListener(web,"MindnoteNative",Collections.singleton(ORIGIN),(view,message,origin,main,reply)->{
   if(!main)return;
   try{
    if(pendingDownload!=null)throw new IllegalStateException("โปรดบันทึกไฟล์ก่อนหน้าก่อน");
    String payload=message.getData();if(payload==null||payload.length()>70000000)throw new IllegalArgumentException("ไฟล์ใหญ่เกินไป");
    JSONObject obj=new JSONObject(payload);String type=obj.getString("type");
    if(!type.equals("image/png")&&!type.equals("image/svg+xml")&&!type.equals("application/json"))throw new IllegalArgumentException("ชนิดไฟล์ไม่รองรับ");
    byte[] bytes=android.util.Base64.decode(obj.getString("base64"),android.util.Base64.DEFAULT);
    String name=obj.getString("name").replaceAll("[^a-zA-Z0-9._-]","_");
    pendingDownload=bytes;
    startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType(type).addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,name),101);
   }catch(Exception e){pendingDownload=null;android.widget.Toast.makeText(this,"ดาวน์โหลดไม่ได้: "+e.getMessage(),android.widget.Toast.LENGTH_LONG).show();}
  });
  web.loadUrl(ORIGIN+"/assets/index.html");
 }
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(request==100&&chooser!=null){chooser.onReceiveValue(result==RESULT_OK&&data!=null&&data.getData()!=null?new Uri[]{data.getData()}:null);chooser=null;}
  if(request==101){byte[] bytes=pendingDownload;pendingDownload=null;
   if(result==RESULT_OK&&data!=null&&data.getData()!=null&&bytes!=null)new Thread(()->{
    try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new IllegalStateException();out.write(bytes);runOnUiThread(()->android.widget.Toast.makeText(this,"บันทึกไฟล์แล้ว",android.widget.Toast.LENGTH_SHORT).show());}
    catch(Exception e){runOnUiThread(()->android.widget.Toast.makeText(this,"บันทึกไฟล์ไม่ได้",android.widget.Toast.LENGTH_LONG).show());}
   }).start();
  }
 }
 @Override public void onBackPressed(){new AlertDialog.Builder(this).setMessage("ออกจาก Mind Map? โปรดบันทึกผลงานก่อนออก").setPositiveButton("ออก",(d,w)->finish()).setNegativeButton("อยู่ต่อ",null).show();}
 @Override protected void onDestroy(){if(chooser!=null)chooser.onReceiveValue(null);web.destroy();super.onDestroy();}
}
