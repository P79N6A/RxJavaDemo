package com.taobao.lambda;

/**
 * @author huichi  shaokai.ysk@alibaba-inc.com
 * @Description:
 * @date 2018/11/1 下午8:15
 */
public class StringCombiner {

    private StringBuilder builder = new StringBuilder();

    private String delim;

    public StringCombiner(String delim) {
        this.delim = delim;
    }

    public StringCombiner add(String element) {
        if (builder.length() > 0) {
            builder.append(delim);
        }
        builder.append(element);
        return this;
    }

    public StringCombiner merge(StringCombiner other) {
        builder.append(other.builder);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
