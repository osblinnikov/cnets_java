package com.github.osblinnikov.cnets.mapbuffer;
import com.github.osblinnikov.cnets.readerwriter.*;
import com.github.osblinnikov.cnets.types.IntBoxer;

public class WritesReads {
    public static int writeFunction(Writer w, int tries, boolean onDebugOutput){
        int packIterator = 0, lastSecPack = 0;
        IntBoxer ptr = null;
        long curtime =  System.currentTimeMillis();
        long endtime_sec = curtime + 1000;/*1sec*/

        for(int i=0; i<tries; ++i){
            ptr = (IntBoxer) w.writeNext(-1);
            if(ptr==null){continue;}
            ptr.value = packIterator;
            if(onDebugOutput){
                System.out.printf("Write to ptr the value %d\n", ptr);
            }
            if(w.writeFinished() != 0){System.out.println("ERROR: res !=0 after WriteFinished\n"); return -1;}

    /*STATS*/
            packIterator++;
            curtime =  System.currentTimeMillis();
            if(endtime_sec <= curtime ){
                endtime_sec = curtime + 1000;
                System.out.printf("w thread FPS: %d\n", packIterator - lastSecPack);
                lastSecPack = packIterator;
            }
        }
        return 0;
    }

    public static int readFunction(Reader r, int tries, boolean onDebugOutput){
        int packIterator = 0, lastSecPack = 0;
        IntBoxer ptr = null;
        long curtime =  System.currentTimeMillis();
        long endtime_sec = curtime + 1000;/*1sec*/

        for(int i=0; i<tries; ++i){
            ptr = (IntBoxer) r.readNext(-1);
            if(ptr==null){continue;}
            ptr.value = packIterator;
            if(onDebugOutput){
                System.out.printf("Read from ptr the value %d\n", ptr);
            }
            if(r.readFinished() != 0){System.out.println("ERROR: res !=0 after ReadFinished\n"); return -1;}

    /*STATS*/
            packIterator++;
            curtime =  System.currentTimeMillis();
            if(endtime_sec <= curtime ){
                endtime_sec = curtime + 1000;
                System.out.printf("r thread FPS: %d\n", packIterator - lastSecPack);
                lastSecPack = packIterator;
            }
        }
        return 0;
    }
}