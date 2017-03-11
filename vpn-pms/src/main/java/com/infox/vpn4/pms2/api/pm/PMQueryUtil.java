package com.infox.vpn4.pms2.api.pm;

import com.infox.vpn4.pms2.api.StpKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Administrator on 2016/11/30.
 */
public class PMQueryUtil {
    public static void main(String[] args) {

        System.out.println(toInString(Arrays.asList(1l, 2l, 3l, 4l)));
    }

    public static String toInString(List<Long> ids) {

        return ids == null || ids.isEmpty() ? "-1" : ids.stream().map(id -> ","+id ).reduce((a, b) -> a + b).get().substring(1);
    }

    public static String toEntityIdInString(List<String> stpKeys) {
        return stpKeys.stream().map(key -> {
            try {
                return ("," + StpKey.parse(key).getStpId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }).reduce((a, b) -> a + b).get().substring(1);
    }


}
