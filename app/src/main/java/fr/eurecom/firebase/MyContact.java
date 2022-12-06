package fr.eurecom.firebase;

import java.util.Comparator;

public class MyContact {


    public String name;
    public String phone;

    public static Comparator<MyContact> nameAscending = new Comparator<MyContact>() {
        @Override
        public int compare(MyContact a, MyContact b) {

            return a.name.compareTo(b.name);
        }
    };

}
