// Origin-scoped Android messaging; normal browsers keep their standard download.
const browserDownload=download;
download=async function(blob,name){
 if(!window.MindnoteNative?.postMessage){browserDownload(blob,name);return}
 try{
  if(blob.size>48*1024*1024)throw Error('ไฟล์ใหญ่เกินไปสำหรับ Android กรุณาใช้ SVG');
  const reader=new FileReader();
  const base64=await new Promise((resolve,reject)=>{reader.onload=()=>resolve(String(reader.result).split(',')[1]);reader.onerror=()=>reject(Error('อ่านไฟล์ไม่ได้'));reader.readAsDataURL(blob)});
  window.MindnoteNative.postMessage(JSON.stringify({name,type:blob.type.split(';')[0],base64}));
 }catch(e){toast(e.message)}
};
$('export').onclick=()=>download(new Blob([JSON.stringify({version:1,exportedAt:new Date().toISOString(),records,draft:Object.fromEntries(fields.map(k=>[k,$(k).value])),current:result},null,2)],{type:'application/json'}),'mindnote-backup-'+new Date().toISOString().slice(0,10)+'.json');
