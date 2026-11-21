# Aturan R8 untuk build release (minify + resource shrinking aktif).
# Tujuan: tetap aman untuk launcher yang banyak pakai custom View & reflect API framework.

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Custom View dipakai dari XML layout -> konstruktor (Context, AttributeSet) wajib ada.
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Parcelable / enum yang dipakai lintas komponen.
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Refine (hidden API stubs) dipanggil lewat reflect ke framework.
-dontwarn dev.rikka.tools.refine.**
-keep class dev.rikka.tools.refine.** { *; }

# Library pihak ketiga: jangan gagal build karena warning referensi opsional.
-dontwarn io.reactivex.**
-dontwarn rx.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**
-dontwarn lineageos.**
-dontwarn foundation.e.**
-dontwarn com.hoko.blur.**
