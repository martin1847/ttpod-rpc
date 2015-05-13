package test;

/**
 * @author: yangyang.cong@ttpod.com
 */
public class B {


    public static int a(int a[],int i){
        a[i++] = 1;
        return i;
    }

    public static int b(int a[],int i){
        a[i] = 2; i++;
        return i;
    }


}
