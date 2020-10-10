package cn.luern0313.wristReaderFTP.util;

import android.Manifest;
import android.content.pm.PackageManager;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import androidx.core.app.ActivityCompat;


/**
 * 被 luern0313 创建于 2020/2/3.
 */

public class DataProcessUtil
{
    public static float getFloatRandom(Random r, float lrange, float urange)
    {
        return r.nextFloat() * (urange - lrange) + lrange;
    }

    public static String getMinFromSec(int sec)
    {
        String m = String.valueOf(sec / 60);
        String s = String.valueOf(sec - sec / 60 * 60);
        if(m.length() == 1) m = "0" + m;
        if(s.length() == 1) s = "0" + s;
        return m + ":" + s;
    }

    public static String joinList(String[] list, String split)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.length; i++)
            stringBuilder.append(i == 0 ? "" : split).append(list[i]);
        return stringBuilder.toString();
    }

    public static String joinArrayList(ArrayList<String> list, String split)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
            stringBuilder.append(i == 0 ? "" : split).append(list.get(i));
        return stringBuilder.toString();
    }

    public static int dip2px(float dpValue)
    {
        final float scale = MyApplication.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(float spValue)
    {
        final float fontScale = MyApplication.getContext().getResources()
                .getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getPositionInArrayList(ArrayList<String> arrayList, String string)
    {
        for (int i = 0; i < arrayList.size(); i++)
            if(arrayList.get(i).equals(string)) return i;
        return -1;
    }

    public static <T extends Serializable> int getPositionInList(T[] list, T element)
    {
        for (int i = 0; i < list.length; i++)
            if(list[i].equals(element)) return i;
        return -1;
    }

    public static String getSize(long size)
    {
        String[] unit = new String[]{"B", "KB", "MB", "GB"};
        long s = size * 10;
        int u = 0;
        while (s > 10240 && u < unit.length - 1)
        {
            s /= 1024;
            u++;
        }
        return s / 10.0 + unit[u];
    }

    public static String getSurplusTime(long surplusByte, int speed)
    {
        if(speed <= 0) return "未知";
        long time = surplusByte / speed;

        String sec = String.valueOf(time % 60);
        if(sec.length() == 1) sec = "0" + sec;
        String min = String.valueOf(time / 60 % 60);
        if(min.length() == 1) min = "0" + min;
        String hour = String.valueOf(time / 3600 % 60);
        if(hour.length() == 1) hour = "0" + hour;

        if(hour.equals("00")) return min + ":" + sec;
        else return hour + ":" + min + ":" + sec;
    }

    public static String getTime(int timeStamp, String pattern)
    {
        try
        {
            Date date = new Date(timeStamp * 1000L);
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
            return format.format(date);
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean checkSelfPermission(String[] permission)
    {
        for (String i : permission)
            if(ActivityCompat.checkSelfPermission(MyApplication.getContext(), i) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }
}
