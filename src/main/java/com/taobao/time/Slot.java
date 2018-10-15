package com.taobao.time;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/10/13 上午6:41
 */
public class Slot<E> {
    /**
     * 每个槽的编号
     */
    private int id;
    //TODO 为什么这种数据类型？
    private Map<E, E> elements = new ConcurrentHashMap<>();

    public Slot(int id) {
        this.id = id;
    }

    public void add(E e) {
        elements.put(e, e);
    }

    public E remove(E e) {
        return elements.remove(e);
    }

    public Set<E> elements() {
        return elements.keySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Slot other = (Slot) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Slot [id=" + id + ", elements=" + elements + "]";
    }
}
