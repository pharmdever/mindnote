# Mindnote

แอปภาษาไทย: โน้ตดิบ → พรอมป์ต์สำหรับ AI ที่เลือกเอง → นำคำตอบกลับ → โน้ต / Mind Map / Flowchart

- ผัง SVG เส้นเชื่อมจริง ปรับรูปทรง สีกล่อง สีเส้น และรายละเอียด
- ส่งออก PNG / SVG และนำเข้า–ส่งออกไฟล์สำรอง JSON
- เว็บและ Android ใช้ซอร์สใน `web/` ชุดเดียวกัน
- Android 10 ขึ้นไป: ใช้งานในเครื่อง ไม่มีบัญชีหรือซิงก์ในรุ่นนี้
- APK เก็บแอปไว้ในเครื่อง ไม่โหลดเว็บจาก Sites; เลือกที่เก็บไฟล์ผ่าน Android

## ทดลองบนคอม

ติดตั้ง Python 3 และเปิดจากโฟลเดอร์โปรเจกต์:

```sh
python -m http.server 8000 --directory web
```

เปิด http://localhost:8000 และตรวจซอร์สด้วย `node scripts/check.mjs` (Node 22 ขึ้นไป)

## สร้างรีโปใหม่

ติดตั้ง Git และ GitHub CLI แล้วล็อกอินบนเครื่องของคุณ:

```sh
gh auth login
# คำสั่งต่อไปนี้สำหรับชุดซอร์สที่ยังไม่ได้มี Git เท่านั้น
git init -b main
git add .
git commit -m "Initial Mindnote web and Android app"
gh repo create pharmdever/mindnote --public --source=. --remote=origin --push
```

ไม่ต้องใส่ token ลงโค้ดหรือส่ง token ในแชต การเชื่อมต่อ GitHub ใช้สำหรับแก้ซอร์ส ส่วน Android signing key ใช้เซ็นแอป เป็นคนละอย่างกัน

## คีย์ Android สำหรับอัปเดตทับรุ่นเดิม

เก็บไฟล์ signing bundle แยกจากรีโป สำรองไว้อย่างปลอดภัย ห้าม commit `.jks` หรือรหัสผ่าน

แตก signing bundle ลงโฟลเดอร์ส่วนตัว แล้วตั้ง GitHub Actions secrets:

```sh
python scripts/configure-signing.py pharmdever/mindnote /path/to/mindnote-signing
```

สคริปต์ตั้ง `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_PASSWORD`, `ANDROID_KEY_ALIAS` โดยไม่แสดงค่าในหน้าจอ

การอัปเดตต้องใช้ application ID `com.pharmdever.mindnote` และ signing key เดิม พร้อม versionCode ที่เพิ่มขึ้น Workflow ใช้ `github.run_number`; หากย้าย workflow/รีโปหรือตั้งเลขใหม่ ต้องมากกว่ารุ่นที่เคยติดตั้ง

## ดาวน์โหลด APK

- เมื่อมี signing secrets แล้ว การ push ไป `main` จะ build รุ่นเซ็นคีย์ ตรวจลายเซ็น และเผยแพร่ APK ใน Releases อัตโนมัติ ชื่อรุ่น `v0.1.<run_number>`
- ถ้ายังไม่มี signing secrets จะสร้างเฉพาะ development APK ใน Actions → Artifacts สำหรับทดลองเท่านั้น คีย์ debug ของแต่ละ runner อาจต่างกัน
- ตั้ง signing secrets ก่อน แล้วออกรุ่น:

```sh
git tag v0.1.0
git push origin v0.1.0
```

GitHub Actions จะ build APK เซ็นด้วยคีย์เดิม และแนบ `mindnote.apk` + `SHA256SUMS.txt` ในหน้า Releases เมื่อสำเร็จ

หน้า Releases ของรีโป Public เปิดดาวน์โหลด APK ได้โดยไม่ต้องล็อกอิน ส่วน Artifacts ต้องล็อกอินและมีอายุเก็บจำกัด

## Build บนเครื่อง

ใช้ JDK 17, Android SDK platform 36, build-tools 35.0.0 และ Gradle 8.13:

```sh
gradle -p android assembleDebug
```

สำหรับ release ตั้ง environment variables ชื่อเดียวกับใน `android/app/build.gradle` แล้ว `gradle -p android assembleRelease` คีย์ต้องอยู่ภายนอกรีโป

## ข้อจำกัดและข้อมูลเดิม

ข้อมูลเว็บเดิมกับ APK แยกกัน ให้ส่งออก JSON จากเว็บเดิม แล้วนำเข้าใน APK ก่อนเปลี่ยนอุปกรณ์/ถอนแอป ข้อมูลในเครื่องจะหายเมื่อถอนแอปหรือเคลียร์ข้อมูล

แผนผังการรักษาเป็นการจัดระเบียบโน้ต ไม่ใช่ระบบตัดสินใจทางการแพทย์ ต้องตรวจเนื้อหาจากต้นฉบับและแหล่งอ้างอิง

โครงการนี้ยังต้องทดสอบ APK บนอุปกรณ์ Android จริง รวมถึงคัดลอกข้อความ เลือกไฟล์ และบันทึกภาพ รุ่นแรกยังไม่ใช่ Google Play release

## เปลี่ยนจาก APK ทดลองเป็นรุ่นเซ็นคีย์

ถ้าติดตั้ง development APK เดิมอยู่ คีย์จะต่างจาก release ให้สำรองข้อมูล JSON ก่อน ถอนแอปรุ่นทดลอง แล้วติดตั้งรุ่นเซ็นคีย์และนำเข้าข้อมูลเพียงครั้งแรก หลังจากนั้นใช้คีย์ release เดิมและ versionCode ที่เพิ่มขึ้นในการอัปเดตทับรุ่น release ได้ตามปกติ
