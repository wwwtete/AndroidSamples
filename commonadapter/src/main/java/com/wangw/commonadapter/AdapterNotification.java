package com.wangw.commonadapter;

import java.util.List;

/**
 * Created by wangw on 2016/4/14.
 */
public interface AdapterNotification<T> {

    void add(T data);

    void add(List<T> datas);

    void set(int position,T data);

    void refreshData(List<T> datas);

    void clearData();

    void removeData(int position);

}
